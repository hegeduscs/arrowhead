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

/**
 * @author Paulo Barbosa Database Acces Object with CRUD methods on the authorization tables of the
 * database. (These tables are found in the *.qos.database package.)
 */
public class QoSRepositoryImpl implements IQoSRepository {

  public static final String URL = "hibernateQOS.cfg.xml";
  private static SessionFactory sessionFactory;

  public QoSRepositoryImpl() {
    if (sessionFactory == null) {
      sessionFactory = new Configuration().configure(URL).
          buildSessionFactory();
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
    List<T> retrievedList = new ArrayList<T>();

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

  private <T> T saveOrUpdate(T object) {
    Session session = getSessionFactory().openSession();
    Transaction transaction = null;

    try {
      transaction = session.beginTransaction();
      session.saveOrUpdate(object);
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

  public <T> T merge(T object) {
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

  public <T> void delete(T object) {
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

  public SessionFactory getSessionFactory() {
    if (sessionFactory != null) {
      return sessionFactory;
    } else {
      sessionFactory = new Configuration().configure(URL).
          buildSessionFactory();
      return sessionFactory;
    }
  }

  @Override
  public List<QoS_Resource_Reservation> getQoSReservationsFromArrowheadSystem(
      ArrowheadSystem_qos system) {
    List<Message_Stream> list = getAllMessage_Streams();
    List<QoS_Resource_Reservation> output = new ArrayList<>();
    for (Message_Stream mS : list) {
      if (mS.getConsumer().equals(system) || mS.getProvider().
          equals(system)) {
        output.add(mS.getQualityOfService());
      }
    }

    return output;
  }

  @Override
  public List<QoS_Resource_Reservation> getAllQoS_Resource_Reservations() {
    HashMap<String, Object> restrictionMap = new HashMap<String, Object>();
    return getAll(QoS_Resource_Reservation.class, restrictionMap);
  }

  @Override
  public List<Message_Stream> getAllMessage_Streams() {
    HashMap<String, Object> restrictionMap = new HashMap<String, Object>();
    return getAll(Message_Stream.class, restrictionMap);
  }

  @Override
  public List<ArrowheadSystem_qos> getAllArrowheadSystems() {
    HashMap<String, Object> restrictionMap = new HashMap<String, Object>();
    return getAll(ArrowheadSystem_qos.class, restrictionMap);
  }

  @Override
  public List<ArrowheadService_qos> getAllArrowheadServices() {
    HashMap<String, Object> restrictionMap = new HashMap<String, Object>();
    return getAll(ArrowheadService_qos.class, restrictionMap);
  }

  @Override
  public Message_Stream getMessage_Stream(Message_Stream messageStream) {
    HashMap<String, Object> restrictionMap = new HashMap<String, Object>();
    restrictionMap.put("code", messageStream.getCode());
    return get(Message_Stream.class, restrictionMap);
  }

  @Override
  public ArrowheadSystem_qos getArrowheadSystem(ArrowheadSystem_qos system) {
    HashMap<String, Object> restrictionMap = new HashMap<String, Object>();
    restrictionMap.put("system_group", system.getSystemGroup());
    restrictionMap.put("system_name", system.getSystemName());
    return get(ArrowheadSystem_qos.class, restrictionMap);
  }

  @Override
  public ArrowheadService_qos getArrowheadService(ArrowheadService_qos service) {
    HashMap<String, Object> restrictionMap = new HashMap<String, Object>();
    restrictionMap.put("service_group", service.getServiceGroup());
    restrictionMap.put("service_definition", service.getServiceDefinition());
    return get(ArrowheadService_qos.class, restrictionMap);
  }

  @Override
  public Message_Stream saveMessageStream(Message_Stream messageStream) {
    deleteMessageStream(messageStream);
    return saveOrUpdate(messageStream);
  }

  @Override
  public boolean deleteMessageStream(Message_Stream messageStream) {
    Message_Stream mS = getMessage_Stream(messageStream);
    if (mS == null) {
      return false;
    }
    delete(mS);
    return true;
  }

  @Override
  public boolean deleteArrowheadSystem(ArrowheadSystem_qos system) {
    ArrowheadSystem_qos mS = getArrowheadSystem(system);
    if (mS == null) {
      return false;
    }
    delete(system);
    return true;
  }

  @Override
  public boolean deleteArrowheadService(ArrowheadService_qos service) {
    ArrowheadService_qos mS = getArrowheadService(service);
    if (mS == null) {
      return false;
    }
    delete(service);
    return false;
  }

  @Override
  public List<Message_Stream> getQoS_Resource_ReservationsFromFilter(
      Map<String, String> filter) {
    List<Message_Stream> output = new ArrayList<>();
    List<Message_Stream> list = getAllMessage_Streams();
    if (filter == null || filter.isEmpty()) {
      return list;
    }
    Boolean advance = false;
    Integer cont = 0;
    for (Message_Stream m : list) {
      advance = false;
      if (m.getQualityOfService().getQosParameters().isEmpty()) {
        continue;
      }
      for (Map.Entry<String, String> entry : filter.entrySet()) {
        if (m.getQualityOfService().getQosParameters().
            containsKey(entry.getKey())) {
          String value = m.getQualityOfService().getQosParameters().
              get(entry.getKey());
          if (value.compareToIgnoreCase(entry.getValue()) != 0) {
            advance = true;
            break;
          }
          cont++;
        }
      }
      if (!advance && cont == filter.size()) {
        output.add(m);
      }
    }

    return output;
  }

}
