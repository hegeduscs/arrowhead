package eu.arrowhead.common.database.qos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "node", uniqueConstraints = {@UniqueConstraint(columnNames = {"device_model_code"})})
@XmlRootElement
public class Node {

  @Column(name = "id")
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private int id;

  @Column(name = "device_model_code")
  private String device_model_code;

  /*
   * @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL) private
   * Map<ArrowheadSystem, Network_Device> deployedSystems;
   */
  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  private List<DeployedSystem> deployedSystems;

  @ElementCollection
  private Map<String, String> processingCapabilities;

  Node() {
    deployedSystems = new ArrayList<>();
    processingCapabilities = new HashMap<>();
  }

  public Node(String device_model_code, List<DeployedSystem> deployedSystems, Map<String, String> processingCapabilities) {
    this.device_model_code = device_model_code;
    this.deployedSystems = deployedSystems;
    this.processingCapabilities = processingCapabilities;
  }

  /**
   * get device model code
   *
   * @return returns device model code
   */
  public String getDevice_model_code() {
    return device_model_code;
  }

  /**
   * set device model code
   *
   * @param device_model_code device model code
   */
  public void setDevice_model_code(String device_model_code) {
    this.device_model_code = device_model_code;
  }

  /**
   * get ID
   *
   * @return returns ID
   */
  public int getId() {
    return id;
  }

  /**
   * set iD
   *
   * @param id ID
   */
  public void setId(int id) {
    this.id = id;
  }

  /**
   * get deployed systems
   *
   * @return returns list of deployed systems
   */
  public List<DeployedSystem> getDeployedSystems() {
    return deployedSystems;
  }

  /**
   * set deployed systems
   */
  public void setDeployedSystems(List<DeployedSystem> deployedSystems) {
    this.deployedSystems = deployedSystems;
  }

  /**
   * get processing capabilities
   *
   * @return returns map with all the processing capabilities
   */
  public Map<String, String> getProcessingCapabilities() {
    return processingCapabilities;
  }

  /**
   * set processing caacilities.
   */
  public void setProcessingCapabilities(Map<String, String> processingCapabilities) {
    this.processingCapabilities = processingCapabilities;
  }

}
