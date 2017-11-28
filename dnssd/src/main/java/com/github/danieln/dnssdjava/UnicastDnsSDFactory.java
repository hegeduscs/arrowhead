/*
 * Copyright (c) 2011, Daniel Nilsson
 * Released under a simplified BSD license,
 * see README.txt for details.
 */
package com.github.danieln.dnssdjava;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.xbill.DNS.Name;
import org.xbill.DNS.TextParseException;

/**
 * Unicast {@link DnsSDFactory} implementation backed by dnsjava.
 *
 * @author Daniel Nilsson
 */
public class UnicastDnsSDFactory extends DnsSDFactory {

  UnicastDnsSDFactory() {
  }

  @NotNull
  @Override
  public DnsSDDomainEnumerator createDomainEnumerator(@NotNull Collection<String> computerDomains) {
    List<Name> domains = new ArrayList<>(computerDomains.size());
    for (String domain : computerDomains) {
      try {
        domains.add(Name.fromString(domain));
      } catch (TextParseException ex) {
        throw new IllegalArgumentException("Invalid domain name: " + domain, ex);
      }
    }
    return new UnicastDnsSDDomainEnumerator(domains);
  }

  @NotNull
  @Override
  public DnsSDBrowser createBrowser(@NotNull Collection<String> browserDomains) {
    List<Name> domains = new ArrayList<>(browserDomains.size());
    for (String domain : browserDomains) {
      try {
        domains.add(Name.fromString(domain));
      } catch (TextParseException ex) {
        throw new IllegalArgumentException("Invalid domain name: " + domain, ex);
      }
    }
    return new UnicastDnsSDBrowser(domains);
  }

  @NotNull
  @Override
  public DnsSDRegistrator createRegistrator(@NotNull String registeringDomain) throws DnsSDException {
    try {
      return new UnicastDnsSDRegistrator(Name.fromString(registeringDomain));
    } catch (UnknownHostException ex) {
      throw new DnsSDException("Failed to find DNS update server for domain: " + registeringDomain, ex);
    } catch (TextParseException ex) {
      throw new IllegalArgumentException("Invalid domain name: " + registeringDomain, ex);
    }
  }

  @NotNull
  @Override
  public DnsSDRegistrator createRegistrator(@NotNull String registeringDomain, InetSocketAddress resolverSocaddr) throws DnsSDException {
    try {
      return new UnicastDnsSDRegistrator(Name.fromString(registeringDomain), resolverSocaddr);
    } catch (UnknownHostException ex) {
      throw new DnsSDException("Failed to find DNS update server for domain: " + registeringDomain, ex);
    } catch (TextParseException ex) {
      throw new IllegalArgumentException("Invalid domain name: " + registeringDomain, ex);
    }
  }

}
