/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.core.serviceregistry_sql;

import eu.arrowhead.common.database.ServiceRegistryEntry;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class RegistryUtils {

  static boolean pingHost(String host, int port, int timeout) {
    try (Socket socket = new Socket()) {
      socket.connect(new InetSocketAddress(host, port), timeout);
      return true;
    } catch (Exception e) {
      return false; // Either timeout or unreachable or failed DNS lookup.
    }
  }

  static void filterOnVersion(List<ServiceRegistryEntry> fetchedList, int targetVersion) {
    fetchedList.removeIf(current -> current.getVersion() != targetVersion);
  }

  static void filterOnMeta(List<ServiceRegistryEntry> fetchedList, Map<String, String> metadata) {
    fetchedList.removeIf(current -> !metadata.equals(current.getProvidedService().getServiceMetadata()));
  }

  static void filterOnPing(List<ServiceRegistryEntry> fetchedList) {
    Iterator<ServiceRegistryEntry> iterator = fetchedList.iterator();
    while (iterator.hasNext()) {
      ServiceRegistryEntry current = iterator.next();
      if (current.getProvider().getAddress().equals("0.0.0.0")) {
        iterator.remove();
      } else if (!pingHost(current.getProvider().getAddress(), current.getProvider().getPort(), ServiceRegistryMain.PING_TIMEOUT)) {
        iterator.remove();
      }
    }
  }

}
