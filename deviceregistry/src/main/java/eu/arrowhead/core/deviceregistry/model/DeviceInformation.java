package eu.arrowhead.core.deviceregistry.model;

public class DeviceInformation extends Information {

  private DeviceIdentity didentity;
  private DeviceMetadata dmetadata;
  private Elements<SystemIdentity> elements;

  public DeviceInformation(DeviceIdentity identity, Endpoint endpoint, DeviceMetadata metadata, Elements<SystemIdentity> elements) {
    super(identity, endpoint, metadata);
    this.didentity = identity;
    this.dmetadata = metadata;
    this.elements = elements;
  }

  public DeviceInformation() {
    super();
  }

  public DeviceIdentity getIdentity() {
    return this.didentity;
  }

  public void setIdentity(DeviceIdentity identity) {
    this.didentity = identity;
  }

  public DeviceMetadata getMetadata() {
    return this.dmetadata;
  }

  public void setMetadata(DeviceMetadata metadata) {
    this.dmetadata = metadata;
  }

  public Elements<SystemIdentity> getElements() {
    return this.elements;
  }

  public void setElements(Elements<SystemIdentity> elements) {
    this.elements = elements;
  }

  @Override
  public String toString() {
    return "Device: \n\t" + this.didentity + "\n\t" + getEndpoint() + "\n\t" + getElements() + "Metadata [data=" + this.dmetadata + "]";
  }
}
