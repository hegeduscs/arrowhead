package eu.arrowhead.core.systemregistry.model;

public class SystemRegistry {

  private String id;
  private String type;
  private String host;
  private int port;
  private String path;
  private boolean secure;
  private String metadata;
  private String deviceid;

  public SystemRegistry() {
  }

  public SystemRegistry(String type, String host, int port, String path, boolean secure, String metadata, String deviceid) {
    this.type = type;
    this.host = host;
    this.port = port;
    this.path = path;
    this.secure = secure;
    this.metadata = metadata;
    this.deviceid = deviceid;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public int getPort() {
    return port;
  }

  public void setport(int port) {
    this.port = port;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public boolean getSecure() {
    return secure;
  }

  public void setSecure(boolean secure) {
    this.secure = secure;
  }

  public String getMetadata() {
    return metadata;
  }

  public void setMetadata(String metadata) {
    this.metadata = metadata;
  }

  public String getDeviceId() {
    return deviceid;
  }

  public void setDeviceId(String deviceid) {
    this.deviceid = deviceid;
  }
}
