package eu.arrowhead.common.json.supportadapter;

import eu.arrowhead.common.messages.OrchestrationForm;
import eu.arrowhead.common.messages.OrchestrationResponse;
import java.util.ArrayList;
import java.util.List;

public class OrchestrationResponseSupport {

  private List<OrchestrationFormSupport> response = new ArrayList<>();

  public OrchestrationResponseSupport() {
  }

  public OrchestrationResponseSupport(OrchestrationResponse orchResponse) {
    List<OrchestrationFormSupport> response = new ArrayList<>();
    for (OrchestrationForm form : orchResponse.getResponse()) {
      ArrowheadServiceSupport supportService = new ArrowheadServiceSupport(form.getService());
      OrchestrationFormSupport supportForm = new OrchestrationFormSupport(supportService, form.getProvider(), form.getServiceURI(),
                                                                          form.getInstruction(), form.getAuthorizationToken(), form.getSignature());
      response.add(supportForm);
    }
    this.response = response;
  }

  public OrchestrationResponseSupport(List<OrchestrationFormSupport> response) {
    this.response = response;
  }

  public List<OrchestrationFormSupport> getResponse() {
    return response;
  }

  public void setResponse(List<OrchestrationFormSupport> response) {
    this.response = response;
  }

}
