package eu.arrowhead.common.database.qos;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;

@Entity
@Table(name = "topology")
public class Topology {

  @Column(name = "id")
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private String id;

  @ElementCollection(fetch = FetchType.LAZY)
  @MapKeyColumn(name = "config_key")
  @Column(name = "config_value")
  @CollectionTable(name = "topology_config_map", joinColumns = @JoinColumn(name = "id"))
  private Map<String, String> configurations = new HashMap<>();

  @Column(name = "status")
  private String status;

  public Topology() {
  }

  public Topology(Map<String, String> configurations, String status) {
    this.configurations = configurations;
    this.status = status;
  }

  @XmlTransient
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Map<String, String> getConfigurations() {
    return configurations;
  }

  public void setConfigurations(Map<String, String> configurations) {
    this.configurations = configurations;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

}
