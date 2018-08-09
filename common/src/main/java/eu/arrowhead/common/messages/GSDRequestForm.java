/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.common.messages;

import eu.arrowhead.common.database.ArrowheadCloud;
import eu.arrowhead.common.database.ArrowheadService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class GSDRequestForm {

  @Valid
  @NotNull
  private ArrowheadService requestedService;
  private List<ArrowheadCloud> searchPerimeter = new ArrayList<>();
  private Map<String, Boolean> registryFlags = new HashMap<>();

  public GSDRequestForm() {
  }

  public GSDRequestForm(ArrowheadService requestedService, List<ArrowheadCloud> searchPerimeter, Map<String, Boolean> registryFlags) {
    this.requestedService = requestedService;
    this.searchPerimeter = searchPerimeter;
    this.registryFlags = registryFlags;
  }

  public ArrowheadService getRequestedService() {
    return requestedService;
  }

  public void setRequestedService(ArrowheadService requestedService) {
    this.requestedService = requestedService;
  }

  public List<ArrowheadCloud> getSearchPerimeter() {
    return searchPerimeter;
  }

  public void setSearchPerimeter(List<ArrowheadCloud> searchPerimeter) {
    this.searchPerimeter = searchPerimeter;
  }

  public Map<String, Boolean> getRegistryFlags() {
    return registryFlags;
  }

  public void setRegistryFlags(Map<String, Boolean> registryFlags) {
    this.registryFlags = registryFlags;
  }

}
