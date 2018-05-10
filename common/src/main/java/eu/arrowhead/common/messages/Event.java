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
import eu.arrowhead.common.exception.BadPayloadException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@JsonIgnoreProperties({"alwaysMandatoryFields"})
public class Event extends ArrowheadBase {

  private static final Set<String> alwaysMandatoryFields = new HashSet<>(Collections.singleton("type"));

  private String type;
  private String payload;
  private LocalDateTime timestamp;
  private Map<String, String> eventMetadata = new HashMap<>();

  public Event() {
  }

  public Event(String type, String payload, LocalDateTime timestamp, Map<String, String> eventMetadata) {
    this.type = type;
    this.payload = payload;
    this.timestamp = timestamp;
    this.eventMetadata = eventMetadata;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }

  public Map<String, String> getEventMetadata() {
    return eventMetadata;
  }

  public void setEventMetadata(Map<String, String> eventMetadata) {
    this.eventMetadata = eventMetadata;
  }

  public Set<String> missingFields(boolean throwException, Set<String> mandatoryFields) {
    Set<String> mf = new HashSet<>(alwaysMandatoryFields);
    if (mandatoryFields != null) {
      mf.addAll(mandatoryFields);
    }
    Set<String> nonNullFields = getFieldNamesWithNonNullValue();
    mf.removeAll(nonNullFields);

    if (throwException && !mf.isEmpty()) {
      throw new BadPayloadException("Missing mandatory fields for " + getClass().getSimpleName() + ": " + String.join(", ", mf));
    }
    return mf;
  }

}
