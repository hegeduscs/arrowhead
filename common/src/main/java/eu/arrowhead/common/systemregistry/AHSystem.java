package eu.arrowhead.common.systemregistry;

public class AHSystem {
	private SystemInformation info;
	
	public AHSystem(SystemInformation info) {
		this.info = info;
	}
	
	public SystemInformation getInfo() {
		return this.info;
	}
	
	public void setInfo(SystemInformation info) {
		this.info = info;
	}
	
	public String toString() {
		return info.toString();
	}
}
