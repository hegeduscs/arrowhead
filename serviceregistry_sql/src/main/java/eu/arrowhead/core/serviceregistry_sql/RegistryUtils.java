package eu.arrowhead.core.serviceregistry_sql;

import eu.arrowhead.common.database.ServiceRegistryEntry;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

class RegistryUtils {

  static boolean pingHost(@NotNull String host, int port, int timeout) {
    try (Socket socket = new Socket()) {
      socket.connect(new InetSocketAddress(host, port), timeout);
      return true;
    } catch (Exception e) {
      return false; // Either timeout or unreachable or failed DNS lookup.
    }
  }

  static void filterOnVersion(@NotNull List<ServiceRegistryEntry> fetchedList, int targetVersion) {
    fetchedList.removeIf(current -> current.getVersion() != targetVersion);
  }

  static void filterOnMeta(@NotNull List<ServiceRegistryEntry> fetchedList, @NotNull Map<String, String> metadata) {
    fetchedList.removeIf(current -> !metadata.equals(current.getProvidedService().getServiceMetadata()));
  }

  static void filterOnPing(@NotNull List<ServiceRegistryEntry> fetchedList) {
    Iterator<ServiceRegistryEntry> iterator = fetchedList.iterator();
    while (iterator.hasNext()) {
      ServiceRegistryEntry current = iterator.next();
      if (current.getProvider().getAddress().equals("localhost") || current.getProvider().getAddress().equals("127.0.0.1") || current.getProvider()
          .getAddress().equals("0.0.0.0")) {
        iterator.remove();
      } else if (!pingHost(current.getProvider().getAddress(), current.getProvider().getPort(), ServiceRegistryMain.pingTimeout)) {
        iterator.remove();
      }
    }
  }

}
