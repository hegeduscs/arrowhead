package eu.arrowhead.common;

import eu.arrowhead.common.exception.DuplicateEntryException;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DatabaseManager {

  private static Logger log = Logger.getLogger(DatabaseManager.class.getName());
  private static DatabaseManager instance = null;
  private static SessionFactory sessionFactory;
  private static Properties prop;
  private static final String dbAddress = getProp().getProperty("db_address", "jdbc:mysql://arrowhead.tmit.bme.hu:3306/arrowhead");
  private static final String dbUser = getProp().getProperty("db_user", "root");
  private static final String dbPassword = getProp().getProperty("db_password", "root");

  static {
    try {
      if (sessionFactory == null) {
        sessionFactory = new Configuration().configure().setProperty("hibernate.connection.url", dbAddress)
            .setProperty("hibernate.connection.username", dbUser).setProperty("hibernate.connection.password", dbPassword).buildSessionFactory();
      }
    } catch (Exception e) {
      log.fatal("Database connection failed, check the configuration!");
      e.printStackTrace();
    }
  }

  private DatabaseManager() {
  }

  public static DatabaseManager getInstance() {
    if (instance == null) {
      instance = new DatabaseManager();
    }
    return instance;
  }

  private synchronized static Properties getProp() {
    try {
      if (prop == null) {
        prop = new Properties();
        File file = new File("config" + File.separator + "app.properties");
        FileInputStream inputStream = new FileInputStream(file);
        prop.load(inputStream);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    return prop;
  }

  public <T> T get(Class<T> queryClass, int id) {
    T object;
    Transaction transaction = null;

    try (Session session = getSessionFactory().openSession()) {
      transaction = session.beginTransaction();
      object = session.get(queryClass, id);
      transaction.commit();
    } catch (Exception e) {
      if (transaction != null) {
        transaction.rollback();
      }
      throw e;
    }

    return object;
  }

  private SessionFactory getSessionFactory() {
    if (sessionFactory == null) {
      sessionFactory = new Configuration().configure().setProperty("hibernate.connection.url", dbAddress)
          .setProperty("hibernate.connection.username", dbUser).setProperty("hibernate.connection.password", dbPassword).buildSessionFactory();
    }
    return sessionFactory;
  }

  @Nullable
  @SuppressWarnings("unchecked")
  public <T> T get(Class<T> queryClass, Map<String, Object> restrictionMap) {
    T object;
    Transaction transaction = null;

    try (Session session = getSessionFactory().openSession()) {
      transaction = session.beginTransaction();
      Criteria criteria = session.createCriteria(queryClass);
      if (restrictionMap != null && !restrictionMap.isEmpty()) {
        for (Entry<String, Object> entry : restrictionMap.entrySet()) {
          criteria.add(Restrictions.eq(entry.getKey(), entry.getValue()));
        }
      }
      object = (T) criteria.uniqueResult();
      transaction.commit();
    } catch (Exception e) {
      e.printStackTrace();
      log.error("get throws exception: " + e.getMessage());
      if (transaction != null) {
        transaction.rollback();
      }
      throw e;
    }

    return object;
  }

  //TODO get method Object paraméterrel, és switch case azokra az osztályokra, ahol van uniqeConstraint nem összetett mezőkkel

  @NotNull
  @SuppressWarnings("unchecked")
  public <T> List<T> getAll(Class<T> queryClass, Map<String, Object> restrictionMap) {
    List<T> retrievedList;
    Transaction transaction = null;

    try (Session session = getSessionFactory().openSession()) {
      transaction = session.beginTransaction();
      Criteria criteria = session.createCriteria(queryClass);
      if (restrictionMap != null && !restrictionMap.isEmpty()) {
        for (Entry<String, Object> entry : restrictionMap.entrySet()) {
          criteria.add(Restrictions.eq(entry.getKey(), entry.getValue()));
        }
      }
      retrievedList = (List<T>) criteria.list();
      transaction.commit();
    } catch (Exception e) {
      e.printStackTrace();
      log.error("getAll throws exception: " + e.getMessage());
      if (transaction != null) {
        transaction.rollback();
      }
      throw e;
    }

    return retrievedList;
  }

  @SuppressWarnings("unchecked")
  public <T> List<T> getAllOfEither(Class<T> queryClass, Map<String, Object> restrictionMap) {
    List<T> retrievedList;
    Transaction transaction = null;

    try (Session session = getSessionFactory().openSession()) {
      transaction = session.beginTransaction();
      Criteria criteria = session.createCriteria(queryClass);
      if (restrictionMap != null && !restrictionMap.isEmpty()) {
        Disjunction disjunction = Restrictions.disjunction();
        for (Entry<String, Object> entry : restrictionMap.entrySet()) {
          disjunction.add(Restrictions.eq(entry.getKey(), entry.getValue()));
        }
        criteria.add(disjunction);
      }
      retrievedList = (List<T>) criteria.list();
      transaction.commit();
    } catch (Exception e) {
      if (transaction != null) {
        transaction.rollback();
      }
      throw e;
    }

    return retrievedList;
  }

  public <T> T save(T object) {
    Transaction transaction = null;

    try (Session session = getSessionFactory().openSession()) {
      transaction = session.beginTransaction();
      session.save(object);
      transaction.commit();
    } catch (ConstraintViolationException e) {
      if (transaction != null) {
        transaction.rollback();
      }
      log.error("DatabaseManager:save throws DuplicateEntryException");
      throw new DuplicateEntryException(
          "DuplicateEntryException: there is already an entry in the database with these parameters. Please check the unique fields of the " + object
              .getClass());
    } catch (Exception e) {
      if (transaction != null) {
        transaction.rollback();
      }
      throw e;
    }

    return object;
  }

  public <T> T merge(T object) {
    Transaction transaction = null;

    try (Session session = getSessionFactory().openSession()) {
      transaction = session.beginTransaction();
      session.merge(object);
      transaction.commit();
    } catch (ConstraintViolationException e) {
      if (transaction != null) {
        transaction.rollback();
      }
      log.error("DatabaseManager:merge throws DuplicateEntryException");
      throw new DuplicateEntryException(
          "DuplicateEntryException: there is already an entry in the database with these parameters. Please check the unique fields of the " + object
              .getClass());
    } catch (Exception e) {
      if (transaction != null) {
        transaction.rollback();
      }
      throw e;
    }

    return object;
  }

  public <T> void delete(T object) {
    Transaction transaction = null;

    try (Session session = getSessionFactory().openSession()) {
      transaction = session.beginTransaction();
      session.delete(object);
      transaction.commit();
    } catch (ConstraintViolationException e) {
      if (transaction != null) {
        transaction.rollback();
      }
      log.error("DatabaseManager:delete throws ConstraintViolationException");
      throw new DuplicateEntryException(
          "ConstraintViolationException: there is a reference to this object in another table, which prevents the delete operation. (" + object
              .getClass() + ")");
    } catch (Exception e) {
      if (transaction != null) {
        transaction.rollback();
      }
      throw e;
    }
  }

  //TODO find out why it does not work
  public void deleteAll(String tableName) {
    Session session = getSessionFactory().openSession();
    String stringQuery = "DELETE FROM " + tableName;
    Query query = session.createQuery(stringQuery);
    query.executeUpdate();
  }
}
