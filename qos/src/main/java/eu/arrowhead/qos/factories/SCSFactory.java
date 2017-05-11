/* This Source Code Form is subject to the terms of the Mozilla Public
* License, v. 2.0. If a copy of the MPL was not distributed with this
* file, you can obtain one at http://mozilla.org/MPL/2.0/. 
*
* This work was supported by National Funds through FCT (Portuguese
* Foundation for Science and Technology) and by the EU ECSEL JU
* funding, within Arrowhead project, ref. ARTEMIS/0001/2012,
* JU grant nr. 332987.
* ISEP, Polytechnic Institute of Porto.
*/
package eu.arrowhead.qos.factories;

import eu.arrowhead.common.database.qos.*;
import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Paulo
 *
 */
/**
 * @author Paulo
 *
 */
public class SCSFactory {

	private static SCSFactory instance;
	private ISCSRepository repo;

	protected SCSFactory() {
		super();
		repo = new SCSRepositoryImpl();
	}

	/**
	 * Returns a instance from this singleton class.
	 *
	 * @return
	 */
	public static SCSFactory getInstance() {
		if (instance == null) {
			instance = new SCSFactory();
		}
		return instance;
	}

	public ISCSRepository getRepo() {
		return repo;
	}

	public void setRepo(ISCSRepository repo) {
		this.repo = repo;
	}

	/**
	 * Get NetworkDevice from System.
	 *
	 * @param system System which is deployed on the network device.
	 * @return Returns the network device where the system is deployed.
	 */
	public Network_Device getNetworkDeviceFromSystem(ArrowheadSystem system) {
		return repo.getNetworkDeviceFromSystem(converFromDTO(system));
	}

	/**
	 * Get Node from system.
	 *
	 * @param system System which is deployed on the network device.
	 * @return Returns the node where the system is deployed.
	 */
	public Node getNodeFromSystem(ArrowheadSystem system) {
		return repo.getNodeFromSystem(converFromDTO(system));
	}

	/**
	 * Get network from a network device.
	 *
	 * @param networkDevice Network device.
	 * @return Returns the network from where the network device belongs.
	 */
	public Network getNetworkFromNetworkDevice(Network_Device networkDevice) {
		return repo.getNetworkFromNetworkDevice(networkDevice);
	}

	/**
	 * Save node.
	 *
	 * @param node Node to be saved.
	 * @return Returns the saved node.
	 */
	public Node saveNode(Node node) {
		return repo.saveNode(node);
	}

	/**
	 * Save network.
	 *
	 * @param network Network to be saved.
	 * @return Returns the saved network.
	 */
	public Network saveNetwork(Network network) {
		return repo.saveNetwork(network);
	}

	/**
	 * Update a already saved network.
	 *
	 * @param network Network to be updated.
	 * @return Returns the updated network.
	 */
	public Network updateNetwork(Network network) {
		return repo.updateNetwork(network);
	}

	/**
	 * Add network device to a network.
	 *
	 * @param network Network.
	 * @param networkDevice NetworkDevice.
	 * @return Returns the network from where the network device was added.
	 */
	public Network addNetworkDeviceToNetwork(Network network,
											 Network_Device networkDevice) {
		return repo.addNetworkDeviceToNetwork(network, networkDevice);
	}

	/**
	 * Get all nodes.
	 *
	 * @return Return a list of nodes.
	 */
	public List<Node> getAllNodes() {
		return repo.getAllNodes();
	}

	/**
	 * Get all networks.
	 *
	 * @return Returns a list of networks.
	 */
	public List<Network> getAllNetworks() {
		return repo.getAllNetworks();
	}

	/**
	 * Get all arrowhead systems.
	 *
	 * @return Returns a list of systems.
	 */
	public List<ArrowheadSystem> getAllArrowheadSystems() {
		return convertToDTO_List(repo.getAllArrowheadSystems());
	}

	/**
	 * Delete node.
	 *
	 * @param node Node to be deleted.
	 * @return Returns true if the node was successfully deleted.
	 */
	public boolean deleteNode(Node node) {
		return repo.deleteNode(node);
	}

	/**
	 * Delete a network.
	 *
	 * @param network Network to be deleted.
	 * @return Returns true if the network was successfully deleted.
	 */
	public boolean deleteNetwork(Network network) {
		return repo.deleteNetwork(network);
	}

	/**
	 * Get node.
	 *
	 * @param n Node.
	 * @return Returns the already saved node.
	 */
	public Node getNode(Node n) {
		return repo.getNode(n);
	}

	/**
	 * Get a network.
	 *
	 * @param network Network to search.
	 * @return Returns the network found on the db.
	 */
	public Network getNetwork(Network network) {
		return repo.getNetwork(network);
	}

	/**
	 * **********************************************
	 ************** Converts From/To DTO ************
	 * ***********************************************
	 */
	protected eu.arrowhead.common.database.qos.ArrowheadSystem_qos converFromDTO(
		ArrowheadSystem system) {
		if (system == null) {
			return null;
		}

		eu.arrowhead.common.database.qos.ArrowheadSystem_qos systemDB = new eu.arrowhead.common.database.qos.ArrowheadSystem_qos();
		systemDB.setAuthenticationInfo(system.getAuthenticationInfo());
		systemDB.setAddress(system.getAddress());
		systemDB.setPort(system.getPort());
		systemDB.setSystemGroup(system.getSystemGroup());
		systemDB.setSystemName(system.getSystemName());
		return systemDB;
	}

	protected static eu.arrowhead.common.database.qos.ArrowheadSystem_qos convertFromDTO(
		eu.arrowhead.common.model.ArrowheadSystem in) {

		eu.arrowhead.common.database.qos.ArrowheadSystem_qos out = new eu.arrowhead.common.database.qos.ArrowheadSystem_qos();

		out.setAuthenticationInfo(in.getAuthenticationInfo());
		out.setAddress(in.getAddress());
		out.setPort(in.getPort());
		out.setSystemGroup(in.getSystemGroup());
		out.setSystemName(in.getSystemName());

		return out;
	}

	protected static eu.arrowhead.common.model.ArrowheadSystem convertToDTO(
		eu.arrowhead.common.database.qos.ArrowheadSystem_qos in) {

		eu.arrowhead.common.model.ArrowheadSystem out = new eu.arrowhead.common.model.ArrowheadSystem();

		out.setAuthenticationInfo(in.getAuthenticationInfo());
		out.setAddress(in.getAddress());
		out.setPort(in.getPort());
		out.setSystemGroup(in.getSystemGroup());
		out.setSystemName(in.getSystemName());

		return out;
	}

	protected static List<ArrowheadSystem> convertToDTO_List(
		List<eu.arrowhead.common.database.qos.ArrowheadSystem_qos> in) {
		if (in == null) {
			return null;
		}
		List<ArrowheadSystem> out = new ArrayList<>();
		for (eu.arrowhead.common.database.qos.ArrowheadSystem_qos system : in) {
			out.add(convertToDTO(system));
		}

		return out;
	}

	protected static List<eu.arrowhead.common.database.qos.ArrowheadSystem_qos> convertFromDTO_List(
		List<ArrowheadSystem> in) {
		if (in == null) {
			return null;
		}
		List<eu.arrowhead.common.database.qos.ArrowheadSystem_qos> out = new ArrayList<>();
		for (ArrowheadSystem system : in) {
			out.add(convertFromDTO(system));
		}

		return out;
	}

	protected static eu.arrowhead.common.database.qos.ArrowheadService_qos convertFromDTO(
		eu.arrowhead.common.model.ArrowheadService in) {

		eu.arrowhead.common.database.qos.ArrowheadService_qos out = new eu.arrowhead.common.database.qos.ArrowheadService_qos();

		out.setInterfaces(in.getInterfaces());
		out.setServiceDefinition(in.getServiceDefinition());
		out.setServiceGroup(in.getServiceGroup());

		return out;
	}

	protected static eu.arrowhead.common.model.ArrowheadService convertToDTO(
		eu.arrowhead.common.database.qos.ArrowheadService_qos in) {

		eu.arrowhead.common.model.ArrowheadService out = new eu.arrowhead.common.model.ArrowheadService();

		out.setInterfaces(in.getInterfaces());
		out.setServiceDefinition(in.getServiceDefinition());
		out.setServiceGroup(in.getServiceGroup());

		return out;
	}

	protected static List<ArrowheadService> convertToDTO_ArrowheadServices(
		List<eu.arrowhead.common.database.qos.ArrowheadService_qos> in) {

		if (in == null) {
			return null;
		}
		List<ArrowheadService> out = new ArrayList<>();
		for (eu.arrowhead.common.database.qos.ArrowheadService_qos system : in) {
			out.add(convertToDTO(system));
		}

		return out;
	}

}
