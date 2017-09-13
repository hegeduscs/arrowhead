package eu.arrowhead.core.serviceregistry_sql;

import eu.arrowhead.common.database.ServiceMetadata;
import eu.arrowhead.common.database.ServiceRegistryEntry;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;

class RegistryUtils {

  static boolean pingHost(String host, int port, int timeout) {
    try (Socket socket = new Socket()) {
      socket.connect(new InetSocketAddress(host, port), timeout);
      return true;
    } catch (IOException e) {
      return false; // Either timeout or unreachable or failed DNS lookup.
    }
  }

  static void filterOnVersion(List<ServiceRegistryEntry> fetchedList, int targetVersion) {
    fetchedList.removeIf(current -> current.getVersion() != targetVersion);
  }

  static void filterOnMeta(List<ServiceRegistryEntry> fetchedList, List<ServiceMetadata> metadata) {
    Iterator<ServiceRegistryEntry> iterator = fetchedList.iterator();
    while (iterator.hasNext()) {
      ServiceRegistryEntry current = iterator.next();
      boolean allMatch = true;
      for (ServiceMetadata currentMeta : current.getProvidedService().getServiceMetadata()) {
        if (!metadata.contains(currentMeta)) {
          allMatch = false;
        }
      }
      if (!allMatch) {
        iterator.remove();
      }
    }
  }

  static void filterOnPing(List<ServiceRegistryEntry> fetchedList) {
    Iterator<ServiceRegistryEntry> iterator = fetchedList.iterator();
    while (iterator.hasNext()) {
      ServiceRegistryEntry current = iterator.next();
      if (current.getProvider().getAddress().equals("localhost") || current.getProvider().getAddress().equals("127.0.0.1")) {
        iterator.remove();
      } else if (!pingHost(current.getProvider().getAddress(), current.getProvider().getPort(), ServiceRegistryMain.pingTimeout)) {
        iterator.remove();
      }
    }
  }

}
