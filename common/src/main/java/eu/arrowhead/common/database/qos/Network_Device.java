package eu.arrowhead.common.database.qos;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(name = "network_device", uniqueConstraints = {
	@UniqueConstraint(columnNames = {"macAddress"})})
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@XmlRootElement
public class Network_Device {

	@Column(name = "network_device_id")
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@XmlTransient
	private int id;

	@Column(name = "name")
	private String name;

	@Column(name = "macAddress")
	private String macAddress;

	@ElementCollection
	@LazyCollection(LazyCollectionOption.FALSE)
	private Map<String, String> networkCapabilities;

	@ManyToOne(cascade = CascadeType.ALL)
	private Network network;

	protected Network_Device() {
		networkCapabilities = new HashMap<>();
	}

	public Network_Device(String name, String macAddress,
						  Map<String, String> networkCapabilities,
						  Network network) {
		this.name = name;
		this.macAddress = macAddress;
		this.networkCapabilities = networkCapabilities;
		this.network = network;
	}

	/**
	 * Get ID.
	 *
	 * @return Returns integer woth ID.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Set ID.
	 *
	 * @param id Integer with ID.
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Get Name of network device.
	 *
	 * @return Returns string with name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set name of network device.
	 *
	 * @param name String with name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get MAC Address.
	 *
	 * @return Returns a String with a MAC Address.
	 */
	public String getMacAddress() {
		return macAddress;
	}

	/**
	 * Set MAC Address.
	 *
	 * @param macAddress String with MAC address.
	 */
	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	/**
	 * Get Network Device Capabilities (ex. bandwidth)
	 *
	 * @return Returns Map with capabilities.
	 */
	public Map<String, String> getNetworkCapabilities() {
		return networkCapabilities;
	}

	/**
	 * Set Network Capabilities.
	 *
	 * @param networkCapabilities Map with network device capabilities.
	 */
	public void setNetworkCapabilities(Map<String, String> networkCapabilities) {
		this.networkCapabilities = networkCapabilities;
	}

	/**
	 * Get network where the network device is included..
	 *
	 * @return Returns network.
	 */
	public Network getNetwork() {
		return network;
	}

	/**
	 * Set Network.
	 *
	 * @param network Network.
	 */
	public void setNetwork(Network network) {
		this.network = network;
	}

}
