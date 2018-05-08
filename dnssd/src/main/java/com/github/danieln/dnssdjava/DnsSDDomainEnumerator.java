/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */
package com.github.danieln.dnssdjava;

import java.util.Collection;

/**
 * A DnsSDDomainEnumerator object provides methods for finding out which domains to use for registering and browsing for services.
 *
 * @author Daniel Nilsson
 */
public interface DnsSDDomainEnumerator {

  /**
   * Get the list of domains recommended for browsing.
   *
   * @return a collection of domain names.
   */
  Collection<String> getBrowsingDomains();

  /**
   * Get the recommended default domain for browsing.
   *
   * @return a domain name.
   */
  String getDefaultBrowsingDomain();

  /**
   * Get the recommended default domain for registering services.
   *
   * @return a domain name.
   */
  String getDefaultRegisteringDomain();

  /**
   * Get the "support browsing" or "automatic browsing" domains.
   *
   * @return a collection of domain names.
   */
  Collection<String> getLegacyBrowsingDomains();

  /**
   * Get the list of domains recommended for registering services.
   *
   * @return a collection of domain names.
   */
  Collection<String> getRegisteringDomains();

}
