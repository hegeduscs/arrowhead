package eu.arrowhead.common.database.qos;


import java.util.List;

public interface ISCSRepository {

  /**
   * get the node where the arrowhead system is deployed
   *
   * @param provider arrowhead system
   * @return returns node
   */
  Node getNodeFromSystem(ArrowheadSystem_qos provider);

  /**
   * get network device where the arrowhead system is deployed
   *
   * @param provider arrowhead system
   * @return returns network device
   */
  Network_Device getNetworkDeviceFromSystem(ArrowheadSystem_qos provider);

  /**
   * get network where the network device is deployed
   *
   * @param networkDevice network device
   * @return returns network
   */
  Network getNetworkFromNetworkDevice(Network_Device networkDevice);

  /**
   * save node
   *
   * @param node node to be saved
   * @return returns node
   */
  Node saveNode(Node node);

  /**
   * save network
   *
   * @param network network to be saved
   * @return returns networks
   */
  Network saveNetwork(Network network);

  /**
   * save arrowhead system
   *
   * @param arrowheadSystem arrowhead system
   * @return returns arrowhead system
   */
  ArrowheadSystem_qos saveArrowheadSystem(ArrowheadSystem_qos arrowheadSystem);

  /**
   * save network device
   *
   * @param networkDevice network device
   * @return returns network device
   */
  Network_Device saveNetworkDevice(Network_Device networkDevice);

  /**
   * add network device to network
   *
   * @param network network
   * @param networkDevice network device
   * @return returns the network
   */
  Network addNetworkDeviceToNetwork(Network network, Network_Device networkDevice);

  /**
   * get all nodes
   *
   * @return returns list of nodes
   */
  List<Node> getAllNodes();

  /**
   * get all networks
   *
   * @return returns list of networks
   */
  List<Network> getAllNetworks();

  /**
   * get all arrowhead system
   *
   * @return returns list of arrowhead systems
   */
  List<ArrowheadSystem_qos> getAllArrowheadSystems();

  /**
   * delete node
   *
   * @param node node to be deleted
   * @return returns true if node was successfully deleted
   */
  boolean deleteNode(Node node);

  /**
   * delete network
   *
   * @return returns true if network was successfully deleted
   */
  boolean deleteNetwork(Network network);

  /**
   * get node
   *
   * @param n node
   * @return returns node from db.
   */
  Node getNode(Node n);

  /**
   * get network
   *
   * @param network network
   * @return returns network
   */
  Network getNetwork(Network network);

  /**
   * update network
   *
   * @param network network
   * @return returns network
   */
  Network updateNetwork(Network network);

}
