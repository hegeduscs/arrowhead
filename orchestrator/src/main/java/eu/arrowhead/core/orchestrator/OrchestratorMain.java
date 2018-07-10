/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.core.orchestrator;

import eu.arrowhead.common.ArrowheadMain;
import eu.arrowhead.common.misc.CoreSystem;
import eu.arrowhead.common.misc.CoreSystemService;
import eu.arrowhead.common.misc.GetCoreSystemServicesTask;
import eu.arrowhead.common.misc.NeedsCoreSystemService;
import eu.arrowhead.core.orchestrator.support.OldOrchResource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


public class OrchestratorMain extends ArrowheadMain implements NeedsCoreSystemService {

  public static TimerTask getServicesTask;

  static boolean USE_GATEKEEPER = true;
  static String SR_BASE_URI;
  static String AUTH_CONTROL_URI;
  static String TOKEN_GEN_URI;
  static String GSD_SERVICE_URI;
  static String ICN_SERVICE_URI;

  private OrchestratorMain(String[] args) {
    Set<Class<?>> classes = new HashSet<>(Arrays.asList(OrchestratorResource.class, OldOrchResource.class));
    String[] packages = {"eu.arrowhead.common", "eu.arrowhead.core.orchestrator.api", "eu.arrowhead.core.orchestrator.filter"};
    init(CoreSystem.ORCHESTRATOR, args, classes, packages);
    listenForInput();
  }

  public static void main(String[] args) {
    new OrchestratorMain(args);
  }

  @Override
  protected void init(CoreSystem coreSystem, String[] args, Set<Class<?>> classes, String[] packages) {
    super.init(coreSystem, args, classes, packages);
    argLoop:
    for (String arg : args) {
      switch (arg) {
        case "-nogk":
          USE_GATEKEEPER = false;
          break argLoop;
      }
    }
    SR_BASE_URI = srBaseUri;

    List<String> serviceDefs = new ArrayList<>(
        Arrays.asList(CoreSystemService.AUTH_CONTROL_SERVICE.getServiceDef(), CoreSystemService.TOKEN_GEN_SERVICE.getServiceDef()));
    if (USE_GATEKEEPER) {
      serviceDefs.addAll(Arrays.asList(CoreSystemService.GSD_SERVICE.getServiceDef(), CoreSystemService.ICN_SERVICE.getServiceDef()));
    }
    getServicesTask = new GetCoreSystemServicesTask(this, serviceDefs);
    Timer timer = new Timer();
    timer.schedule(getServicesTask, 15L * 1000L, 10L * 60L * 1000L); //15 sec delay, 10 min period

    listenForInput();
  }

  //NOTE if a service def is changed, it needs to be modified here too!
  //TODO find a way to make the switch/case work without the hardcoded strings
  @Override
  public void getCoreSystemServiceUris(Map<String, String[]> uriMap) {
    for (Entry<String, String[]> entry : uriMap.entrySet()) {
      switch (entry.getKey()) {
        case "AuthorizationControl":
          AUTH_CONTROL_URI = entry.getValue()[0];
          break;
        case "TokenGeneration":
          TOKEN_GEN_URI = entry.getValue()[0];
          break;
        case "GlobalServiceDiscovery":
          GSD_SERVICE_URI = entry.getValue()[0];
          break;
        case "InterCloudNegotiations":
          ICN_SERVICE_URI = entry.getValue()[0];
          break;
        default:
          break;
      }
    }
    System.out.println("Core system URLs acquired/updated.");
  }

}
