package eu.arrowhead.common.database;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlTransient;

/**
 * JPA entity class for storing intra-cloud (within the cloud) authorization rights in the database. The <i>consumer_system_id</i>,
 * <i>provider_system_id</i> and <i>arrowhead_service_id</i> columns must be unique together.
 * <p>
 * The table contains foreign keys to {@link ArrowheadSystem} and {@link ArrowheadService}. A
 * particular Consumer System/Provider System/Arrowhead Service trio is authorized if there is a database entry for it in this table. The existence of
 * the database entry means the given Consumer System is authorized to consume the given Arrowhead Serice from the given Provider System inside the
 * Local Cloud. The reverse of it is not authorized.
 *
 * @author Umlauf Zoltán
 */
@Entity
@Table(name = "intra_cloud_authorization", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"consumer_system_id", "provider_system_id", "arrowhead_service_id"})})
public class IntraCloudAuthorization {

  @Column(name = "id")
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private int id;

  @JoinColumn(name = "consumer_system_id")
  @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE})
  @NotNull
  private ArrowheadSystem consumer;

  @JoinColumn(name = "provider_system_id")
  @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE})
  @NotNull
  private ArrowheadSystem provider;

  @JoinColumn(name = "arrowhead_service_id")
  @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE})
  @NotNull
  private ArrowheadService service;

  public IntraCloudAuthorization() {
  }

  public IntraCloudAuthorization(ArrowheadSystem consumer, ArrowheadSystem provider, ArrowheadService service) {
    this.consumer = consumer;
    this.provider = provider;
    this.service = service;
  }

  @XmlTransient
  public int getId() {
    return id;
  }

  public void setId(int id) {
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

}
