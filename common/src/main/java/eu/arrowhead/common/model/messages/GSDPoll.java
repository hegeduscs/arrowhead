package eu.arrowhead.common.model.messages;


import eu.arrowhead.common.model.ArrowheadCloud;
import eu.arrowhead.common.model.ArrowheadService;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class GSDPoll {

  private ArrowheadService requestedService;
  private ArrowheadCloud requesterCloud;

  public GSDPoll() {
  }

  public GSDPoll(ArrowheadService requestedService,
      ArrowheadCloud requesterCloud) {
    this.requestedService = requestedService;
    this.requesterCloud = requesterCloud;
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


}
