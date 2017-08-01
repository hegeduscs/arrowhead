package eu.arrowhead.common.database.qos;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class SCSRepositoryImpl implements ISCSRepository {

  private static final String URL = "hibernateSCS.cfg.xml";
  private static SessionFactory sessionFactory;

  public SCSRepositoryImpl() {
    if (sessionFactory == null) {
      sessionFactory = new Configuration().configure(URL).
          buildSessionFactory();
      /*try {
        bootstrap();
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}*/
    }

  }

  private void consumer(Network net) {
    // CONSUMER*****************************:
    List<DeployedSystem> deployedSystems = new ArrayList<>();
    Map<String, String> networkCapabilities = new HashMap<>();
    Map<String, String> capabilities = new HashMap<>();
    ArrowheadSystem_qos sC = new ArrowheadSystem_qos("Cs", "C1", "192.168.1.67", "9997", "noAuth");
    networkCapabilities.put("bandwitdh", "100");
    NetworkDevice nD = new NetworkDevice("intel wifi", "FF:FF:FF:FF:FF:FB", networkCapabilities, net);
    capabilities.put("processorArchitecture", "x86");
    deployedSystems.add(new DeployedSystem(sC, nD));

    Node consumerN = new Node("CONSUMER_NODE1", deployedSystems, capabilities);

    saveNode(consumerN);
  }

  private void producer(Network net) {
    // PRODUCER******************************:
    List<DeployedSystem> deployedSystems = new ArrayList<>();
    Map<String, String> networkCapabilities = new HashMap<>();
    Map<String, String> capabilities = new HashMap<>();

    deployedSystems = new ArrayList<>();
    networkCapabilities = new HashMap<>();
    capabilities = new HashMap<>();
    ArrowheadSystem_qos sP = new ArrowheadSystem_qos("Ps", "P1", "192.168.1.67", "9997", "noAuth");
    networkCapabilities.put("bandwitdh", "100");

    NetworkDevice nD = new NetworkDevice("intel wifi", "FF:FF:FF:FF:FF:FC", networkCapabilities, net);
    capabilities.put("processorArchitecture", "x86");
    deployedSystems.add(new DeployedSystem(sP, nD));

    Node producerN = new Node("PRODUCER_NODE2", deployedSystems, capabilities);

    saveNode(producerN);
  }

  protected void bootstrap() {
    // ADD NETWORK
    Map<String, String> configurations = new HashMap<>();
    configurations.put("ENTRYPOINT_URL", "http://192.168.1.79:8080/server/");
    configurations.put("EC", "20");
    configurations.put("STREAM_ID", "0");

    Network net = new Network("homeNetwork", "192.168.1.0/24", "fttse", configurations);
    saveNetwork(net);
    consumer(net);
    producer(net);
  }

  @Override
  public NetworkDevice getNetworkDeviceFromSystem(ArrowheadSystem_qos system) {
    HashMap<String, Object> restrictionMap = new HashMap<>();
    // TODO: avoid getAll and use a restrictionMap for a Map<>
    List<Node> list = getAll(Node.class, restrictionMap);

    for (Node n : list) {
      for (DeployedSystem dS : n.getDeployedSystems()) {
        if (dS.getSystem().getSystemGroup().equalsIgnoreCase((system).
            getSystemGroup()) && dS.getSystem().getSystemName().
            equalsIgnoreCase(system.getSystemName())) {
          return dS.getNetworkDevice();
        }
      }
    }

    return null;
  }

  @Override
  public Network updateNetwork(Network network) {
    return merge(network);
  }

}
