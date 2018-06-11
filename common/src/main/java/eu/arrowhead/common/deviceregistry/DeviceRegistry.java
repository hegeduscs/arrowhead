package eu.arrowhead.common.deviceregistry;

public class DeviceRegistry {
	private String id;
	private String mac;
	private String host;
	private int port;
	private String path;
	private boolean secure;
	private String metadata;
	
	public DeviceRegistry() {}
	
	public DeviceRegistry(String mac, String host, int port, String path, boolean secure, String metadata) {
		this.mac = mac;
		this.host = host;
		this.port = port;
		this.path = path;
		this.secure = secure;
		this.metadata = metadata;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getMac() {
		return mac;
	}
	
	public void setMac(String mac) {
		this.mac = mac;
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
}
