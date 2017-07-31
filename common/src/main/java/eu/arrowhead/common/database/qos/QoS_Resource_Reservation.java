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
@Table(name = "QOS_RESOURCE_RESERVATION")
@XmlRootElement
public class QoS_Resource_Reservation {

  @Column(name = "id")
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private int id;

  @Column(name = "state")
  private String state;

  @ElementCollection
  @LazyCollection(LazyCollectionOption.FALSE)
  private Map<String, String> qosParameters;

  public QoS_Resource_Reservation() {

  }

  public QoS_Resource_Reservation(String state) {
    super();
    this.state = state;
  }

  public QoS_Resource_Reservation(String state, Map<String, String> qosParameters) {
    this.state = state;
    this.qosParameters = qosParameters;
  }

  /**
   * Get ID
   *
   * @return returns integer with ID
   */
  public int getId() {
    return id;
  }

  /**
   * set ID
   *
   * @param id integer ID
   */
  public void setId(int id) {
    this.id = id;
  }

  /**
   * get state of qos reservation
   *
   * @return returns state
   */
  public String getState() {
    return state;
  }

  /**
   * set qos reservation state
   *
   * @param state qos reservation state
   */
  public void setState(String state) {
    this.state = state;
  }

  /**
   * get qos parameters
   *
   * @return returns map with qos parameters
   */
  public Map<String, String> getQosParameters() {
    return qosParameters;
  }

  /**
   * set qos parameters
   *
   * @param qosParameter map with the qos parameters
   */
  public void setQosParameter(Map<String, String> qosParameter) {
    this.qosParameters = qosParameter;
  }

}
