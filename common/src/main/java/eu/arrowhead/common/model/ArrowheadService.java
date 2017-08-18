package eu.arrowhead.common.model;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlTransient;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

/**
 * Entity class for storing Arrowhead Services in the database. The "service_group" and service_definition" columns must be unique together.
 */
@Entity
@Table(name = "arrowhead_service", uniqueConstraints = {@UniqueConstraint(columnNames = {"service_group", "service_definition"})})
public class ArrowheadService {

  @Column(name = "id")
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private int id;

  @Column(name = "service_group")
  @NotNull
  private String serviceGroup;

  @Column(name = "service_definition")
  @NotNull
  //TODO look over entity classes and put NotNull annotation everywhere it should be there + use @Nullable too?!
  private String serviceDefinition;

  @ElementCollection(fetch = FetchType.LAZY)
  @LazyCollection(LazyCollectionOption.FALSE)
  @CollectionTable(name = "arrowhead_service_interface_list")
  private List<String> interfaces = new ArrayList<>();

  @Transient
  private List<ServiceMetadata> serviceMetadata = new ArrayList<>();

  public ArrowheadService() {
  }

  public ArrowheadService(String serviceGroup, String serviceDefinition, List<String> interfaces, List<ServiceMetadata> serviceMetadata) {
    this.serviceGroup = serviceGroup;
    this.serviceDefinition = serviceDefinition;
    this.interfaces = interfaces;
    this.serviceMetadata = serviceMetadata;
  }

  @XmlTransient
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getServiceGroup() {
    return serviceGroup;
  }

  public void setServiceGroup(String serviceGroup) {
    this.serviceGroup = serviceGroup;
  }

  public String getServiceDefinition() {
    return serviceDefinition;
  }

  public void setServiceDefinition(String serviceDefinition) {
    this.serviceDefinition = serviceDefinition;
  }

  public List<String> getInterfaces() {
    return interfaces;
  }

  public void setInterfaces(String oneInterface) {
    this.interfaces.clear();
    this.interfaces.add(oneInterface);
  }

  public void setInterfaces(List<String> interfaces) {
    this.interfaces = interfaces;
  }

  public List<ServiceMetadata> getServiceMetadata() {
    return serviceMetadata;
  }

  public void setServiceMetadata(List<ServiceMetadata> metaData) {
    this.serviceMetadata = metaData;
  }

  /*
   * @note  ArrowheadServices cannot contain the character "_" in any fields.
   */
  public boolean isValid() {

    boolean areInterfacesClean = true;
    for (String interf : interfaces) {
      if (interf.contains("_")) {
        areInterfacesClean = false;
      }
    }

    return serviceGroup != null && serviceDefinition != null && !interfaces.isEmpty() && !serviceGroup.contains("_") && !serviceDefinition
        .contains("_") && areInterfacesClean;
  }

  public boolean isValidForDatabase() {
    return serviceGroup != null && serviceDefinition != null;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((serviceDefinition == null) ? 0 : serviceDefinition.hashCode());
    result = prime * result + ((serviceGroup == null) ? 0 : serviceGroup.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ArrowheadService other = (ArrowheadService) obj;
    if (serviceDefinition == null) {
      if (other.serviceDefinition != null) {
        return false;
      }
    } else if (!serviceDefinition.equals(other.serviceDefinition)) {
      return false;
    }
    if (serviceGroup == null) {
      if (other.serviceGroup != null) {
        return false;
      }
    } else if (!serviceGroup.equals(other.serviceGroup)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "(" + serviceGroup + ":" + serviceDefinition + ")";
  }

}
