package eu.arrowhead.common.database;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;

@Entity
@Table(name = "arrowhead_service_metadata")
public class ServiceMetadata {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private int id;
  private String key;
  private String value;

  public ServiceMetadata() {
  }

  public ServiceMetadata(String key, String value) {
    this.key = key;
    this.value = value;
  }

  @XmlTransient
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ServiceMetadata that = (ServiceMetadata) o;

    if (!key.equals(that.key)) {
      return false;
    }
    return value.equals(that.value);
  }

  @Override
  public int hashCode() {
    int result = key.hashCode();
    result = 31 * result + value.hashCode();
    return result;
  }
}
