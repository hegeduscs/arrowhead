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
import eu.arrowhead.common.database.ArrowheadSystem;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class TokenGenerationRequest {

  @Valid
  @NotNull
  private ArrowheadSystem consumer;
  @Valid
  private ArrowheadCloud consumerCloud;
  @NotEmpty
  private List<@NotNull @Valid ArrowheadSystem> providers = new ArrayList<>();
  @Valid
  @NotNull
  private ArrowheadService service;
  private int duration;

  public TokenGenerationRequest() {
  }

  public TokenGenerationRequest(ArrowheadSystem consumer, ArrowheadCloud consumerCloud, List<ArrowheadSystem> providers, ArrowheadService service,
                                int duration) {
    this.consumer = consumer;
    this.consumerCloud = consumerCloud;
    this.providers = providers;
    this.service = service;
    this.duration = duration;
  }

  public ArrowheadSystem getConsumer() {
    return consumer;
  }

  public void setConsumer(ArrowheadSystem consumer) {
    this.consumer = consumer;
  }

  public ArrowheadCloud getConsumerCloud() {
    return consumerCloud;
  }

  public void setConsumerCloud(ArrowheadCloud consumerCloud) {
    this.consumerCloud = consumerCloud;
  }

  public List<ArrowheadSystem> getProviders() {
    return providers;
  }

  public void setProviders(List<ArrowheadSystem> providers) {
    this.providers = providers;
  }

  public ArrowheadService getService() {
    return service;
  }

  public void setService(ArrowheadService service) {
    this.service = service;
  }

  public int getDuration() {
    return duration;
  }

  public void setDuration(int duration) {
    this.duration = duration;
  }

}