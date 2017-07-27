package eu.arrowhead.common.model.messages;

import eu.arrowhead.common.model.ArrowheadCloud;
import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ICNProposal {

  private ArrowheadService requestedService;
  private String authenticationInfo;
  private ArrowheadCloud requesterCloud;
  private ArrowheadSystem requesterSystem;
  private List<ArrowheadSystem> preferredProviders = new ArrayList<ArrowheadSystem>();
  private Map<String, Boolean> negotiationFlags = new HashMap<String, Boolean>();

  public ICNProposal() {
  }

  public ICNProposal(ArrowheadService requestedService, String authenticationInfo,
      ArrowheadCloud requesterCloud,
      ArrowheadSystem requesterSystem, List<ArrowheadSystem> preferredProviders,
      Map<String, Boolean> negotiationFlags) {
    this.requestedService = requestedService;
    this.authenticationInfo = authenticationInfo;
    this.requesterCloud = requesterCloud;
    this.requesterSystem = requesterSystem;
    this.preferredProviders = preferredProviders;
    this.negotiationFlags = negotiationFlags;
  }

  public ArrowheadService getRequestedService() {
    return requestedService;
  }

  public void setRequestedService(ArrowheadService requestedService) {
    this.requestedService = requestedService;
  }

  public String getAuthenticationInfo() {
    return authenticationInfo;
  }

  public void setAuthenticationInfo(String authenticationInfo) {
    this.authenticationInfo = authenticationInfo;
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

  public List<ArrowheadSystem> getPreferredProviders() {
    return preferredProviders;
  }

  public void setPreferredProviders(List<ArrowheadSystem> preferredProviders) {
    this.preferredProviders = preferredProviders;
  }

  public Map<String, Boolean> getNegotiationFlags() {
    return negotiationFlags;
  }

  public void setNegotiationFlags(Map<String, Boolean> negotiationFlags) {
    this.negotiationFlags = negotiationFlags;
  }


}
