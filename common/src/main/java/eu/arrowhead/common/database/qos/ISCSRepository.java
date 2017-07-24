package eu.arrowhead.common.database.qos;


import java.util.List;

public interface ISCSRepository {

	/**
	 * get the node where the arrowhead system is deployed
	 *
	 * @param provider arrowhead system
	 * @return returns node
	 */
	public Node getNodeFromSystem(ArrowheadSystem_qos provider);

	/**
	 * get network device where the arrowhead system is deployed
	 *
	 * @param provider arrowhead system
	 * @return returns network device
	 */
	public Network_Device getNetworkDeviceFromSystem(ArrowheadSystem_qos provider);

	/**
	 * get network where the network device is deployed
	 *
	 * @param networkDevice network device
	 * @return returns network
	 */
	public Network getNetworkFromNetworkDevice(Network_Device networkDevice);

	/**
	 * save node
	 *
	 * @param node node to be saved
	 * @return returns node
	 */
	public Node saveNode(Node node);

	/**
	 * save network
	 *
	 * @param network network to be saved
	 * @return returns networks
	 */
	public Network saveNetwork(Network network);

	/**
	 * save arrowhead system
	 *
	 * @param arrowheadSystem arrowhead system
	 * @return returns arrowhead system
	 */
	public ArrowheadSystem_qos saveArrowheadSystem(ArrowheadSystem_qos arrowheadSystem);

	/**
	 * save network device
	 *
	 * @param networkDevice network device
	 * @return returns network device
	 */
	public Network_Device saveNetworkDevice(Network_Device networkDevice);

	/**
	 * add network device to network
	 *
	 * @param network network
	 * @param networkDevice network device
	 * @return returns the network
	 */
	public Network addNetworkDeviceToNetwork(Network network,
											 Network_Device networkDevice);

	/**
	 * get all nodes
	 *
	 * @return returns list of nodes
	 */
	public List<Node> getAllNodes();

	/**
	 * get all networks
	 *
	 * @return returns list of networks
	 */
	public List<Network> getAllNetworks();

	/**
	 * get all arrowhead system
	 *
	 * @return returns list of arrowhead systems
	 */
	public List<ArrowheadSystem_qos> getAllArrowheadSystems();

	/**
	 * delete node
	 *
	 * @param node node to be deleted
	 * @return returns true if node was successfully deleted
	 */
	public boolean deleteNode(Node node);

	/**
	 * delete network
	 *
	 * @param network
	 * @return returns true if network was successfully deleted
	 */
	public boolean deleteNetwork(Network network);

	/**
	 * get node
	 *
	 * @param n node
	 * @return returns node from db.
	 */
	public Node getNode(Node n);

	/**
	 * get network
	 *
	 * @param network network
	 * @return returns network
	 */
	public Network getNetwork(Network network);

	/**
	 * update network
	 *
	 * @param network network
	 * @return returns network
	 */
	public Network updateNetwork(Network network);

}
