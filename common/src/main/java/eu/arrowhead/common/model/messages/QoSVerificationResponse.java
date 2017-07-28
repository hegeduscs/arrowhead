package eu.arrowhead.common.model.messages;

import eu.arrowhead.common.model.ArrowheadSystem;
import eu.arrowhead.common.model.messages.QoSVerifierResponse.RejectMotivationTypes;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class QoSVerificationResponse {

  private Map<ArrowheadSystem, Boolean> response = new HashMap<>();
  private Map<ArrowheadSystem, QoSVerifierResponse.RejectMotivationTypes> rejectMotivation = new HashMap<>();

  public QoSVerificationResponse() {
    super();
  }

  public QoSVerificationResponse(Map<ArrowheadSystem, Boolean> response,
                                 Map<ArrowheadSystem, RejectMotivationTypes> rejectMotivation) {
    super();
    this.response = response;
    this.rejectMotivation = rejectMotivation;
  }

  public Map<ArrowheadSystem, Boolean> getResponse() {
    return response;
  }

  public void setResponse(Map<ArrowheadSystem, Boolean> response) {
    this.response = response;
  }

  public Map<ArrowheadSystem, QoSVerifierResponse.RejectMotivationTypes> getRejectMotivation() {
    return rejectMotivation;
  }

  public void setRejectMotivation(
      Map<ArrowheadSystem, QoSVerifierResponse.RejectMotivationTypes> rejectMotivation) {
    this.rejectMotivation = rejectMotivation;
  }

  public void addResponse(ArrowheadSystem system, Boolean resp) {
    response.put(system, resp);
  }

  public void addRejectMotivation(ArrowheadSystem system,
                                  QoSVerifierResponse.RejectMotivationTypes rejectType) {
    rejectMotivation.put(system, rejectType);
  }

}
