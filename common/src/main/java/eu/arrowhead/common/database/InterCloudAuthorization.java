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
import javax.xml.bind.annotation.XmlTransient;

/**
 * JPA entity class for storing inter-cloud authorization rights in the database. The <i>consumer_cloud_id</i> and <i>arrowhead_service_id</i> columns
 * must be unique together.
 * <p>
 * The table contains foreign keys to {@link ArrowheadCloud} and {@link ArrowheadService}. A particular <tt>ArrowheadCloud</tt> -
 * <tt>ArrowheadService</tt> pair is authorized if there is a database entry for it in this table. The existence of the database entry means the given
 * cloud can consume the given service from an {@link ArrowheadSystem} inside the Local Cloud.
 *
 * @author Umlauf Zoltán
 */
@Entity
@Table(name = "inter_cloud_authorization", uniqueConstraints = {@UniqueConstraint(columnNames = {"consumer_cloud_id", "arrowhead_service_id"})})
public class InterCloudAuthorization {

  @Column(name = "id")
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private int id;

  @JoinColumn(name = "consumer_cloud_id")
  @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE})

  private ArrowheadCloud cloud;

  @JoinColumn(name = "arrowhead_service_id")
  @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE})

  private ArrowheadService service;

  public InterCloudAuthorization() {
  }

  public InterCloudAuthorization(ArrowheadCloud cloud, ArrowheadService service) {
    this.cloud = cloud;
    this.service = service;
  }

  @XmlTransient
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public ArrowheadCloud getCloud() {
    return cloud;
  }

  public void setCloud(ArrowheadCloud cloud) {
    this.cloud = cloud;
  }

  public ArrowheadService getService() {
    return service;
  }

  public void setService(ArrowheadService service) {
    this.service = service;
  }

}
