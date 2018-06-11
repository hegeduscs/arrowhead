package eu.arrowhead.common.deviceregistry;

import eu.arrowhead.common.model.Identity;

public class DeviceIdentity extends Identity{
	private String mac;
	
	public DeviceIdentity(String id, String mac) {
		super(id);
		this.mac = mac;
	}
	
	public String getMac() {
		return this.mac;
	}
	
	public void setMac(String mac) {
		this.mac = mac;
	}
	
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((mac == null) ? 0 : mac.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DeviceIdentity other = (DeviceIdentity) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (mac == null) {
			if (other.mac != null)
				return false;
		} else if (!mac.equals(other.mac))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DeviceIdentity [id=" + id + ", mac=" + mac + "]";
	}

	@Override
	public int compareTo(Identity other) {
		return other.getId().compareTo(id);
	}
}