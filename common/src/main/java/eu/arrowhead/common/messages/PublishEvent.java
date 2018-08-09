/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.common.messages;

import eu.arrowhead.common.database.ArrowheadSystem;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class PublishEvent {

  @Valid
  @NotNull
  private ArrowheadSystem source;
  @Valid
  @NotNull
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

}
