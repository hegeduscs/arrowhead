/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.common.database;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(name = "event")
public class Event {

  @Column(name = "id")
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private int id;

  @Column(name = "type")
  private String type;

  @JoinColumn(name = "source_system_id")
  @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE})
  private ArrowheadSystem source;

  @ElementCollection(fetch = FetchType.LAZY)
  @LazyCollection(LazyCollectionOption.FALSE)
  @MapKeyColumn(name = "metadata_key")
  @Column(name = "metadata_value", length = 2047)
  @CollectionTable(name = "event_metadata", joinColumns = @JoinColumn(name = "event_id"))
  private Map<String, String> eventMetadata = new HashMap<>();

  @Column(name = "payload", length = 2047)
  private String payload;

  public Event() {
  }

  public Event(String type, ArrowheadSystem source, Map<String, String> eventMetadata, String payload) {
    this.type = type;
    this.source = source;
    this.eventMetadata = eventMetadata;
    this.payload = payload;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public ArrowheadSystem getSource() {
    return source;
  }

  public void setSource(ArrowheadSystem source) {
    this.source = source;
  }

  public Map<String, String> getEventMetadata() {
    return eventMetadata;
  }

  public void setEventMetadata(Map<String, String> eventMetadata) {
    this.eventMetadata = eventMetadata;
  }

  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Event event = (Event) o;

    if (!type.equals(event.type)) {
      return false;
    }
    if (!source.equals(event.source)) {
      return false;
    }
    return payload != null ? payload.equals(event.payload) : event.payload == null;
  }

  @Override
  public int hashCode() {
    int result = type.hashCode();
    result = 31 * result + source.hashCode();
    result = 31 * result + (payload != null ? payload.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer("Event{");
    sb.append("type='").append(type).append('\'');
    sb.append(", source=").append(source);
    sb.append(", payload='").append(payload).append('\'');
    sb.append('}');
    return sb.toString();
  }

}
