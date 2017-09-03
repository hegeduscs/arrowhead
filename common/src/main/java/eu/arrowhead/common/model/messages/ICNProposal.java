package eu.arrowhead.common.model.messages;

import eu.arrowhead.common.model.ArrowheadCloud;
import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;
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

  public ICNProposal() {
  }

  public ICNProposal(ArrowheadService requestedService, ArrowheadCloud requesterCloud, ArrowheadSystem requesterSystem,
                     List<ArrowheadSystem> preferredSystems, Map<String, Boolean> negotiationFlags, String authenticationInfo) {
    this.requestedService = requestedService;
    this.requesterCloud = requesterCloud;
    this.requesterSystem = requesterSystem;
    this.preferredSystems = preferredSystems;
    this.negotiationFlags = negotiationFlags;
    this.authenticationInfo = authenticationInfo;
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

  public boolean isValid() {
    return requestedService != null && requesterCloud != null && requesterSystem != null && requestedService.isValid() && requesterCloud
        .isValidForDatabase() && requesterSystem.isValid();
  }

}
