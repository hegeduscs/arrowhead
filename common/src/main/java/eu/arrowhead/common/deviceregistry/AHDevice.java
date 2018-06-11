package eu.arrowhead.common.deviceregistry;

public class AHDevice {
	private DeviceInformation info;
	
	public AHDevice(DeviceInformation info) {
		this.info = info;
	}
	
	public DeviceInformation getInfo() {
		return this.info;
	}
	
	public void setInfo(DeviceInformation info) {
		this.info = info;
	}
	
	public String toString() {
		return info.toString();
	}
}
