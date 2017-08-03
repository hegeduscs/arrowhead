package eu.arrowhead.common.database.qos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlTransient;

@Entity
@Table(name = "node", uniqueConstraints = {@UniqueConstraint(columnNames = {"device_model_code"})})
public class Node {

  @Column(name = "id")
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private int id;

  @Column(name = "device_model_code")
  private String device_model_code;

  @JoinColumn(name = "deployed_system_id")
  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
  private List<DeployedSystem> deployedSystems = new ArrayList<>();

  @ElementCollection(fetch = FetchType.LAZY)
  @MapKeyColumn(name = "capability_key")
  @Column(name = "capability_value")
  @CollectionTable(name = "node_processing_capabilities", joinColumns = @JoinColumn(name = "id"))
  private Map<String, String> processingCapabilities = new HashMap<>();

  public Node() {
  }

  public Node(String device_model_code, List<DeployedSystem> deployedSystems, Map<String, String> processingCapabilities) {
    this.device_model_code = device_model_code;
    this.deployedSystems = deployedSystems;
    this.processingCapabilities = processingCapabilities;
  }

  @XmlTransient
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getDevice_model_code() {
    return device_model_code;
  }

  public void setDevice_model_code(String device_model_code) {
    this.device_model_code = device_model_code;
  }

  public List<DeployedSystem> getDeployedSystems() {
    return deployedSystems;
  }

  public void setDeployedSystems(List<DeployedSystem> deployedSystems) {
    this.deployedSystems = deployedSystems;
  }

  public Map<String, String> getProcessingCapabilities() {
    return processingCapabilities;
  }

  public void setProcessingCapabilities(Map<String, String> processingCapabilities) {
    this.processingCapabilities = processingCapabilities;
  }

}
