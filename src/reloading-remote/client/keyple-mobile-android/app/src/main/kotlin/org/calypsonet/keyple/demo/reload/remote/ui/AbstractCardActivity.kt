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

import android.os.Build
import android.os.Bundle
import javax.inject.Inject
import kotlin.jvm.Throws
import org.calypsonet.keyple.demo.common.constant.CardConstant
import org.calypsonet.keyple.demo.reload.remote.data.ReaderRepository
import org.calypsonet.keyple.demo.reload.remote.data.model.AppSettings
import org.calypsonet.keyple.demo.reload.remote.data.model.CardProtocolEnum
import org.calypsonet.keyple.demo.reload.remote.data.model.CardReaderResponse
import org.calypsonet.keyple.demo.reload.remote.data.model.DeviceEnum
import org.calypsonet.keyple.demo.reload.remote.data.model.Status
import org.calypsonet.keyple.plugin.bluebird.BluebirdConstants
import org.calypsonet.keyple.plugin.bluebird.BluebirdContactlessProtocols
import org.calypsonet.keyple.plugin.bluebird.BluebirdPluginFactoryProvider
import org.calypsonet.keyple.plugin.storagecard.ApduInterpreterFactoryProvider
import org.eclipse.keyple.core.service.KeyplePluginException
import org.eclipse.keyple.core.service.Plugin
import org.eclipse.keyple.distributed.LocalServiceClient
import org.eclipse.keyple.plugin.android.nfc.AndroidNfcConfig
import org.eclipse.keyple.plugin.android.nfc.AndroidNfcConstants
import org.eclipse.keyple.plugin.android.nfc.AndroidNfcPluginFactoryProvider
import org.eclipse.keyple.plugin.android.nfc.AndroidNfcSupportedProtocols
import org.eclipse.keyple.plugin.android.omapi.AndroidOmapiPlugin
import org.eclipse.keyple.plugin.android.omapi.AndroidOmapiPluginFactoryProvider
import org.eclipse.keyple.plugin.android.omapi.AndroidOmapiReader
import org.eclipse.keypop.reader.ConfigurableCardReader
import org.eclipse.keypop.reader.ObservableCardReader
import org.eclipse.keypop.reader.spi.CardReaderObservationExceptionHandlerSpi
import org.eclipse.keypop.reader.spi.CardReaderObserverSpi
import timber.log.Timber

abstract class AbstractCardActivity :
    AbstractDemoActivity(), CardReaderObserverSpi, CardReaderObservationExceptionHandlerSpi {

  @Inject lateinit var localServiceClient: LocalServiceClient
  @Inject lateinit var readerRepository: ReaderRepository
  lateinit var selectedDeviceReaderName: String
  lateinit var device: DeviceEnum
  lateinit var pluginType: String

  val isBluebirdDevice = Build.MANUFACTURER?.lowercase()?.contains("bluebird") == true

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    device = DeviceEnum.getDeviceEnum(prefData.loadDeviceType()!!)
    selectedDeviceReaderName =
        when (device) {
          DeviceEnum.CONTACTLESS_CARD -> {
            pluginType = if (isBluebirdDevice) "Bluebird" else "Android Nfc"
            AppSettings.aidEnums.clear()
            AppSettings.aidEnums.add(CardConstant.AID_KEYPLE_GENERIC)
            AppSettings.aidEnums.add(CardConstant.AID_CD_LIGHT_GTML)
            AppSettings.aidEnums.add(CardConstant.AID_CALYPSO_LIGHT)
            AppSettings.aidEnums.add(CardConstant.AID_NORMALIZED_IDF)
            if (isBluebirdDevice) BluebirdConstants.CARD_READER_NAME
            else AndroidNfcConstants.READER_NAME
          }
          DeviceEnum.SIM -> {
            pluginType = "Android OMAPI"
            AppSettings.aidEnums.clear()
            AppSettings.aidEnums.add(CardConstant.AID_CD_LIGHT_GTML)
            AppSettings.aidEnums.add(CardConstant.AID_NORMALIZED_IDF)
            AndroidOmapiReader.READER_NAME_SIM_1
          }
          DeviceEnum.WEARABLE -> {
            pluginType = "Android WEARABLE"
            AppSettings.aidEnums.clear()
            AppSettings.aidEnums.add(CardConstant.AID_CD_LIGHT_GTML)
            "WEARABLE"
          }
          DeviceEnum.EMBEDDED -> {
            pluginType = "Android EMBEDDED"
            AppSettings.aidEnums.clear()
            AppSettings.aidEnums.add(CardConstant.AID_CD_LIGHT_GTML)
            "EMBEDDED"
          }
        }
  }

  override fun onResume() {
    super.onResume()
    initReaders()
  }

  /** Android Nfc Reader is strongly dependent and Android Activity component. */
  @Throws(KeyplePluginException::class)
  fun initAndActivateCardReader() {
    val plugin: Plugin? =
        readerRepository.registerPlugin(
            if (isBluebirdDevice) {
              BluebirdPluginFactoryProvider.provideFactory(
                  this@AbstractCardActivity, ApduInterpreterFactoryProvider.provideFactory())
            } else {
              AndroidNfcPluginFactoryProvider.provideFactory(
                  AndroidNfcConfig(
                      this@AbstractCardActivity, ApduInterpreterFactoryProvider.provideFactory()))
            })

    if (plugin == null) {
      return
    }

    val observableCardReader: ObservableCardReader =
        readerRepository.getObservableReader(selectedDeviceReaderName)
    observableCardReader.setReaderObservationExceptionHandler(this@AbstractCardActivity)
    observableCardReader.addObserver(this@AbstractCardActivity)
    observableCardReader.setReaderObservationExceptionHandler(this@AbstractCardActivity)

    val configurableReader = observableCardReader as ConfigurableCardReader

    if (isBluebirdDevice) {
      configurableReader.activateProtocol(
          BluebirdContactlessProtocols.ISO_14443_4_A.name,
          CardProtocolEnum.ISO_14443_4_LOGICAL_PROTOCOL.name)
      configurableReader.activateProtocol(
          BluebirdContactlessProtocols.ISO_14443_4_B.name,
          CardProtocolEnum.ISO_14443_4_LOGICAL_PROTOCOL.name)
      configurableReader.activateProtocol(
          BluebirdContactlessProtocols.MIFARE_ULTRALIGHT.name,
          CardProtocolEnum.MIFARE_ULTRALIGHT_LOGICAL_PROTOCOL.name)
      configurableReader.activateProtocol(
          BluebirdContactlessProtocols.ST25_SRT512.name,
          CardProtocolEnum.ST25_SRT512_LOGICAL_PROTOCOL.name)
    } else {
      configurableReader.activateProtocol(
          AndroidNfcSupportedProtocols.ISO_14443_4.name,
          CardProtocolEnum.ISO_14443_4_LOGICAL_PROTOCOL.name)
      configurableReader.activateProtocol(
          AndroidNfcSupportedProtocols.MIFARE_ULTRALIGHT.name,
          CardProtocolEnum.MIFARE_ULTRALIGHT_LOGICAL_PROTOCOL.name)
    }

    observableCardReader.startCardDetection(ObservableCardReader.DetectionMode.REPEATING)
  }

  @Throws(KeyplePluginException::class)
  fun deactivateAndClearCardReader() {
    (readerRepository.getReader(selectedDeviceReaderName) as ObservableCardReader)
        .stopCardDetection()
    readerRepository.unregisterPlugin(
        if (isBluebirdDevice) BluebirdConstants.PLUGIN_NAME else AndroidNfcConstants.PLUGIN_NAME)
  }

  /**
   * Initialisation of AndroidOmapiPlugin is async and take time and cannot be observed. So we'll
   * trigger process only when the plugin is registered
   */
  @Throws(KeyplePluginException::class)
  fun initOmapiReader(callback: () -> Unit) {
    AndroidOmapiPluginFactoryProvider(this@AbstractCardActivity) {
      readerRepository.registerPlugin(it)
      callback()
    }
  }

  @Throws(KeyplePluginException::class)
  fun deactivateAndClearOmapiReader() {
    readerRepository.unregisterPlugin(AndroidOmapiPlugin.PLUGIN_NAME)
  }

  fun launchInvalidCardResponse(cardType: String, message: String) {
    runOnUiThread {
      changeDisplay(
          CardReaderResponse(
              Status.INVALID_CARD, cardType, 0, arrayListOf(), arrayListOf(), "", message),
          finishActivity =
              device !=
                  DeviceEnum.CONTACTLESS_CARD // /Only with NFC we can come back to 'wait for device
          // screen'
          )
    }
  }

  fun launchCardCommunicationErrorResponse() {
    runOnUiThread {
      changeDisplay(
          CardReaderResponse(
              Status.ERROR, "", 0, arrayListOf(), arrayListOf(), "", "Card communication error"),
          finishActivity =
              device !=
                  DeviceEnum.CONTACTLESS_CARD // /Only with NFC we can come back to 'wait for device
          // screen'
          )
    }
  }

  fun launchServerErrorResponse() {
    runOnUiThread {
      changeDisplay(
          CardReaderResponse(Status.ERROR, "", 0, arrayListOf(), arrayListOf(), ""),
          finishActivity =
              device !=
                  DeviceEnum.CONTACTLESS_CARD // /Only with NFC we can come back to 'wait for device
          // screen'
          )
    }
  }

  fun launchExceptionResponse(e: Exception, finishActivity: Boolean? = false) {
    runOnUiThread {
      changeDisplay(
          CardReaderResponse(Status.ERROR, "", 0, arrayListOf(), arrayListOf(), "", e.message),
          finishActivity = finishActivity)
    }
  }

  protected abstract fun changeDisplay(
      cardReaderResponse: CardReaderResponse,
      applicationSerialNumber: String? = null,
      finishActivity: Boolean? = false
  )

  override fun onReaderObservationError(contextInfo: String?, readerName: String?, e: Throwable?) {
    Timber.e(e)
    Timber.d("Error on $contextInfo, $readerName")
    this@AbstractCardActivity.finish()
  }

  protected abstract fun initReaders()

  companion object {
    const val CARD_APPLICATION_NUMBER = "cardApplicationNumber"
    const val CARD_CONTENT = "cardContent"
  }
}
