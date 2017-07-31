/* This Source Code Form is subject to the terms of the Mozilla Public
* License, v. 2.0. If a copy of the MPL was not distributed with this
* file, you can obtain one at http://mozilla.org/MPL/2.0/. 
*
* This work was supported by National Funds through FCT (Portuguese
* Foundation for Science and Technology) and by the EU ECSEL JU
* funding, within Arrowhead project, ref. ARTEMIS/0001/2012,
* JU grant nr. 332987.
* ISEP, Polytechnic Institute of Porto.
*/
package eu.arrowhead.common.database.qos;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

/**
 * Entity class for storing Arrowhead Services in the database. The "service_group" and service_definition" columns must be unique together.
 */
@Entity
@Table(name = "arrowhead_service", uniqueConstraints = {@UniqueConstraint(columnNames = {"service_group", "service_definition"})})
@XmlRootElement
public class ArrowheadService_qos {

  @Column(name = "id")
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private int id;

  @Column(name = "service_group")
  private String serviceGroup;

  @Column(name = "service_definition")
  private String serviceDefinition;

  @ElementCollection(fetch = FetchType.LAZY)
  @LazyCollection(LazyCollectionOption.FALSE)
  private List<String> interfaces = new ArrayList<>();

  public ArrowheadService_qos() {
  }

  public ArrowheadService_qos(String serviceGroup, String serviceDefinition, List<String> interfaces) {
    this.serviceGroup = serviceGroup;
    this.serviceDefinition = serviceDefinition;
    this.interfaces = interfaces;
  }

  @XmlTransient
  public int getId() {
    return id;
  }

  /**
   * Set ID.
   *
   * @param id Indentifiers of the object.
   */
  public void setId(int id) {
    this.id = id;
  }

  /**
   * Get Service Group.
   *
   * @return Returns String.
   */
  public String getServiceGroup() {
    return serviceGroup;
  }

  /**
   * Set Service Group.
   *
   * @param serviceGroup String serviceGroup.
   */
  public void setServiceGroup(String serviceGroup) {
    this.serviceGroup = serviceGroup;
  }

  /**
   * Get Service Definition.
   *
   * @return Returns String.
   */
  public String getServiceDefinition() {
    return serviceDefinition;
  }

  /**
   * Set Service Definition.
   *
   * @param serviceDefinition ServiceDefinition.
   */
  public void setServiceDefinition(String serviceDefinition) {
    this.serviceDefinition = serviceDefinition;
  }

  /**
   * Get service definition.
   *
   * @return Returns list of string.
   */
  public List<String> getInterfaces() {
    return interfaces;
  }

  /**
   * Set Interfaces.
   *
   * @param interfaces Set list of interfaces.
   */
  public void setInterfaces(List<String> interfaces) {
    this.interfaces = interfaces;
  }

  /**
   * Add one interface.
   */
  public void setInterfaces(String oneInterface) {
    List<String> interfaces = new ArrayList<>();
    interfaces.add(oneInterface);
    this.interfaces = interfaces;
  }

  /**
   * Determines if object is valid.
   *
   * @return Returns true if is valid.
   */
  public boolean isValid() {
    return serviceGroup != null && serviceDefinition != null;
  }

  @Override
  public String toString() {
    return "(" + serviceGroup + ":" + serviceDefinition + ")";
  }

}
