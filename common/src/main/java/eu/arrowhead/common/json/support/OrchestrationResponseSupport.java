/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.common.json.support;

import eu.arrowhead.common.messages.OrchestrationForm;
import eu.arrowhead.common.messages.OrchestrationResponse;
import java.util.ArrayList;
import java.util.List;

public class OrchestrationResponseSupport {

  private List<OrchestrationFormSupport> response = new ArrayList<>();

  public OrchestrationResponseSupport() {
  }

  public OrchestrationResponseSupport(OrchestrationResponse orchResponse) {
    for (OrchestrationForm form : orchResponse.getResponse()) {
      OrchestrationFormSupport supportForm = new OrchestrationFormSupport(form);
      response.add(supportForm);
    }
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
