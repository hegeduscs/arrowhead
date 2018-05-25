/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

import eu.arrowhead.common.DatabaseManager;
import java.util.HashMap;
import org.apache.log4j.Logger;

final class DeviceRegistryService {

  private static final Logger log = Logger.getLogger(DeviceRegistryService.class.getName());
  private static final DatabaseManager dm = DatabaseManager.getInstance();
  private static final HashMap<String, Object> restrictionMap = new HashMap<>();

  //business logic here, behind the REST resource
}
