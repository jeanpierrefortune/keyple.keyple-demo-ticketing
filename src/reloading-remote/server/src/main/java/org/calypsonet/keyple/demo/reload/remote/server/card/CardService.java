/* ******************************************************************************
 * Copyright (c) 2022 Calypso Networks Association https://calypsonet.org/
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
package org.calypsonet.keyple.demo.reload.remote.server.card;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Collections;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.calypsonet.keyple.demo.common.constant.CardConstant;
import org.calypsonet.keyple.demo.common.dto.*;
import org.calypsonet.keyple.demo.common.model.ContractStructure;
import org.calypsonet.keyple.demo.common.model.EnvironmentHolderStructure;
import org.calypsonet.keyple.demo.common.model.EventStructure;
import org.calypsonet.keyple.demo.common.model.type.DateCompact;
import org.calypsonet.keyple.demo.common.model.type.PriorityCode;
import org.calypsonet.keyple.demo.common.model.type.VersionNumber;
import org.calypsonet.keyple.demo.reload.remote.server.activity.Activity;
import org.calypsonet.keyple.demo.reload.remote.server.activity.ActivityService;
import org.eclipse.keyple.core.service.resource.CardResource;
import org.eclipse.keyple.core.service.resource.CardResourceServiceProvider;
import org.eclipse.keyple.core.util.HexUtil;
import org.eclipse.keypop.calypso.card.card.CalypsoCard;
import org.eclipse.keypop.calypso.card.transaction.CardIOException;
import org.eclipse.keypop.reader.CardReader;
import org.eclipse.keypop.reader.selection.spi.SmartCard;
import org.eclipse.keypop.storagecard.card.StorageCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class CardService {

  private static final Logger logger = LoggerFactory.getLogger(CardService.class);
  private static final String SUCCESS = "SUCCESS";
  private static final String FAIL = "FAIL";
  private static final String READ = "READ";
  private static final String SECURED_READ = "SECURED READ";
  private static final String RELOAD = "RELOAD";
  private static final String ISSUANCE = "ISSUANCE";
  private static final String AN_ERROR_OCCURRED_WHILE_INCREASING_THE_CONTRACT_COUNTER =
      "An error occurred while increasing the contract counter: {}";
  private static final String AN_ERROR_OCCURRED_WHILE_ANALYZING_THE_CONTRACTS =
      "An error occurred while analyzing the contracts: {}";
  private static final String AN_ERROR_OCCURRED_WHILE_WRITING_THE_CONTRACT =
      "An error occurred while writing the contract: {}";
  private static final String AN_ERROR_OCCURRED_WHILE_INITIALIZING_THE_CARD =
      "An error occurred while initializing the card: {}";
  private static final String VERSION_NUMBER_OF_CARD_IS_INVALID_REJECT_CARD =
      "Version Number of card is invalid, reject card";
  private static final String ENV_END_DATE_OF_CARD_IS_INVALID_REJECT_CARD =
      "EnvEndDate of card is invalid, reject card";
  private static final String VERSION_NUMBER_OF_CARD_IS_INVALID_REJECT_CARD1 =
      "EventVersionNumber of card is invalid, reject card";
  private static final String CONTRACT_TARIFF_IS_NOT_VALID_FOR_THIS_CONTRACT =
      "Contract tariff is not valid for this contract";
  private static final String ONLY_SEASON_PASS_OR_MULTI_TRIP_TICKET_CAN_BE_LOADED =
      "Only Season Pass or Multi Trip ticket can be loaded";
  private static final String UNEXPECTED_CONTRACT_NUMBER = "Unexpected contract number: ";
  private static final String THE_CARD_IS_NOT_PERSONALIZED = "The card is not personalized";
  private static final String THE_ENVIRONMENT_HAS_EXPIRED = "The environment has expired";
  private static final String CONTRACT_AT_INDEX = "Contract at index {}: {} {}";
  private static final String CONTRACTS = "Contracts {}";
  private static final String CUSTOM_PLUGIN = "Non Keyple plugin";
  private static final String CARD_NOT_PERSONALIZED = "Card not personalized.";
  private static final String ENVIRONMENT_EXPIRED = "Environment expired.";
  private static final String RUNTIME_EXCEPTION = "Runtime exception: ";
  private static final String PROCESSED_CARD_SELECTION_SCENARIO_JSON_STRING =
      "processedCardSelectionScenarioJsonString";

  private final DateTimeFormatter dateTimeFormatter =
      DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH);

  @Inject CardRepository cardRepository;
  @Inject ActivityService activityService;

  private String formatContractStructure(ContractStructure contractStructure) {
    StringBuilder builder = new StringBuilder();

    builder
        .append("Contract Version Number: ")
        .append(contractStructure.getContractVersionNumber())
        .append("\n");

    builder.append("Contract Tariff: ").append(contractStructure.getContractTariff()).append("\n");

    builder
        .append("Contract Sale Date: ")
        .append(contractStructure.getContractSaleDate().getDate())
        .append("\n");

    builder
        .append("Contract Validity End Date: ")
        .append(contractStructure.getContractValidityEndDate().getDate())
        .append("\n");

    if (contractStructure.getContractSaleSam() != null) {
      builder
          .append("Contract Sale Sam: ")
          .append(contractStructure.getContractSaleSam())
          .append("\n");
    }

    if (contractStructure.getContractSaleCounter() != null) {
      builder
          .append("Contract Sale Counter: ")
          .append(contractStructure.getContractSaleCounter())
          .append("\n");
    }

    if (contractStructure.getContractAuthKvc() != null) {
      builder
          .append("Contract Auth Kvc: ")
          .append(contractStructure.getContractAuthKvc())
          .append("\n");
    }

    if (contractStructure.getContractAuthenticator() != null) {
      builder
          .append("Contract Authenticator: ")
          .append(contractStructure.getContractAuthenticator())
          .append("\n");
    }

    if (contractStructure.getCounterValue() != null) {
      builder.append("Counter Value: ").append(contractStructure.getCounterValue()).append("\n");
    }

    return builder.toString();
  }

  SelectAppAndReadContractsOutputDto selectAppAndReadContracts(CardReader cardReader) {

    CalypsoCard calypsoCard = null;
    List<String> output = new ArrayList<>();
    int statusCode = 0;
    String message = "Success.";
    CardResource samResource = null;

    try {
      calypsoCard = cardRepository.selectCard(cardReader);

      samResource =
          CardResourceServiceProvider.getService()
              .getCardResource(CardConfigurator.SAM_RESOURCE_PROFILE_NAME);

      Card card = cardRepository.readCard(cardReader, calypsoCard, samResource);
      // logger.info("{}", card); deactivate until LocalDate is properly processed by KeypleUtil
      activityService.push(
          new Activity()
              .setPlugin(CUSTOM_PLUGIN)
              .setStatus(SUCCESS)
              .setType(SECURED_READ)
              .setCardSerialNumber(HexUtil.toHex(calypsoCard.getApplicationSerialNumber())));
      List<ContractStructure> validContracts = findValidContracts(card);

      for (ContractStructure contractStructure : validContracts) {
        output.add(formatContractStructure(contractStructure));
      }
    } catch (CardNotPersonalizedException e) {
      statusCode = 3;
      logger.error(AN_ERROR_OCCURRED_WHILE_ANALYZING_THE_CONTRACTS, e.getMessage());
      message = CARD_NOT_PERSONALIZED;
    } catch (ExpiredEnvironmentException e) {
      statusCode = 4;
      logger.error(AN_ERROR_OCCURRED_WHILE_ANALYZING_THE_CONTRACTS, e.getMessage());
      message = ENVIRONMENT_EXPIRED;
    } catch (RuntimeException e) {
      statusCode = 1;
      logger.error(AN_ERROR_OCCURRED_WHILE_ANALYZING_THE_CONTRACTS, e.getMessage(), e);
      message = RUNTIME_EXCEPTION + e.getMessage();
    } finally {
      activityService.push(
          new Activity()
              .setPlugin(CUSTOM_PLUGIN)
              .setStatus(statusCode == 0 ? SUCCESS : FAIL)
              .setType(RELOAD)
              .setCardSerialNumber(
                  calypsoCard == null
                      ? "-"
                      : HexUtil.toHex(calypsoCard.getApplicationSerialNumber())));
      if (samResource != null) {
        CardResourceServiceProvider.getService().releaseCardResource(samResource);
      }
    }
    return new SelectAppAndReadContractsOutputDto(output, statusCode, message);
  }

  SelectAppAndIncreaseContractCounterOutputDto selectAppAndIncreaseContractCounter(
      CardReader cardReader, SelectAppAndIncreaseContractCounterInputDto inputData) {

    CalypsoCard calypsoCard = null;
    CardResource samResource = null;
    int statusCode = 0;
    String message = "Success.";
    try {
      calypsoCard = cardRepository.selectCard(cardReader);

      samResource =
          CardResourceServiceProvider.getService()
              .getCardResource(CardConfigurator.SAM_RESOURCE_PROFILE_NAME);

      Card card = cardRepository.readCard(cardReader, calypsoCard, samResource);
      // logger.info("{}", card); deactivate until LocalDate is properly processed by KeypleUtil
      activityService.push(
          new Activity()
              .setPlugin(CUSTOM_PLUGIN)
              .setStatus(SUCCESS)
              .setType(RELOAD)
              .setCardSerialNumber(HexUtil.toHex(calypsoCard.getApplicationSerialNumber()))
              .setContractLoaded("MULTI TRIP: " + inputData.getCounterIncrement()));
      insertNewContract(PriorityCode.MULTI_TRIP, inputData.getCounterIncrement(), card);
      statusCode = cardRepository.writeCard(cardReader, calypsoCard, samResource, card);
    } catch (CardNotPersonalizedException e) {
      statusCode = 3;
      message = CARD_NOT_PERSONALIZED;
      logger.error(AN_ERROR_OCCURRED_WHILE_INCREASING_THE_CONTRACT_COUNTER, e.getMessage());
    } catch (ExpiredEnvironmentException e) {
      statusCode = 4;
      message = ENVIRONMENT_EXPIRED;
      logger.error(AN_ERROR_OCCURRED_WHILE_INCREASING_THE_CONTRACT_COUNTER, e.getMessage());
    } catch (RuntimeException e) {
      statusCode = 1;
      message = RUNTIME_EXCEPTION + e.getMessage();
      logger.error(AN_ERROR_OCCURRED_WHILE_INCREASING_THE_CONTRACT_COUNTER, e.getMessage(), e);
    } finally {
      activityService.push(
          new Activity()
              .setPlugin(CUSTOM_PLUGIN)
              .setStatus(statusCode == 0 ? SUCCESS : FAIL)
              .setType(RELOAD)
              .setCardSerialNumber(
                  calypsoCard == null
                      ? "-"
                      : HexUtil.toHex(calypsoCard.getApplicationSerialNumber())));
      if (samResource != null) {
        CardResourceServiceProvider.getService().releaseCardResource(samResource);
      }
    }
    return new SelectAppAndIncreaseContractCounterOutputDto(statusCode, message);
  }

  AnalyzeContractsOutputDto analyzeContracts(
      CardReader cardReader, SmartCard smartCard, AnalyzeContractsInputDto inputData) {
    if (smartCard instanceof CalypsoCard) {
      return analyzeCalypsoCardContracts(cardReader, (CalypsoCard) smartCard, inputData);
    } else {
      return analyzeStorageCardContracts(cardReader, (StorageCard) smartCard, inputData);
    }
  }

  private AnalyzeContractsOutputDto analyzeCalypsoCardContracts(
      CardReader cardReader, CalypsoCard calypsoCard, AnalyzeContractsInputDto inputData) {

    String pluginType = inputData.getPluginType();
    String appSerialNumber = HexUtil.toHex(calypsoCard.getApplicationSerialNumber());

    if (!CardConstant.Companion.getALLOWED_FILE_STRUCTURES()
        .contains(calypsoCard.getApplicationSubtype())) {
      return new AnalyzeContractsOutputDto(Collections.emptyList(), 3);
    }

    CardResource samResource =
        CardResourceServiceProvider.getService()
            .getCardResource(CardConfigurator.SAM_RESOURCE_PROFILE_NAME);
    try {
      Card card = cardRepository.readCard(cardReader, calypsoCard, samResource);
      // logger.info("{}", card); deactivate until LocalDate is properly processed by KeypleUtil
      List<ContractStructure> validContracts = findValidContracts(card);
      activityService.push(
          new Activity()
              .setPlugin(pluginType)
              .setStatus(SUCCESS)
              .setType(SECURED_READ)
              .setCardSerialNumber(appSerialNumber));
      return new AnalyzeContractsOutputDto(validContracts, 0);
    } catch (CardNotPersonalizedException e) {
      logger.error(AN_ERROR_OCCURRED_WHILE_ANALYZING_THE_CONTRACTS, e.getMessage());
      activityService.push(
          new Activity()
              .setPlugin(pluginType)
              .setStatus(FAIL)
              .setType(SECURED_READ)
              .setCardSerialNumber(appSerialNumber));
      return new AnalyzeContractsOutputDto(Collections.emptyList(), 4);
    } catch (ExpiredEnvironmentException e) {
      logger.error(AN_ERROR_OCCURRED_WHILE_ANALYZING_THE_CONTRACTS, e.getMessage());
      activityService.push(
          new Activity()
              .setPlugin(pluginType)
              .setStatus(FAIL)
              .setType(SECURED_READ)
              .setCardSerialNumber(appSerialNumber));
      return new AnalyzeContractsOutputDto(Collections.emptyList(), 5);
    } catch (CardIOException e) {
      logger.error(AN_ERROR_OCCURRED_WHILE_ANALYZING_THE_CONTRACTS, e.getMessage(), e);
      activityService.push(
          new Activity()
              .setPlugin(pluginType)
              .setStatus(FAIL)
              .setType(SECURED_READ)
              .setCardSerialNumber(appSerialNumber));
      return new AnalyzeContractsOutputDto(Collections.emptyList(), 1);
    } catch (RuntimeException e) {
      logger.error(AN_ERROR_OCCURRED_WHILE_ANALYZING_THE_CONTRACTS, e.getMessage(), e);
      activityService.push(
          new Activity()
              .setPlugin(pluginType)
              .setStatus(FAIL)
              .setType(SECURED_READ)
              .setCardSerialNumber(appSerialNumber));
      return new AnalyzeContractsOutputDto(Collections.emptyList(), 2);
    } finally {
      CardResourceServiceProvider.getService().releaseCardResource(samResource);
    }
  }

  private AnalyzeContractsOutputDto analyzeStorageCardContracts(
      CardReader cardReader, StorageCard storageCard, AnalyzeContractsInputDto inputData) {

    String pluginType = inputData.getPluginType();
    String cardUID = HexUtil.toHex(storageCard.getUID());

    CardResource samResource =
        CardResourceServiceProvider.getService()
            .getCardResource(CardConfigurator.SAM_RESOURCE_PROFILE_NAME);
    try {
      Card card = cardRepository.readCard(cardReader, storageCard, samResource);
      // logger.info("{}", card); deactivate until LocalDate is properly processed by KeypleUtil
      List<ContractStructure> validContracts = findValidContracts(card);
      activityService.push(
          new Activity()
              .setPlugin(pluginType)
              .setStatus(SUCCESS)
              .setType(READ)
              .setCardSerialNumber(cardUID));
      return new AnalyzeContractsOutputDto(validContracts, 0);
    } catch (CardNotPersonalizedException e) {
      logger.error(AN_ERROR_OCCURRED_WHILE_ANALYZING_THE_CONTRACTS, e.getMessage());
      activityService.push(
          new Activity()
              .setPlugin(pluginType)
              .setStatus(FAIL)
              .setType(READ)
              .setCardSerialNumber(cardUID));
      return new AnalyzeContractsOutputDto(Collections.emptyList(), 4);
    } catch (ExpiredEnvironmentException e) {
      logger.error(AN_ERROR_OCCURRED_WHILE_ANALYZING_THE_CONTRACTS, e.getMessage());
      activityService.push(
          new Activity()
              .setPlugin(pluginType)
              .setStatus(FAIL)
              .setType(READ)
              .setCardSerialNumber(cardUID));
      return new AnalyzeContractsOutputDto(Collections.emptyList(), 5);
    } catch (CardIOException e) {
      logger.error(AN_ERROR_OCCURRED_WHILE_ANALYZING_THE_CONTRACTS, e.getMessage(), e);
      activityService.push(
          new Activity()
              .setPlugin(pluginType)
              .setStatus(FAIL)
              .setType(READ)
              .setCardSerialNumber(cardUID));
      return new AnalyzeContractsOutputDto(Collections.emptyList(), 1);
    } catch (RuntimeException e) {
      logger.error(AN_ERROR_OCCURRED_WHILE_ANALYZING_THE_CONTRACTS, e.getMessage(), e);
      activityService.push(
          new Activity()
              .setPlugin(pluginType)
              .setStatus(FAIL)
              .setType(READ)
              .setCardSerialNumber(cardUID));
      return new AnalyzeContractsOutputDto(Collections.emptyList(), 2);
    } finally {
      CardResourceServiceProvider.getService().releaseCardResource(samResource);
    }
  }

  WriteContractOutputDto writeContract(
      CardReader cardReader, SmartCard smartCard, WriteContractInputDto inputData) {
    if (smartCard instanceof CalypsoCard) {
      return writeCalypsoCardContract(cardReader, (CalypsoCard) smartCard, inputData);
    } else {
      return writeStorageCardContract(cardReader, (StorageCard) smartCard, inputData);
    }
  }

  private WriteContractOutputDto writeCalypsoCardContract(
      CardReader cardReader, CalypsoCard calypsoCard, WriteContractInputDto inputData) {

    String pluginType = inputData.getPluginType();
    String appSerialNumber = HexUtil.toHex(calypsoCard.getApplicationSerialNumber());

    if (!CardConstant.Companion.getALLOWED_FILE_STRUCTURES()
        .contains(calypsoCard.getApplicationSubtype())) {
      return new WriteContractOutputDto(3);
    }

    logger.info("Inserted card application serial number: {}", appSerialNumber);

    CardResource samResource =
        CardResourceServiceProvider.getService()
            .getCardResource(CardConfigurator.SAM_RESOURCE_PROFILE_NAME);
    try {
      Card card = cardRepository.readCard(cardReader, calypsoCard, samResource);
      if (card == null) {
        // If card has not been read previously, throw error
        return new WriteContractOutputDto(4);
      }
      // logger.info("{}", card); deactivate until LocalDate is properly processed by KeypleUtil
      insertNewContract(inputData.getContractTariff(), inputData.getTicketToLoad(), card);
      int statusCode = cardRepository.writeCard(cardReader, calypsoCard, samResource, card);
      activityService.push(
          new Activity()
              .setPlugin(pluginType)
              .setStatus(SUCCESS)
              .setType(RELOAD)
              .setCardSerialNumber(appSerialNumber)
              .setContractLoaded(
                  inputData.getContractTariff().toString().replace("_", " ")
                      + ((inputData.getTicketToLoad() != 0)
                          ? ": " + inputData.getTicketToLoad()
                          : "")));
      return new WriteContractOutputDto(statusCode);
    } catch (CardIOException e) {
      logger.error(AN_ERROR_OCCURRED_WHILE_WRITING_THE_CONTRACT, e.getMessage(), e);
      activityService.push(
          new Activity()
              .setPlugin(pluginType)
              .setStatus(FAIL)
              .setType(RELOAD)
              .setCardSerialNumber(appSerialNumber)
              .setContractLoaded(""));
      return new WriteContractOutputDto(1);
    } catch (RuntimeException e) {
      logger.error(AN_ERROR_OCCURRED_WHILE_WRITING_THE_CONTRACT, e.getMessage(), e);
      activityService.push(
          new Activity()
              .setPlugin(pluginType)
              .setStatus(FAIL)
              .setType(RELOAD)
              .setCardSerialNumber(appSerialNumber)
              .setContractLoaded(""));
      return new WriteContractOutputDto(2);
    } finally {
      CardResourceServiceProvider.getService().releaseCardResource(samResource);
    }
  }

  private WriteContractOutputDto writeStorageCardContract(
      CardReader cardReader, StorageCard storageCard, WriteContractInputDto inputData) {

    String pluginType = inputData.getPluginType();
    String cardUID = HexUtil.toHex(storageCard.getUID());

    CardResource samResource =
        CardResourceServiceProvider.getService()
            .getCardResource(CardConfigurator.SAM_RESOURCE_PROFILE_NAME);
    try {
      Card card = cardRepository.readCard(cardReader, storageCard, samResource);
      if (card == null) {
        // If card has not been read previously, throw error
        return new WriteContractOutputDto(4);
      }
      // logger.info("{}", card); deactivate until LocalDate is properly processed by KeypleUtil
      insertNewContract(inputData.getContractTariff(), inputData.getTicketToLoad(), card);
      int statusCode = cardRepository.writeCard(cardReader, storageCard, samResource, card);
      activityService.push(
          new Activity()
              .setPlugin(pluginType)
              .setStatus(SUCCESS)
              .setType(RELOAD)
              .setCardSerialNumber(cardUID)
              .setContractLoaded(
                  inputData.getContractTariff().toString().replace("_", " ")
                      + ((inputData.getTicketToLoad() != 0)
                          ? ": " + inputData.getTicketToLoad()
                          : "")));
      return new WriteContractOutputDto(statusCode);
    } catch (CardIOException e) {
      logger.error(AN_ERROR_OCCURRED_WHILE_WRITING_THE_CONTRACT, e.getMessage(), e);
      activityService.push(
          new Activity()
              .setPlugin(pluginType)
              .setStatus(FAIL)
              .setType(RELOAD)
              .setCardSerialNumber(cardUID)
              .setContractLoaded(""));
      return new WriteContractOutputDto(1);
    } catch (RuntimeException e) {
      logger.error(AN_ERROR_OCCURRED_WHILE_WRITING_THE_CONTRACT, e.getMessage(), e);
      activityService.push(
          new Activity()
              .setPlugin(pluginType)
              .setStatus(FAIL)
              .setType(RELOAD)
              .setCardSerialNumber(cardUID)
              .setContractLoaded(""));
      return new WriteContractOutputDto(2);
    } finally {
      CardResourceServiceProvider.getService().releaseCardResource(samResource);
    }
  }

  CardIssuanceOutputDto initCard(
      CardReader cardReader, SmartCard smartCard, CardIssuanceInputDto inputData) {
    if (smartCard instanceof CalypsoCard) {
      return initCalypsoCard(cardReader, (CalypsoCard) smartCard, inputData);
    } else {
      return initStorageCard(cardReader, (StorageCard) smartCard, inputData);
    }
  }

  private CardIssuanceOutputDto initCalypsoCard(
      CardReader cardReader, CalypsoCard calypsoCard, CardIssuanceInputDto inputData) {

    String pluginType = inputData.getPluginType();
    String appSerialNumber = HexUtil.toHex(calypsoCard.getApplicationSerialNumber());

    if (!CardConstant.Companion.getALLOWED_FILE_STRUCTURES()
        .contains(calypsoCard.getApplicationSubtype())) {
      return new CardIssuanceOutputDto(3);
    }

    CardResource samResource =
        CardResourceServiceProvider.getService()
            .getCardResource(CardConfigurator.SAM_RESOURCE_PROFILE_NAME);
    try {
      cardRepository.initCard(cardReader, calypsoCard, samResource);
      activityService.push(
          new Activity()
              .setPlugin(pluginType)
              .setStatus(SUCCESS)
              .setType(ISSUANCE)
              .setCardSerialNumber(appSerialNumber));
      return new CardIssuanceOutputDto(0);
    } catch (CardIOException e) {
      logger.error(AN_ERROR_OCCURRED_WHILE_INITIALIZING_THE_CARD, e.getMessage(), e);
      activityService.push(
          new Activity()
              .setPlugin(pluginType)
              .setStatus(FAIL)
              .setType(ISSUANCE)
              .setCardSerialNumber(appSerialNumber));
      return new CardIssuanceOutputDto(1);
    } catch (RuntimeException e) {
      logger.error(AN_ERROR_OCCURRED_WHILE_INITIALIZING_THE_CARD, e.getMessage(), e);
      activityService.push(
          new Activity()
              .setPlugin(pluginType)
              .setStatus(FAIL)
              .setType(ISSUANCE)
              .setCardSerialNumber(appSerialNumber));
      return new CardIssuanceOutputDto(2);
    } finally {
      CardResourceServiceProvider.getService().releaseCardResource(samResource);
    }
  }

  CardIssuanceOutputDto initStorageCard(
      CardReader cardReader, StorageCard storageCard, CardIssuanceInputDto inputData) {

    String pluginType = inputData.getPluginType();
    String cardUID = HexUtil.toHex(storageCard.getUID());

    CardResource samResource =
        CardResourceServiceProvider.getService()
            .getCardResource(CardConfigurator.SAM_RESOURCE_PROFILE_NAME);
    try {
      cardRepository.initCard(cardReader, storageCard, samResource);
      activityService.push(
          new Activity()
              .setPlugin(pluginType)
              .setStatus(SUCCESS)
              .setType(ISSUANCE)
              .setCardSerialNumber(cardUID));
      return new CardIssuanceOutputDto(0);
    } catch (CardIOException e) {
      logger.error(AN_ERROR_OCCURRED_WHILE_INITIALIZING_THE_CARD, e.getMessage(), e);
      activityService.push(
          new Activity()
              .setPlugin(pluginType)
              .setStatus(FAIL)
              .setType(ISSUANCE)
              .setCardSerialNumber(cardUID));
      return new CardIssuanceOutputDto(1);
    } catch (RuntimeException e) {
      logger.error(AN_ERROR_OCCURRED_WHILE_INITIALIZING_THE_CARD, e.getMessage(), e);
      activityService.push(
          new Activity()
              .setPlugin(pluginType)
              .setStatus(FAIL)
              .setType(ISSUANCE)
              .setCardSerialNumber(cardUID));
      return new CardIssuanceOutputDto(2);
    } finally {
      CardResourceServiceProvider.getService().releaseCardResource(samResource);
    }
  }

  SelectAppAndAnalyzeContractsOutputDto selectAppAndAnalyzeContracts(
      CardReader cardReader,
      SelectAppAndAnalyzeContractsInputDto inputData,
      Properties properties) {

    String pluginType = inputData.getPluginType();
    String processedCardSelectionScenarioJsonString =
        properties != null
            ? properties.getProperty(PROCESSED_CARD_SELECTION_SCENARIO_JSON_STRING)
            : null;

    // Select application
    CalypsoCard calypsoCard;
    try {
      if (processedCardSelectionScenarioJsonString != null) {
        calypsoCard =
            cardRepository.importProcessedCardSelectionScenario(
                processedCardSelectionScenarioJsonString);
      } else {
        calypsoCard = cardRepository.selectCard(cardReader);
      }
    } catch (CardIOException e) {
      logger.error(AN_ERROR_OCCURRED_WHILE_ANALYZING_THE_CONTRACTS, e.getMessage(), e);
      activityService.push(
          new Activity()
              .setPlugin(pluginType)
              .setStatus(FAIL)
              .setType(SECURED_READ)
              .setCardSerialNumber(""));
      return new SelectAppAndAnalyzeContractsOutputDto(
          "", Collections.emptyList(), 1, e.getMessage());
    } catch (RuntimeException e) {
      logger.error(AN_ERROR_OCCURRED_WHILE_ANALYZING_THE_CONTRACTS, e.getMessage(), e);
      activityService.push(
          new Activity()
              .setPlugin(pluginType)
              .setStatus(FAIL)
              .setType(SECURED_READ)
              .setCardSerialNumber(""));
      return new SelectAppAndAnalyzeContractsOutputDto(
          "", Collections.emptyList(), 2, e.getMessage());
    }

    // Analyze contracts
    AnalyzeContractsInputDto inputData2 = new AnalyzeContractsInputDto(pluginType);
    AnalyzeContractsOutputDto outputData2 = analyzeContracts(cardReader, calypsoCard, inputData2);

    // Build result
    String appSerialNumber = HexUtil.toHex(calypsoCard.getApplicationSerialNumber());

    List<SelectAppAndAnalyzeContractsOutputDto.ContractInfo> validContracts =
        outputData2.getValidContracts().stream()
            .map(
                contract -> {
                  String title;
                  String description;
                  boolean isValid;
                  switch (contract.getContractTariff()) {
                    case MULTI_TRIP:
                      title = "Multi trip";
                      description =
                          contract.getCounterValue() != null
                              ? contract.getCounterValue() + " trip(s) left"
                              : "No counter";
                      isValid =
                          contract.getCounterValue() != null && contract.getCounterValue() >= 1;
                      break;
                    case SEASON_PASS:
                      title = "Season pass";
                      description =
                          "From\n"
                              + contract.getContractSaleDate().getDate().format(dateTimeFormatter)
                              + "\nto\n"
                              + contract
                                  .getContractValidityEndDate()
                                  .getDate()
                                  .format(dateTimeFormatter);
                      LocalDate now = LocalDate.now();
                      isValid =
                          (contract.getContractSaleDate().getDate().isBefore(now)
                                  || contract.getContractSaleDate().getDate().isEqual(now))
                              && (contract.getContractValidityEndDate().getDate().isAfter(now)
                                  || contract.getContractValidityEndDate().getDate().isEqual(now));
                      break;
                    case EXPIRED:
                      title = "Season pass - Expired";
                      description =
                          "From\n"
                              + contract.getContractSaleDate().getDate().format(dateTimeFormatter)
                              + "\nto\n"
                              + contract
                                  .getContractValidityEndDate()
                                  .getDate()
                                  .format(dateTimeFormatter);
                      isValid = false;
                      break;
                    case FORBIDDEN:
                      title = "FORBIDDEN";
                      description = "";
                      isValid = false;
                      break;
                    case STORED_VALUE:
                      title = "STORED_VALUE";
                      description = "";
                      isValid = false;
                      break;
                    default:
                      title = "UNKNOWN";
                      description = "";
                      isValid = false;
                      break;
                  }
                  return new SelectAppAndAnalyzeContractsOutputDto.ContractInfo(
                      title, description, isValid);
                })
            .collect(Collectors.toList());

    int statusCode = outputData2.getStatusCode();

    String message;
    switch (statusCode) {
      case 0:
        message = "Success";
        break;
      case 1:
        message = "Card communication error";
        break;
      case 2:
        message = "Server error";
        break;
      case 3:
        message =
            "Invalid card\nFile structure "
                + HexUtil.toHex(calypsoCard.getApplicationSubtype())
                + "h not supported";
        break;
      case 4:
        message = "Environment error: wrong version number";
        break;
      case 5:
        message = "Environment error: end date expired";
        break;
      default:
        message = "";
    }
    return new SelectAppAndAnalyzeContractsOutputDto(
        appSerialNumber, validContracts, statusCode, message);
  }

  SelectAppAndLoadContractOutputDto selectAppAndLoadContract(
      CardReader cardReader, SelectAppAndLoadContractInputDto inputData, Properties properties) {

    String pluginType = inputData.getPluginType();
    String processedCardSelectionScenarioJsonString =
        properties != null
            ? properties.getProperty(PROCESSED_CARD_SELECTION_SCENARIO_JSON_STRING)
            : null;

    // Select application
    CalypsoCard calypsoCard;
    try {
      if (processedCardSelectionScenarioJsonString != null) {
        calypsoCard =
            cardRepository.importProcessedCardSelectionScenario(
                processedCardSelectionScenarioJsonString);
      } else {
        calypsoCard = cardRepository.selectCard(cardReader);
      }
    } catch (CardIOException e) {
      logger.error(AN_ERROR_OCCURRED_WHILE_WRITING_THE_CONTRACT, e.getMessage(), e);
      activityService.push(
          new Activity()
              .setPlugin(pluginType)
              .setStatus(FAIL)
              .setType(SECURED_READ)
              .setCardSerialNumber(""));
      return new SelectAppAndLoadContractOutputDto(1, e.getMessage());
    } catch (RuntimeException e) {
      logger.error(AN_ERROR_OCCURRED_WHILE_WRITING_THE_CONTRACT, e.getMessage(), e);
      activityService.push(
          new Activity()
              .setPlugin(pluginType)
              .setStatus(FAIL)
              .setType(SECURED_READ)
              .setCardSerialNumber(""));
      return new SelectAppAndLoadContractOutputDto(2, e.getMessage());
    }

    String selectedApplicationSerialNumber =
        HexUtil.toHex(calypsoCard.getApplicationSerialNumber());
    String expectedApplicationSerialNumber = inputData.getApplicationSerialNumber();
    if (!selectedApplicationSerialNumber.equals(expectedApplicationSerialNumber)) {
      // Ticket would have been bought for the Card read at step one.
      // To avoid swapping we check thant loading is done on the same card
      return new SelectAppAndLoadContractOutputDto(2, "Not the same card");
    }

    // Write contract
    WriteContractInputDto inputData2 =
        new WriteContractInputDto(
            inputData.getContractTariff(), inputData.getTicketToLoad(), pluginType);
    WriteContractOutputDto outputData2 = writeContract(cardReader, calypsoCard, inputData2);

    // Build result
    int statusCode = outputData2.getStatusCode();

    String message;
    switch (statusCode) {
      case 0:
        message = "Success";
        break;
      case 1:
        message = "Card communication error";
        break;
      case 2:
        message = "Server error";
        break;
      case 3:
        message =
            "Invalid card\nFile structure "
                + HexUtil.toHex(calypsoCard.getApplicationSubtype())
                + "h not supported";
        break;
      default:
        message = "";
    }
    return new SelectAppAndLoadContractOutputDto(statusCode, message);
  }

  SelectAppAndPersonalizeCardOutputDto selectAppAndPersonalizeCard(
      CardReader cardReader, SelectAppAndPersonalizeCardInputDto inputData, Properties properties) {

    String pluginType = inputData.getPluginType();
    String processedCardSelectionScenarioJsonString =
        properties != null
            ? properties.getProperty(PROCESSED_CARD_SELECTION_SCENARIO_JSON_STRING)
            : null;

    // Select application
    CalypsoCard calypsoCard;
    try {
      if (processedCardSelectionScenarioJsonString != null) {
        calypsoCard =
            cardRepository.importProcessedCardSelectionScenario(
                processedCardSelectionScenarioJsonString);
      } else {
        calypsoCard = cardRepository.selectCard(cardReader);
      }
    } catch (CardIOException e) {
      logger.error(AN_ERROR_OCCURRED_WHILE_INITIALIZING_THE_CARD, e.getMessage(), e);
      activityService.push(
          new Activity()
              .setPlugin(pluginType)
              .setStatus(FAIL)
              .setType(SECURED_READ)
              .setCardSerialNumber(""));
      return new SelectAppAndPersonalizeCardOutputDto(1, e.getMessage());
    } catch (RuntimeException e) {
      logger.error(AN_ERROR_OCCURRED_WHILE_INITIALIZING_THE_CARD, e.getMessage(), e);
      activityService.push(
          new Activity()
              .setPlugin(pluginType)
              .setStatus(FAIL)
              .setType(SECURED_READ)
              .setCardSerialNumber(""));
      return new SelectAppAndPersonalizeCardOutputDto(2, e.getMessage());
    }

    // Init card
    CardIssuanceInputDto inputData2 = new CardIssuanceInputDto(pluginType);
    CardIssuanceOutputDto outputData2 = initCard(cardReader, calypsoCard, inputData2);

    // Build result
    int statusCode = outputData2.getStatusCode();

    String message;
    switch (statusCode) {
      case 0:
        message = "Success";
        break;
      case 1:
        message = "Card communication error";
        break;
      case 2:
        message = "Server error";
        break;
      case 3:
        message =
            "Invalid card\nFile structure "
                + HexUtil.toHex(calypsoCard.getApplicationSubtype())
                + "h not supported";
        break;
      default:
        message = "";
    }
    return new SelectAppAndPersonalizeCardOutputDto(statusCode, message);
  }

  private List<ContractStructure> findValidContracts(Card card) {
    // Check environment
    EnvironmentHolderStructure environment = card.getEnvironment();
    if (environment.getEnvVersionNumber() != VersionNumber.CURRENT_VERSION) {
      logger.warn(VERSION_NUMBER_OF_CARD_IS_INVALID_REJECT_CARD);
      throw new CardNotPersonalizedException();
    }
    if (environment.getEnvEndDate().getDate().isBefore(LocalDate.now())) {
      logger.warn(ENV_END_DATE_OF_CARD_IS_INVALID_REJECT_CARD);
      throw new ExpiredEnvironmentException();
    }
    // Check last event
    EventStructure lastEvent = card.getEvent();
    if (lastEvent.getEventVersionNumber() != VersionNumber.CURRENT_VERSION
        && lastEvent.getEventVersionNumber() != VersionNumber.UNDEFINED) {
      logger.warn(VERSION_NUMBER_OF_CARD_IS_INVALID_REJECT_CARD1);
      return Collections.emptyList();
    }
    // Iterate through the contracts in the card session
    List<ContractStructure> contracts = card.getContracts();
    List<ContractStructure> validContracts = new ArrayList<>();
    int contractIndex = 1;
    for (ContractStructure contract : contracts) {
      logger.info(
          CONTRACT_AT_INDEX,
          contractIndex,
          contract.getContractTariff(),
          contract.getContractSaleDate().getValue());
      if (contract.getContractVersionNumber() == VersionNumber.UNDEFINED) {
        // If ContractVersionNumber is 0 ensure that the associated ContractPriority field value is
        // 0 and move on to the next contract.
        if (contract.getContractTariff() != PriorityCode.FORBIDDEN) {
          logger.warn(CONTRACT_TARIFF_IS_NOT_VALID_FOR_THIS_CONTRACT);
        }
      } else {
        // If ContractValidityEndDate points to a date in the past
        if (contract.getContractValidityEndDate().getDate().isBefore(LocalDate.now())) {
          // Update the associated ContractPriority field present in the persistent object to 31 and
          // set the change flag to true.
          contract.setContractTariff(PriorityCode.EXPIRED);
          // Update contract
          card.setContract(contractIndex - 1, contract);
        }
        validContracts.add(contract);
      }
      contractIndex++;
    }
    logger.info(CONTRACTS, Arrays.deepToString(validContracts.toArray()));
    return validContracts;
  }

  private void insertNewContract(PriorityCode contractTariff, Integer ticketToLoad, Card card) {

    if (contractTariff != PriorityCode.SEASON_PASS && contractTariff != PriorityCode.MULTI_TRIP) {
      throw new IllegalArgumentException(ONLY_SEASON_PASS_OR_MULTI_TRIP_TICKET_CAN_BE_LOADED);
    }

    EnvironmentHolderStructure environment = card.getEnvironment();
    List<ContractStructure> contracts = card.getContracts();
    EventStructure currentEvent = card.getEvent();
    ContractStructure newContract;
    int newContractNumber;

    int existingContractNumber = getContractNumber(contractTariff, contracts);
    if (existingContractNumber > 0) {
      // Reloading
      newContractNumber = existingContractNumber;
      ContractStructure currentContract = contracts.get(existingContractNumber - 1);
      // build new contract
      if (PriorityCode.MULTI_TRIP == contractTariff) {
        newContract =
            buildMultiTripContract(
                environment.getEnvEndDate(), currentContract.getCounterValue() + ticketToLoad);
      } else {
        newContract = buildSeasonContract();
      }
    } else {
      // Issuing
      newContractNumber = findAvailablePosition(contracts);
      if (newContractNumber == 0) {
        // no available position, reject card
        return;
      }
      // build new contract
      if (PriorityCode.MULTI_TRIP == contractTariff) {
        newContract = buildMultiTripContract(environment.getEnvEndDate(), ticketToLoad);
      } else {
        newContract = buildSeasonContract();
      }
    }
    switch (newContractNumber) {
      case 1:
        currentEvent.setContractPriority1(newContract.getContractTariff());
        break;
      case 2:
        currentEvent.setContractPriority2(newContract.getContractTariff());
        break;
      case 3:
        currentEvent.setContractPriority3(newContract.getContractTariff());
        break;
      case 4:
        currentEvent.setContractPriority4(newContract.getContractTariff());
        break;
      default:
        throw new IllegalStateException(UNEXPECTED_CONTRACT_NUMBER + newContractNumber);
    }
    // Update contract & Event
    card.setContract(newContractNumber - 1, newContract);
    card.setEvent(currentEvent);
  }

  private int getContractNumber(PriorityCode contractTariff, List<ContractStructure> contracts) {
    int contractCount = contracts.size();
    for (int i = 0; i < contractCount; i++) {
      if (contractTariff.equals(contracts.get(i).getContractTariff())) {
        return i + 1;
      }
    }
    return 0;
  }

  private ContractStructure buildMultiTripContract(DateCompact envEndDate, Integer counterValue) {
    DateCompact contractSaleDate = new DateCompact(LocalDate.now());
    ContractStructure contract =
        new ContractStructure(
            VersionNumber.CURRENT_VERSION,
            PriorityCode.MULTI_TRIP,
            contractSaleDate,
            envEndDate,
            null,
            null,
            null,
            null);
    contract.setCounterValue(counterValue);
    return contract;
  }

  private ContractStructure buildSeasonContract() {
    DateCompact contractSaleDate = new DateCompact(LocalDate.now());
    DateCompact contractValidityEndDate = new DateCompact(contractSaleDate.getValue() + 30);
    return new ContractStructure(
        VersionNumber.CURRENT_VERSION,
        PriorityCode.SEASON_PASS,
        contractSaleDate,
        contractValidityEndDate,
        null,
        null,
        null,
        null);
  }

  private int findAvailablePosition(List<ContractStructure> contracts) {
    int contractCount = contracts.size();
    for (int i = 0; i < contractCount; i++) {
      if (PriorityCode.FORBIDDEN == contracts.get(i).getContractTariff()) {
        return i + 1;
      }
    }
    for (int i = 0; i < contractCount; i++) {
      if (PriorityCode.EXPIRED == contracts.get(i).getContractTariff()) {
        return i + 1;
      }
    }
    return 0;
  }

  private static class CardNotPersonalizedException extends RuntimeException {
    CardNotPersonalizedException() {
      super(THE_CARD_IS_NOT_PERSONALIZED);
    }
  }

  private static class ExpiredEnvironmentException extends RuntimeException {
    ExpiredEnvironmentException() {
      super(THE_ENVIRONMENT_HAS_EXPIRED);
    }
  }
}
