package eu.arrowhead.common.database;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Entity
@Table(name = "arrowhead_device")
public class ArrowheadDevice {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  @NotBlank
  @Column(name = "device_name")
  @Size(max = 255, message = "System name must be 255 character at max")
  private String deviceName;

  public ArrowheadDevice() {
  }

  public ArrowheadDevice(String deviceName) {
    this.deviceName = deviceName;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getDeviceName() {
    return deviceName;
  }

  public void setDeviceName(String deviceName) {
    this.deviceName = deviceName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ArrowheadDevice)) {
      return false;
    }

    ArrowheadDevice that = (ArrowheadDevice) o;

    return deviceName != null ? deviceName.equals(that.deviceName) : that.deviceName == null;
  }

  @Override
  public int hashCode() {
    return deviceName != null ? deviceName.hashCode() : 0;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("ArrowheadDevice{");
    sb.append("deviceName='").append(deviceName).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
