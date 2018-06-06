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
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.exception.BadPayloadException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@JsonIgnoreProperties({"alwaysMandatoryFields"})
public class PublishEvent extends ArrowheadBase {

  private static final Set<String> alwaysMandatoryFields = new HashSet<>(Arrays.asList("source", "event"));

  private ArrowheadSystem source;
  private Event event;
  private String deliveryCompleteUri;

  public PublishEvent() {
  }

  public PublishEvent(ArrowheadSystem source, Event event, String deliveryCompleteUri) {
    this.source = source;
    this.event = event;
    this.deliveryCompleteUri = deliveryCompleteUri;
  }

  public ArrowheadSystem getSource() {
    return source;
  }

  public void setSource(ArrowheadSystem source) {
    this.source = source;
  }

  public Event getEvent() {
    return event;
  }

  public void setEvent(Event event) {
    this.event = event;
  }

  public String getDeliveryCompleteUri() {
    return deliveryCompleteUri;
  }

  public void setDeliveryCompleteUri(String deliveryCompleteUri) {
    this.deliveryCompleteUri = deliveryCompleteUri;
  }

  public Set<String> missingFields(boolean throwException, Set<String> mandatoryFields) {
    Set<String> mf = new HashSet<>(alwaysMandatoryFields);
    if (mandatoryFields != null) {
      mf.addAll(mandatoryFields);
    }
    Set<String> nonNullFields = getFieldNamesWithNonNullValue();
    mf.removeAll(nonNullFields);
    if (source != null) {
      mf = source.missingFields(false, mf);
    }
    if (event != null) {
      mf = event.missingFields(false, mf);
    }
    if (throwException && !mf.isEmpty()) {
      throw new BadPayloadException("Missing mandatory fields for " + getClass().getSimpleName() + ": " + String.join(", ", mf));
    }
    return mf;
  }

}
