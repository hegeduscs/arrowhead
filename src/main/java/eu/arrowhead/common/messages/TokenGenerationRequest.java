/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.common.messages;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import eu.arrowhead.common.database.ArrowheadCloud;
import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.exception.BadPayloadException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JsonIgnoreProperties({"alwaysMandatoryFields"})
public class TokenGenerationRequest extends ArrowheadBase {

  private static final Set<String> alwaysMandatoryFields = new HashSet<>(Arrays.asList("service", "consumer", "providers"));

  private ArrowheadSystem consumer;
  private ArrowheadCloud consumerCloud;
  private List<ArrowheadSystem> providers = new ArrayList<>();
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

  public Set<String> missingFields(boolean throwException, Set<String> mandatoryFields) {
    Set<String> mf = new HashSet<>(alwaysMandatoryFields);
    if (mandatoryFields != null) {
      mf.addAll(mandatoryFields);
    }
    Set<String> nonNullFields = getFieldNamesWithNonNullValue();
    mf.removeAll(nonNullFields);

    if (consumer != null) {
      mf = consumer.missingFields(false, mf);
    }
    if (service != null) {
      mf = service.missingFields(false, false, mf);
    }
    for (ArrowheadSystem provider : providers) {
      Set<String> fields = provider.missingFields(false, null);
      if (!fields.isEmpty()) {
        mf.add("Provider is missing mandatory field(s): " + String.join(", ", fields));
      }
    }

    if (throwException && !mf.isEmpty()) {
      throw new BadPayloadException("Missing mandatory fields for " + getClass().getSimpleName() + ": " + String.join(", ", mf));
    }
    return mf;
  }

}