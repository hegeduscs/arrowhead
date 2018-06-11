package eu.arrowhead.common.systemregistry;

import eu.arrowhead.common.model.Identity;

public class SystemIdentity extends Identity{
	private String type;
	
	public SystemIdentity(String id, String type) {
		super(id);
		this.type = type;
	}

	public String getType() {
		return this.type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		SystemIdentity other = (SystemIdentity) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SystemIdentity [id=" + id + ", type=" + type +"]";
		//return "\n\t\t\t<System>\n\t\t\t\t<ID>" + id + "</ID>\n\t\t\t\t<Type>" + type + "</Type>\n\t\t\t</System>";
	}

	@Override
	public int compareTo(Identity other) {
		return other.getId().compareTo(id);
	}
	
}