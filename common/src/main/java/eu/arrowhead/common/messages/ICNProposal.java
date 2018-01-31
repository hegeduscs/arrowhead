/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.common.messages;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.arrowhead.common.database.ArrowheadCloud;
import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.database.Broker;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ICNProposal {

  private ArrowheadService requestedService;
  private ArrowheadCloud requesterCloud;
  private ArrowheadSystem requesterSystem;
  private List<ArrowheadSystem> preferredSystems = new ArrayList<>();
  private Map<String, Boolean> negotiationFlags = new HashMap<>();
  private String authenticationInfo;
  private List<Broker> preferredBrokers;
  private int timeout;
  private String gatewayPublicKey;

  public ICNProposal() {
  }

  public ICNProposal(ArrowheadService requestedService, ArrowheadCloud requesterCloud, ArrowheadSystem requesterSystem, List<ArrowheadSystem> preferredSystems, Map<String, Boolean> negotiationFlags, String authenticationInfo,
                     List<Broker> preferredBrokers, int timeout, String gatewayPublicKey) {
    this.requestedService = requestedService;
    this.requesterCloud = requesterCloud;
    this.requesterSystem = requesterSystem;
    this.preferredSystems = preferredSystems;
    this.negotiationFlags = negotiationFlags;
    this.authenticationInfo = authenticationInfo;
    this.preferredBrokers = preferredBrokers;
    this.timeout = timeout;
    this.gatewayPublicKey = gatewayPublicKey;
  }

  public ArrowheadService getRequestedService() {
    return requestedService;
  }

  public void setRequestedService(ArrowheadService requestedService) {
    this.requestedService = requestedService;
  }

  public ArrowheadCloud getRequesterCloud() {
    return requesterCloud;
  }

  public void setRequesterCloud(ArrowheadCloud requesterCloud) {
    this.requesterCloud = requesterCloud;
  }

  public ArrowheadSystem getRequesterSystem() {
    return requesterSystem;
  }

  public void setRequesterSystem(ArrowheadSystem requesterSystem) {
    this.requesterSystem = requesterSystem;
  }

  public List<ArrowheadSystem> getPreferredSystems() {
    return preferredSystems;
  }

  public void setPreferredSystems(List<ArrowheadSystem> preferredSystems) {
    this.preferredSystems = preferredSystems;
  }

  public Map<String, Boolean> getNegotiationFlags() {
    return negotiationFlags;
  }

  public void setNegotiationFlags(Map<String, Boolean> negotiationFlags) {
    this.negotiationFlags = negotiationFlags;
  }

  public String getAuthenticationInfo() {
    return authenticationInfo;
  }

  public void setAuthenticationInfo(String authenticationInfo) {
    this.authenticationInfo = authenticationInfo;
  }

  public List<Broker> getPreferredBrokers() {
    return preferredBrokers;
  }

  public void setPreferredBrokers(List<Broker> preferredBrokers) {
    this.preferredBrokers = preferredBrokers;
  }

  public int getTimeout() {
    return timeout;
  }

  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }

  public String getGatewayPublicKey() {
    return gatewayPublicKey;
  }

  public void setGatewayPublicKey(String gatewayPublicKey) {
    this.gatewayPublicKey = gatewayPublicKey;
  }

  @JsonIgnore
  public boolean isValid() {
    return requestedService != null && requesterCloud != null && requesterSystem != null && requestedService.isValid() && requesterCloud.isValidForDatabase() && requesterSystem.isValid();
  }

}
