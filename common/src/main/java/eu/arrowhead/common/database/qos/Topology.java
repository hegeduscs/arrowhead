package eu.arrowhead.common.database.qos;

import java.util.Map;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(name = "Topology")
@XmlRootElement
public class Topology {

  @Column(name = "id")
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private String id;

  @ElementCollection
  @LazyCollection(LazyCollectionOption.FALSE)
  private Map<String, String> configurations;

  @Column(name = "status")
  private String status;

  public Topology(NetworkDevice device) {
    super();
  }

  /**
   * get configuration
   *
   * @return returns map with configuration
   */
  public Map<String, String> getConfigurations() {
    return configurations;
  }

  /**
   * set configuration
   *
   * @param configurations map with configuration
   */
  public void setConfigurations(Map<String, String> configurations) {
    this.configurations = configurations;
  }

  /**
   * get ID
   *
   * @return returns ID
   */
  public String getId() {
    return id;
  }

  /**
   * set ID
   *
   * @param id ID
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * get status
   *
   * @return returns status
   */
  public String getStatus() {
    return status;
  }

  /**
   * set status
   *
   * @param status status
   */
  public void setStatus(String status) {
    this.status = status;
  }

}
