package eu.arrowhead.common.database;

import eu.arrowhead.common.model.ArrowheadCloud;
import eu.arrowhead.common.model.ArrowheadService;
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

/**
 * @author umlaufz This class maps the inter cloud authorization rights. The table entry itself is the authorization right. The "consumer_cloud_id"
 * and "arrowhead_service_id" columns must be unique together.
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
