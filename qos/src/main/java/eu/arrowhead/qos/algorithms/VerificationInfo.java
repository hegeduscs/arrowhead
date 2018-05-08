/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This work was supported by National Funds through FCT (Portuguese
 * Foundation for Science and Technology) and by the EU ECSEL JU
 * funding, within Arrowhead project, ref. ARTEMIS/0001/2012,
 * JU grant nr. 332987.
 * ISEP, Polytechnic Institute of Porto.
 */
package eu.arrowhead.qos.algorithms;

import eu.arrowhead.common.database.qos.ResourceReservation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Paulo
 */
public class VerificationInfo {

  private Map<String, String> providerDeviceCapabilities = new HashMap<>();
  private Map<String, String> consumerDeviceCapabilities = new HashMap<>();
  private List<ResourceReservation> providerDeviceQoSReservations = new ArrayList<>();
  private List<ResourceReservation> consumerDeviceQoSReservations = new ArrayList<>();
  private Map<String, String> requestedQoS = new HashMap<>();
  private Map<String, String> commands = new HashMap<>();

  public VerificationInfo() {
  }

  public VerificationInfo(Map<String, String> providerDeviceCapabilities, Map<String, String> consumerDeviceCapabilities, List<ResourceReservation> providerDeviceQoSReservations, List<ResourceReservation> consumerDeviceQoSReservations,
                          Map<String, String> requestedQoS, Map<String, String> commands) {
    this.providerDeviceCapabilities = providerDeviceCapabilities;
    this.consumerDeviceCapabilities = consumerDeviceCapabilities;
    this.providerDeviceQoSReservations = providerDeviceQoSReservations;
    this.consumerDeviceQoSReservations = consumerDeviceQoSReservations;
    this.requestedQoS = requestedQoS;
    this.commands = commands;
  }

  public Map<String, String> getProviderDeviceCapabilities() {
    return providerDeviceCapabilities;
  }

  public void setProviderDeviceCapabilities(Map<String, String> providerDeviceCapabilities) {
    this.providerDeviceCapabilities = providerDeviceCapabilities;
  }

  public Map<String, String> getConsumerDeviceCapabilities() {
    return consumerDeviceCapabilities;
  }

  public void setConsumerDeviceCapabilities(Map<String, String> consumerDeviceCapabilities) {
    this.consumerDeviceCapabilities = consumerDeviceCapabilities;
  }

  public List<ResourceReservation> getProviderDeviceQoSReservations() {
    return providerDeviceQoSReservations;
  }

  public void setProviderDeviceQoSReservations(List<ResourceReservation> providerDeviceQoSReservations) {
    this.providerDeviceQoSReservations = providerDeviceQoSReservations;
  }

  public List<ResourceReservation> getConsumerDeviceQoSReservations() {
    return consumerDeviceQoSReservations;
  }

  public void setConsumerDeviceQoSReservations(List<ResourceReservation> consumerDeviceQoSReservations) {
    this.consumerDeviceQoSReservations = consumerDeviceQoSReservations;
  }

  public Map<String, String> getRequestedQoS() {
    return requestedQoS;
  }

  public void setRequestedQoS(Map<String, String> requestedQoS) {
    this.requestedQoS = requestedQoS;
  }

  public Map<String, String> getCommands() {
    return commands;
  }

  public void setCommands(Map<String, String> commands) {
    this.commands = commands;
  }

}
