/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.core.authorization;

import eu.arrowhead.common.ArrowheadMain;
import eu.arrowhead.common.misc.CoreSystem;
import eu.arrowhead.common.misc.SecurityUtils;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AuthorizationMain extends ArrowheadMain {

  public static boolean enableAuthForCloud;

  static PrivateKey privateKey;

  private AuthorizationMain(String[] args) {
    KeyStore keyStore = SecurityUtils.loadKeyStore(getProps().getProperty("keystore"), getProps().getProperty("keystorepass"));
    privateKey = SecurityUtils.getPrivateKey(keyStore, getProps().getProperty("keystorepass"));
    //System.out.println("private key: " + Base64.getEncoder().encodeToString(privateKey.getEncoded()));

    Set<Class<?>> classes = new HashSet<>(Arrays.asList(AuthorizationResource.class, AuthorizationApi.class));
    String[] packages = {"eu.arrowhead.common", "eu.arrowhead.core.authorization.filter"};
    init(CoreSystem.AUTHORIZATION, args, classes, packages);

    enableAuthForCloud = getProps().getBooleanProperty("enable_auth_for_cloud", false);
    listenForInput();
  }

  public static void main(String[] args) {
    new AuthorizationMain(args);
  }

}
