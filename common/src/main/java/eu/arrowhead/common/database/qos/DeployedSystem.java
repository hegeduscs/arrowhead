package eu.arrowhead.common.database.qos;

import eu.arrowhead.common.model.ArrowheadSystem;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlTransient;

@Entity
@Table(name = "deployed_system", uniqueConstraints = {@UniqueConstraint(columnNames = {"arrowhead_system_id"})})
public class DeployedSystem {

  @Column(name = "id")
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private int id;

  @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE})
  @JoinColumn(name = "arrowhead_system_id")
  private ArrowheadSystem system;

  @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE})
  @JoinColumn(name = "network_device_id")
  private NetworkDevice networkDevice;

  public DeployedSystem() {
  }

  public DeployedSystem(ArrowheadSystem system, NetworkDevice networkDevice) {
    this.system = system;
    this.networkDevice = networkDevice;
  }

  @XmlTransient
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public ArrowheadSystem getSystem() {
    return system;
  }

  public void setSystem(ArrowheadSystem system) {
    this.system = system;
  }

  public NetworkDevice getNetworkDevice() {
    return networkDevice;
  }

  public void setNetworkDevice(NetworkDevice networkDevice) {
    this.networkDevice = networkDevice;
  }

}
