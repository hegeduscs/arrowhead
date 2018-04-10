package eu.arrowhead.common.messages;

import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// This is just a utility class for the orchestration, it is not sent between core systems
public class TokenGenHelper {

  private ArrowheadService service;
  private List<ArrowheadSystem> providers = new ArrayList<>();

  public TokenGenHelper() {
  }

  public TokenGenHelper(ArrowheadService service, List<ArrowheadSystem> providers) {
    this.service = service;
    this.providers = providers;
  }

  public ArrowheadService getService() {
    return service;
  }

  public void setService(ArrowheadService service) {
    this.service = service;
  }

  public List<ArrowheadSystem> getProviders() {
    return providers;
  }

  public void setProviders(List<ArrowheadSystem> providers) {
    this.providers = providers;
  }

  public static List<TokenGenHelper> convertOfList(List<OrchestrationForm> ofList) {
    Set<ArrowheadService> uniqueServices = new HashSet<>();
    for (OrchestrationForm form : ofList) {
      Map<String, String> metadata = form.getService().getServiceMetadata();
      if ("token".equals(metadata.get("security"))) {
        uniqueServices.add(form.getService());
      }
    }

    List<TokenGenHelper> tokenGenHelper = new ArrayList<>();
    for (ArrowheadService service : uniqueServices) {
      TokenGenHelper tokenHelp = new TokenGenHelper(service, new ArrayList<>());
      for (OrchestrationForm form : ofList) {
        ArrowheadService formService = form.getService();
        if (formService.equals(service) && "token".equals(formService.getServiceMetadata().get("security"))) {
          tokenHelp.getProviders().add(form.getProvider());
        }
      }
      tokenGenHelper.add(tokenHelp);
    }

    return tokenGenHelper;
  }

  public static void updateFormsWithTokens(List<OrchestrationForm> ofList, List<TokenData> tokenData) {
    for (TokenData data : tokenData) {
      for (OrchestrationForm form : ofList) {
        if (data.getService().equals(form.getService()) && data.getSystem().equals(form.getProvider())) {
          form.setAuthorizationToken(data.getToken());
          form.setSignature(data.getSignature());
        }
      }
    }
  }

}
