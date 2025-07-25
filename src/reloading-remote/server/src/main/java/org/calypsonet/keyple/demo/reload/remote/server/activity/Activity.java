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
package org.calypsonet.keyple.demo.reload.remote.server.activity;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.UUID;

/** Transaction log object for the dashboard view */
public class Activity {

  String id; // id of the transaction
  String cardSerialNumber; // Calypso Card Serial Number
  String plugin; // plugin name
  String startedAt; // when the transaction started
  String type; // type of transaction
  String status; // SUCCESS or FAIL
  String contractLoaded; // (opt) description of the contract loaded

  public Activity() {
    this.id = UUID.randomUUID().toString().substring(0, 4);
    this.startedAt =
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
            .withZone(ZoneId.systemDefault())
            .format(Instant.now());
  }

  public String getType() {
    return type;
  }

  public String getStatus() {
    return status;
  }

  public String getId() {
    return id;
  }

  public String getCardSerialNumber() {
    return cardSerialNumber;
  }

  public String getPlugin() {
    return plugin;
  }

  public String getStartedAt() {
    return startedAt;
  }

  public String getContractLoaded() {
    return contractLoaded;
  }

  public Activity setCardSerialNumber(String cardSerialNumber) {
    this.cardSerialNumber = cardSerialNumber;
    return this;
  }

  public Activity setPlugin(String plugin) {
    this.plugin = plugin;
    return this;
  }

  public Activity setType(String type) {
    this.type = type;
    return this;
  }

  public Activity setStatus(String status) {
    this.status = status;
    return this;
  }

  public Activity setContractLoaded(String contractLoaded) {
    this.contractLoaded = contractLoaded;
    return this;
  }
}
