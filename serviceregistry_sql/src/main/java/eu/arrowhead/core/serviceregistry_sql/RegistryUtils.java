/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
      if (current.getProvider().getAddress().equals("localhost") || current.getProvider().getAddress().equals("127.0.0.1") || current.getProvider().getAddress().equals("0.0.0.0")) {
        iterator.remove();
      } else if (!pingHost(current.getProvider().getAddress(), current.getProvider().getPort(), ServiceRegistryMain.pingTimeout)) {
        iterator.remove();
      }
    }
  }

}
