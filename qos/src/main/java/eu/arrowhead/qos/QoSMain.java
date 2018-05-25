/*
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.qos;

import eu.arrowhead.common.ArrowheadMain;
import eu.arrowhead.common.misc.CoreSystem;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class QoSMain extends ArrowheadMain {

  static String MONITOR_URL;

  private QoSMain(String[] args) {
    Set<Class<?>> classes = new HashSet<>(Collections.singleton(QoSResource.class));
    String[] packages = {"eu.arrowhead.common"};
    init(CoreSystem.QOS, args, classes, packages);

    MONITOR_URL = props.getProperty("monitor_url");
    listenForInput();
  }

  public static void main(String[] args) {
    new QoSMain(args);
  }

}
