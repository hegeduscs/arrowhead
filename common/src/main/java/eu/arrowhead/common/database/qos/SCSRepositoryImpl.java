package eu.arrowhead.common.database.qos;


import eu.arrowhead.common.exception.DuplicateEntryException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;

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
    Network_Device nD = new Network_Device("intel wifi", "FF:FF:FF:FF:FF:FB", networkCapabilities,
                                           net);
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

    Network_Device nD = new Network_Device("intel wifi", "FF:FF:FF:FF:FF:FC", networkCapabilities,
                                           net);
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

  private SessionFactory getSessionFactory() {
    if (sessionFactory != null) {
      return sessionFactory;
    } else {
      sessionFactory = new Configuration().configure(URL).
          buildSessionFactory();
      return sessionFactory;
    }
  }

  private <T> T saveRelation(T object) {
    Session session = getSessionFactory().openSession();
    Transaction transaction = null;

    try {
      transaction = session.beginTransaction();
      session.merge(object);
      transaction.commit();
    } catch (ConstraintViolationException e) {
      if (transaction != null) {
        transaction.rollback();
      }
      throw new DuplicateEntryException(
          "There is already an entry in the " + "authorization database with these parameters.");
    } catch (Exception e) {
      if (transaction != null) {
        transaction.rollback();
      }
      throw e;
    } finally {
      session.close();
    }

    return object;
  }

  private <T> T get(Class<T> queryClass, int id) {
    T object = null;

    Session session = getSessionFactory().openSession();
    Transaction transaction = null;

    try {
      transaction = session.beginTransaction();
      object = session.get(queryClass, id);
      transaction.commit();
    } catch (Exception e) {
      if (transaction != null) {
        transaction.rollback();
      }
      throw e;
    } finally {
      session.close();
    }

    return object;
  }

  @SuppressWarnings("unchecked")
  private <T> T get(Class<T> queryClass, Map<String, Object> restrictionMap) {
    T object = null;

    Session session = getSessionFactory().openSession();
    Transaction transaction = null;

    try {
      transaction = session.beginTransaction();
      Criteria criteria = session.createCriteria(queryClass);
      if (restrictionMap != null && !restrictionMap.isEmpty()) {
        for (Entry<String, Object> entry : restrictionMap.entrySet()) {
          criteria.add(Restrictions.eq(entry.getKey(), entry.
              getValue()));
        }
      }
      object = (T) criteria.uniqueResult();
      transaction.commit();
    } catch (Exception e) {
      if (transaction != null) {
        transaction.rollback();
      }
      throw e;
    } finally {
      session.close();
    }

    return object;
  }

  @SuppressWarnings("unchecked")
  private <T> List<T> getAll(Class<T> queryClass,
                             Map<String, Object> restrictionMap) {
    List<T> retrievedList = new ArrayList<>();

    Session session = getSessionFactory().openSession();
    Transaction transaction = null;

    try {
      transaction = session.beginTransaction();
      Criteria criteria = session.createCriteria(queryClass);
      if (restrictionMap != null && !restrictionMap.isEmpty()) {
        for (Entry<String, Object> entry : restrictionMap.entrySet()) {
          criteria.add(Restrictions.eq(entry.getKey(), entry.
              getValue()));
        }
      }
      retrievedList = (List<T>) criteria.list();
      transaction.commit();
    } catch (Exception e) {
      if (transaction != null) {
        transaction.rollback();
      }
      throw e;
    } finally {
      session.close();
    }

    return retrievedList;
  }

  private <T> T save(T object) {
    Session session = getSessionFactory().openSession();
    Transaction transaction = null;

    try {
      transaction = session.beginTransaction();
      session.save(object);
      transaction.commit();
    } catch (ConstraintViolationException e) {
      if (transaction != null) {
        transaction.rollback();
      }
      throw new DuplicateEntryException(
          "DuplicateEntryException: there is already an entry in the database with these parameters. "
              + "Please check the unique fields of the " + object.getClass());
    } catch (Exception e) {
      if (transaction != null) {
        transaction.rollback();
      }
      throw e;
    } finally {
      session.close();
    }

    return object;
  }

  private <T> T merge(T object) {
    Session session = getSessionFactory().openSession();
    Transaction transaction = null;

    try {
      transaction = session.beginTransaction();
      session.merge(object);
      transaction.commit();
    } catch (ConstraintViolationException e) {
      if (transaction != null) {
        transaction.rollback();
      }
      throw new DuplicateEntryException(
          "DuplicateEntryException: there is already an entry in the database with these parameters. "
              + "Please check the unique fields of the " + object.getClass());
    } catch (Exception e) {
      if (transaction != null) {
        transaction.rollback();
      }
      throw e;
    } finally {
      session.close();
    }

    return object;
  }

  private <T> void delete(T object) {
    Session session = getSessionFactory().openSession();
    Transaction transaction = null;

    try {
      transaction = session.beginTransaction();
      session.delete(object);
      transaction.commit();
    } catch (ConstraintViolationException e) {
      if (transaction != null) {
        transaction.rollback();
      }
      throw new DuplicateEntryException(
          "ConstraintViolationException: there is a reference to this object in another table, "
              + "which prevents the delete operation. (" + object.getClass() + ")");
    } catch (Exception e) {
      if (transaction != null) {
        transaction.rollback();
      }
      throw e;
    } finally {
      session.close();
    }
  }

  /**
   * TODO
   */
  public Node getNodeFromSystem(ArrowheadSystem_qos system) {
    HashMap<String, Object> restrictionMap = new HashMap<>();
    // TODO: avoid getAll and use a restrictionMap for a Map<>
    List<Node> list = getAll(Node.class, restrictionMap);

		/*
     * for (Node n : list) { if
		 * (n.getDeployedSystems().containsKey(provider)) return n; }
		 */
    for (Node n : list) {
      for (DeployedSystem dS : n.getDeployedSystems()) {
        if (dS.getSystem().getSystemGroup().equalsIgnoreCase(system.
            getSystemGroup())
            && dS.getSystem().getSystemName().equalsIgnoreCase(system.
            getSystemName())) {
          return n;
        }
      }
    }

    return null;
  }

  @Override
  public Network_Device getNetworkDeviceFromSystem(ArrowheadSystem_qos system) {
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
  public Network getNetworkFromNetworkDevice(Network_Device networkDevice) {
    HashMap<String, Object> restrictionMap = new HashMap<>();
    // TODO avoid getAll and use
    if (networkDevice.getNetwork() == null) {
      return null;
    }
    return getNetwork(networkDevice.getNetwork());

  }

  @Override
  public Node saveNode(Node node) {
    /*
		 * Iterator it = node.getDeployedSystems().entrySet().iterator(); while
		 * (it.hasNext()) { Map.Entry pair = (Map.Entry) it.next(); try {
		 * saveArrowheadSystem((ArrowheadSystem) pair.getKey());
		 * saveNetworkDevice((Network_Device) pair.getValue()); } catch
		 * (Exception e) { continue; } it.remove(); // avoids a
		 * ConcurrentModificationException }
		 */
    for (DeployedSystem dS : node.getDeployedSystems()) {
      try {
        saveArrowheadSystem(dS.getSystem());
        saveNetworkDevice(dS.getNetworkDevice());
        save(dS);
      } catch (Exception e) {
        continue;
      }
    }
    return save(node);
  }

  @Override
  public Network saveNetwork(Network network) {
    return save(network);
  }

  @Override
  public Network addNetworkDeviceToNetwork(Network network,
                                           Network_Device networkDevice) {
    Network net = getNetwork(network);
    if (net == null) {
      return null;
    }
    return save(net);
  }

  @Override
  public List<Node> getAllNodes() {
    HashMap<String, Object> restrictionMap = new HashMap<>();
    return getAll(Node.class, restrictionMap);
  }

  @Override
  public List<Network> getAllNetworks() {
    HashMap<String, Object> restrictionMap = new HashMap<>();
    return getAll(Network.class, restrictionMap);
  }

  @Override
  public List<ArrowheadSystem_qos> getAllArrowheadSystems() {
    HashMap<String, Object> restrictionMap = new HashMap<>();
    return getAll(ArrowheadSystem_qos.class, restrictionMap);
  }

  @Override
  public boolean deleteNode(Node node) {
    Node nod = getNode(node);
    if (nod == null) {
      return false;
    }
    delete(nod);
    return true;
  }

  @Override
  public boolean deleteNetwork(Network network) {
    Network net = getNetwork(network);
    if (net == null) {
      return false;
    }
    delete(net);
    return true;
  }

  @Override
  public Node getNode(Node n) {
    HashMap<String, Object> restrictionMap = new HashMap<>();
    restrictionMap.put("device_model_code", n.getDevice_model_code());
    return get(Node.class, restrictionMap);
  }

  @Override
  public Network getNetwork(Network network) {
    HashMap<String, Object> restrictionMap = new HashMap<>();
    restrictionMap.put("network_name", network.getName());
    //restrictionMap.put("network_ip", network.getNetworkIP());

    return get(Network.class, restrictionMap);

  }

  @Override
  public ArrowheadSystem_qos saveArrowheadSystem(ArrowheadSystem_qos network) {
    return save(network);
  }

  @Override
  public Network_Device saveNetworkDevice(Network_Device network) {
    return save(network);
  }

  @Override
  public Network updateNetwork(Network network) {
    return merge(network);
  }

}
