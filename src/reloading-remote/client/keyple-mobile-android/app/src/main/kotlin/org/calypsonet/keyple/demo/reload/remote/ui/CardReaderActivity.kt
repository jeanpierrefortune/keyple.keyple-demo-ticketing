/* ******************************************************************************
 * Copyright (c) 2021 Calypso Networks Association https://calypsonet.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the BSD 3-Clause License which is available at
 * https://opensource.org/licenses/BSD-3-Clause.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 ****************************************************************************** */
package org.calypsonet.keyple.demo.reload.remote.ui

import android.content.Intent
import android.nfc.NfcManager
import android.os.Bundle
import android.view.View
import java.lang.IllegalStateException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlin.Exception
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.calypsonet.keyple.demo.common.constant.RemoteServiceId
import org.calypsonet.keyple.demo.common.dto.AnalyzeContractsInputDto
import org.calypsonet.keyple.demo.common.dto.AnalyzeContractsOutputDto
import org.calypsonet.keyple.demo.common.model.ContractStructure
import org.calypsonet.keyple.demo.common.model.type.PriorityCode
import org.calypsonet.keyple.demo.reload.remote.R
import org.calypsonet.keyple.demo.reload.remote.data.model.*
import org.calypsonet.keyple.demo.reload.remote.databinding.ActivityCardReaderBinding
import org.calypsonet.keyple.demo.reload.remote.di.scopes.ActivityScoped
import org.calypsonet.keyple.demo.reload.remote.domain.TicketingService
import org.calypsonet.keyple.demo.reload.remote.ui.cardsummary.CardSummaryActivity
import org.eclipse.keyple.core.service.KeyplePluginException
import org.eclipse.keyple.core.util.HexUtil
import org.eclipse.keypop.calypso.card.card.CalypsoCard
import org.eclipse.keypop.reader.CardReaderEvent
import org.eclipse.keypop.reader.ReaderCommunicationException
import org.eclipse.keypop.storagecard.card.StorageCard
import timber.log.Timber

@ActivityScoped
class CardReaderActivity : AbstractCardActivity() {

  @Inject lateinit var ticketingService: TicketingService

  private val dateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH)
  private lateinit var activityCardReaderBinding: ActivityCardReaderBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityCardReaderBinding = ActivityCardReaderBinding.inflate(layoutInflater)
    toolbarBinding = activityCardReaderBinding.appBarLayout
    setContentView(activityCardReaderBinding.root)
  }

  override fun initReaders() {
    try {
      when (device) {
        DeviceEnum.CONTACTLESS_CARD -> {
          if (!isBluebirdDevice) {
            val nfcManager = getSystemService(NFC_SERVICE) as NfcManager
            if (nfcManager.defaultAdapter?.isEnabled == true) {
              showPresentNfcCardInstructions()
              initAndActivateCardReader()
            } else {
              launchExceptionResponse(
                  IllegalStateException("NFC not activated"), finishActivity = true)
            }
          } else {
            showPresentNfcCardInstructions()
            initAndActivateCardReader()
          }
        }
        DeviceEnum.SIM -> {
          showNowLoadingInformation()
          initOmapiReader {
            GlobalScope.launch {
              remoteServiceExecution(
                  selectedDeviceReaderName, pluginType, AppSettings.aidEnums, null)
            }
          }
        }
        DeviceEnum.WEARABLE -> {
          throw KeyplePluginException("Wearable")
        }
        DeviceEnum.EMBEDDED -> {
          throw KeyplePluginException("Embedded")
        }
      }
    } catch (e: ReaderCommunicationException) {
      Timber.e(e)
      launchExceptionResponse(e, true)
    } catch (e: Exception) {
      Timber.e(e)
    }
  }

  override fun onPause() {
    activityCardReaderBinding.cardAnimation.cancelAnimation()
    activityCardReaderBinding.loadingAnimation.cancelAnimation()
    try {
      if (DeviceEnum.getDeviceEnum(prefData.loadDeviceType()!!) == DeviceEnum.CONTACTLESS_CARD) {
        deactivateAndClearCardReader()
      } else {
        deactivateAndClearOmapiReader()
      }
    } catch (e: Exception) {
      Timber.e(e)
    }
    super.onPause()
  }

  override fun onReaderEvent(event: CardReaderEvent?) {
    if (event?.type == CardReaderEvent.Type.CARD_INSERTED) {
      // We'll select Card when SmartCard is presented in field
      // Method handlePo is described below
      runOnUiThread { showNowLoadingInformation() }
      GlobalScope.launch {
        remoteServiceExecution(
            selectedDeviceReaderName,
            pluginType,
            AppSettings.aidEnums,
            "ISO_14443_4_LOGICAL_PROTOCOL")
      }
    }
  }

  private suspend fun remoteServiceExecution(
      selectedDeviceReaderName: String,
      pluginType: String,
      aidEnums: ArrayList<ByteArray>,
      protocol: String?
  ) {
    withContext(Dispatchers.IO) {
      try {
        val smartCard = ticketingService.getSmartCard(selectedDeviceReaderName, aidEnums)
        val cardType =
            when (smartCard) {
              is CalypsoCard -> "CALYPSO: DF name " + HexUtil.toHex(smartCard.dfName)
              is StorageCard -> smartCard.productType.name
              else -> "unexpected card type"
            }
        val analyseContractsInput = AnalyzeContractsInputDto(pluginType)
        // un-mock for run
        val compatibleContractOutput =
            localServiceClient.executeRemoteService(
                RemoteServiceId.READ_CARD_AND_ANALYZE_CONTRACTS.name,
                selectedDeviceReaderName,
                smartCard,
                analyseContractsInput,
                AnalyzeContractsOutputDto::class.java)

        when (compatibleContractOutput.statusCode) {
          0 -> {
            runOnUiThread {
              val contracts = compatibleContractOutput.validContracts
              val status = if (contracts.isNotEmpty()) Status.TICKETS_FOUND else Status.EMPTY_CARD
              val finishActivity =
                  device !=
                      DeviceEnum
                          .CONTACTLESS_CARD // Only with NFC we can come back to 'wait for device
              // screen'

              when (smartCard) {
                is CalypsoCard -> {
                  changeDisplay(
                      CardReaderResponse(
                          status,
                          cardType,
                          contracts.size,
                          buildCardTitles(contracts),
                          arrayListOf(),
                          ""),
                      HexUtil.toHex(smartCard!!.applicationSerialNumber),
                      finishActivity)
                }
                is StorageCard -> {
                  changeDisplay(
                      CardReaderResponse(
                          status,
                          cardType,
                          contracts.size,
                          buildCardTitles(contracts),
                          arrayListOf(),
                          ""),
                      HexUtil.toHex(smartCard!!.uid),
                      finishActivity)
                }
              }
            }
          } // success,
          1 -> {
            launchServerErrorResponse()
          } // server not ready,
          2 -> {
            when (smartCard) {
              is CalypsoCard -> {
                launchInvalidCardResponse(
                    cardType,
                    String.format(
                        getString(R.string.card_invalid_structure),
                        HexUtil.toHex(smartCard!!.applicationSubtype)))
              }
              is StorageCard -> {
                launchInvalidCardResponse(cardType, getString(R.string.storage_card_invalid))
              }
              else -> {}
            } // card rejected
          }
          3 -> {
            launchInvalidCardResponse(cardType, getString(R.string.card_not_personalized))
          } // card not personalized
          4 -> {
            launchInvalidCardResponse(cardType, getString(R.string.expired_environment))
          } // expired environment
        }
      } catch (e: IllegalStateException) {
        Timber.e(e)
        launchInvalidCardResponse("Undetermined card type", e.message!!)
      } catch (e: Exception) {
        Timber.e(e)
        val finishActivity =
            device !=
                DeviceEnum
                    .CONTACTLESS_CARD // Only with NFC we can come back to 'wait for device screen'
        launchExceptionResponse(
            IllegalStateException("Server error:\n" + e.message), finishActivity)
      }
    }
  }

  private fun buildCardTitle(contractStructure: ContractStructure): CardTitle {
    return when (contractStructure.contractTariff) {
      PriorityCode.MULTI_TRIP -> {
        var isValid = false
        val description =
            contractStructure.counterValue?.let {
              isValid = (it >= 1)
              if (it > 1) "$it trips left" else "$it trip left"
            }
        CardTitle("Multi trip", description ?: "No counter", isValid)
      }
      PriorityCode.SEASON_PASS -> {
        val now = LocalDate.now()
        val isValid =
            (contractStructure.contractSaleDate.getDate().isBefore(now) ||
                contractStructure.contractSaleDate.getDate().isEqual(now)) &&
                (contractStructure.contractValidityEndDate.getDate().isAfter(now) ||
                    contractStructure.contractValidityEndDate.getDate().isEqual(now))
        CardTitle(
            "Season pass",
            "From ${contractStructure.contractSaleDate.getDate().format(dateTimeFormatter)} to ${contractStructure.contractValidityEndDate.getDate().format(dateTimeFormatter)}",
            isValid)
      }
      PriorityCode.EXPIRED -> {
        CardTitle(
            "Season pass - Expired",
            "From ${contractStructure.contractSaleDate.getDate().format(dateTimeFormatter)} to ${contractStructure.contractValidityEndDate.getDate().format(dateTimeFormatter)}",
            false)
      }
      PriorityCode.FORBIDDEN -> {
        CardTitle("FORBIDDEN", "", false)
      }
      PriorityCode.STORED_VALUE -> {
        CardTitle("STORED_VALUE", "", false)
      }
      else -> CardTitle("UNKNOWN", "", false)
    }
  }

  private fun buildCardTitles(contractStructures: List<ContractStructure>?): List<CardTitle> {
    val cardTitles = contractStructures?.map { buildCardTitle(it) }
    return cardTitles ?: arrayListOf()
  }

  override fun changeDisplay(
      cardReaderResponse: CardReaderResponse,
      uniqueIdentifier: String?,
      finishActivity: Boolean?
  ) {
    activityCardReaderBinding.loadingAnimation?.cancelAnimation()
    activityCardReaderBinding.cardAnimation?.cancelAnimation()
    val intent = Intent(this, CardSummaryActivity::class.java)
    intent.putExtra(CARD_CONTENT, cardReaderResponse)
    intent.putExtra(CARD_APPLICATION_NUMBER, uniqueIdentifier)
    startActivity(intent)
    if (finishActivity == true) {
      finish()
    }
  }

  private fun showPresentNfcCardInstructions() {
    activityCardReaderBinding.presentTxt.text = getString(R.string.present_travel_card_label)
    activityCardReaderBinding.cardAnimation.visibility = View.VISIBLE
    activityCardReaderBinding.cardAnimation.playAnimation()
    activityCardReaderBinding.loadingAnimation.cancelAnimation()
    activityCardReaderBinding.loadingAnimation.visibility = View.INVISIBLE
  }

  private fun showNowLoadingInformation() {
    activityCardReaderBinding.presentTxt.text = getString(R.string.read_in_progress)
    activityCardReaderBinding.loadingAnimation.visibility = View.VISIBLE
    activityCardReaderBinding.loadingAnimation.playAnimation()
    activityCardReaderBinding.cardAnimation.cancelAnimation()
    activityCardReaderBinding.cardAnimation.visibility = View.INVISIBLE
  }
}
