/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.common.database;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * JPA entity class for storing intra-cloud (within the cloud) authorization rights in the database. The <i>consumer_system_id</i>,
 * <i>provider_system_id</i> and <i>arrowhead_service_id</i> columns must be unique together. <p> The table contains foreign keys to {@link
 * ArrowheadSystem} and {@link ArrowheadService}. A particular Consumer System/Provider System/Arrowhead Service trio is authorized if there is a
 * database entry for it in this table. The existence of the database entry means the given Consumer System is authorized to consume the given
 * Arrowhead Serice from the given Provider System inside the Local Cloud. The reverse of it is not authorized.
 *
 * @author Umlauf Zoltán
 */
@Entity
@Table(name = "intra_cloud_authorization", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"consumer_system_id", "provider_system_id", "arrowhead_service_id"})})
public class IntraCloudAuthorization {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  @Valid
  @NotNull
  @JoinColumn(name = "consumer_system_id")
  @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @OnDelete(action = OnDeleteAction.CASCADE)
  private ArrowheadSystem consumer;

  @Valid
  @NotNull
  @JoinColumn(name = "provider_system_id")
  @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @OnDelete(action = OnDeleteAction.CASCADE)
  private ArrowheadSystem provider;

  @Valid
  @NotNull
  @JoinColumn(name = "arrowhead_service_id")
  @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @OnDelete(action = OnDeleteAction.CASCADE)
  private ArrowheadService service;

  public IntraCloudAuthorization() {
  }

  public IntraCloudAuthorization(ArrowheadSystem consumer, ArrowheadSystem provider, ArrowheadService service) {
    this.consumer = consumer;
    this.provider = provider;
    this.service = service;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
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

  public void setProvider(ArrowheadSystem providers) {
    this.provider = providers;
  }

  public ArrowheadService getService() {
    return service;
  }

  public void setService(ArrowheadService service) {
    this.service = service;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IntraCloudAuthorization)) {
      return false;
    }

    IntraCloudAuthorization that = (IntraCloudAuthorization) o;

    if (!consumer.equals(that.consumer)) {
      return false;
    }
    if (!provider.equals(that.provider)) {
      return false;
    }
    return service.equals(that.service);
  }

  @Override
  public int hashCode() {
    int result = consumer.hashCode();
    result = 31 * result + provider.hashCode();
    result = 31 * result + service.hashCode();
    return result;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("IntraCloudAuthorization{");
    sb.append("consumer=").append(consumer);
    sb.append(", provider=").append(provider);
    sb.append(", service=").append(service);
    sb.append('}');
    return sb.toString();
  }
}
