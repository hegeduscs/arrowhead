package eu.arrowhead.common.database.qos;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "deployedSystem")
@XmlRootElement
public class DeployedSystem {

	@Column(name = "id")
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@OneToOne
	@JoinColumn(name = "arrowhead_system_id")
	private ArrowheadSystem_qos system;

	@ManyToOne
	@JoinColumn(name = "network_device_id")
	private Network_Device networkDevice;

	protected DeployedSystem() {

	}

	public DeployedSystem(ArrowheadSystem_qos system, Network_Device networkDevice) {
		super();
		this.system = system;
		this.networkDevice = networkDevice;
	}

	/**
	 * Get ArrowheadSystem_qos.
	 *
	 * @return Returns Arrowhead Sytem.
	 */
	public ArrowheadSystem_qos getSystem() {
		return system;
	}

	/**
	 * Set Arrowhead System
	 *
	 * @param system Arrowhead System.
	 */
	public void setSystem(ArrowheadSystem_qos system) {
		this.system = system;
	}

	/**
	 * Get Network Device.
	 *
	 * @return Network Device.
	 */
	public Network_Device getNetworkDevice() {
		return networkDevice;
	}

	/**
	 * Set Network Device.
	 *
	 * @param networkDevice Network Device.
	 */
	public void setNetworkDevice(Network_Device networkDevice) {
		this.networkDevice = networkDevice;
	}

}
