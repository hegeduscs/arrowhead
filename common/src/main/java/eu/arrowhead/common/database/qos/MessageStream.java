/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.common.database.qos;

import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;
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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "message_stream", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"arrowhead_service_id", "consumer_system_id", "provider_system_id"})})
public class MessageStream {

  @Column(name = "id")
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private int id;

  @JoinColumn(name = "arrowhead_service_id")
  @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE})
  private ArrowheadService service;

  @JoinColumn(name = "consumer_system_id")
  @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE})
  private ArrowheadSystem consumer;

  @JoinColumn(name = "provider_system_id")
  @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE})
  private ArrowheadSystem provider;

  @JoinColumn(name = "resource_reservation_id")
  @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE})
  private ResourceReservation qualityOfService;

  @ElementCollection(fetch = FetchType.LAZY)
  @MapKeyColumn(name = "config_key")
  @Column(name = "config_value")
  @CollectionTable(name = "message_stream_config_map", joinColumns = @JoinColumn(name = "id"))
  private Map<String, String> configuration = new HashMap<>();

  @Column(name = "type")
  private String type;

  public MessageStream() {
  }

  public MessageStream(ArrowheadService service, ArrowheadSystem consumer, ArrowheadSystem provider, ResourceReservation qualityOfService,
      Map<String, String> configuration, String type) {
    this.service = service;
    this.consumer = consumer;
    this.provider = provider;
    this.qualityOfService = qualityOfService;
    this.configuration = configuration;
    this.type = type;
  }

  public ArrowheadService getService() {
    return service;
  }

  public void setService(ArrowheadService service) {
    this.service = service;
  }

  public ArrowheadSystem getConsumer() {
    return consumer;
  }

  public void setConsumer(ArrowheadSystem consumer) {
    this.consumer = consumer;
  }

  public ArrowheadSystem getProvider() {
    return provider;
  }

  public void setProvider(ArrowheadSystem provider) {
    this.provider = provider;
  }

  public ResourceReservation getQualityOfService() {
    return qualityOfService;
  }

  public void setQualityOfService(ResourceReservation qualityOfService) {
    this.qualityOfService = qualityOfService;
  }

  public Map<String, String> getConfiguration() {
    return configuration;
  }

  public void setConfiguration(Map<String, String> configuration) {
    this.configuration = configuration;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  /**
   * This method generates the code that also identifies the message stream.
   *
   * @return Returns String with the code.
   */

  public String getCode() {
    return provider.getSystemName() + "," + consumer.getSystemName() + "," + service.getServiceDefinition();
  }

}
