package eu.arrowhead.common.messages;

import eu.arrowhead.common.database.ArrowheadCloud;
import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ICNRequestForm {

  private ArrowheadService requestedService;
  private ArrowheadCloud targetCloud;
  private ArrowheadSystem requesterSystem;
  private List<ArrowheadSystem> preferredSystems = new ArrayList<>();
  private Map<String, Boolean> negotiationFlags = new HashMap<>();
  private String authenticationInfo;

  public ICNRequestForm() {
  }

  public ICNRequestForm(ArrowheadService requestedService, ArrowheadCloud targetCloud, ArrowheadSystem requesterSystem,
                        List<ArrowheadSystem> preferredSystems, Map<String, Boolean> negotiationFlags, String authenticationInfo) {
    this.requestedService = requestedService;
    this.targetCloud = targetCloud;
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

  public ArrowheadCloud getTargetCloud() {
    return targetCloud;
  }

  public void setTargetCloud(ArrowheadCloud targetCloud) {
    this.targetCloud = targetCloud;
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
    return requestedService != null && targetCloud != null && requesterSystem != null && requestedService.isValid() && targetCloud.isValid()
        && requesterSystem.isValid();
  }

}
