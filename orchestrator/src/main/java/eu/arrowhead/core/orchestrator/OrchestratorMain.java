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
import eu.arrowhead.common.Utility;
import eu.arrowhead.common.misc.CoreSystem;
import eu.arrowhead.common.misc.CoreSystemService;
import eu.arrowhead.core.orchestrator.support.OldOrchResource;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class OrchestratorMain extends ArrowheadMain {

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
    getCoreSystemServiceUris();
    listenForInput();
  }

  public static void getCoreSystemServiceUris() {
    AUTH_CONTROL_URI = Utility.getServiceInfo(CoreSystemService.AUTH_CONTROL_SERVICE.getServiceDef())[0];
    TOKEN_GEN_URI = Utility.getServiceInfo(CoreSystemService.TOKEN_GEN_SERVICE.getServiceDef())[0];
    if (USE_GATEKEEPER) {
      GSD_SERVICE_URI = Utility.getServiceInfo(CoreSystemService.GSD_SERVICE.getServiceDef())[0];
      ICN_SERVICE_URI = Utility.getServiceInfo(CoreSystemService.ICN_SERVICE.getServiceDef())[0];
    }
    System.out.println("Core system URLs acquired.");
  }

}
