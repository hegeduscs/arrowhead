/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.core.gateway;

import eu.arrowhead.common.ArrowheadMain;
import eu.arrowhead.common.misc.CoreSystem;
import eu.arrowhead.common.misc.SecurityUtils;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.net.ssl.SSLContext;

public class GatewayMain extends ArrowheadMain {

  static SSLContext clientContext;

  private GatewayMain(String[] args) {
    Set<Class<?>> classes = new HashSet<>(Collections.singleton(GatewayResource.class));
    String[] packages = {"eu.arrowhead.common", "eu.arrowhead.core.gateway.filter"};
    init(CoreSystem.GATEWAY, args, classes, packages);
    listenForInput();
  }

  public static void main(String[] args) {
    new GatewayMain(args);
  }

  @Override
  protected void startSecureServer(Set<Class<?>> classes, String[] packages) {
    String truststorePath = getProps().getProperty("truststore");
    String truststorePass = getProps().getProperty("truststorepass");
    String trustPass = getProps().getProperty("trustpass");
    String masterArrowheadCertPath = getProps().getProperty("master_arrowhead_cert");

    clientContext = SecurityUtils.createMasterSSLContext(truststorePath, truststorePass, trustPass, masterArrowheadCertPath);
    super.startSecureServer(classes, packages);
  }

}
