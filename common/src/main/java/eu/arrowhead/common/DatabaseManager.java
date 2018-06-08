/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.common;

import eu.arrowhead.common.exception.DuplicateEntryException;
import eu.arrowhead.common.misc.TypeSafeProperties;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceConfigurationError;
import javax.ws.rs.core.Response.Status;
import org.apache.log4j.Level;
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

public class DatabaseManager {

  private static DatabaseManager instance;
  private static SessionFactory sessionFactory;
  private static TypeSafeProperties prop;
  private static String dbAddress;
  private static String dbUser;
  private static String dbPassword;
  private static final Logger log = Logger.getLogger(DatabaseManager.class.getName());

  static {
    if (getProp().containsKey("db_address") || getProp().containsKey("log4j.appender.DB.URL")) {
      if (getProp().containsKey("db_address")) {
        dbAddress = getProp().getProperty("db_address");
        dbUser = getProp().getProperty("db_user");
        dbPassword = getProp().getProperty("db_password");
      } else {
        dbAddress = getProp().getProperty("log4j.appender.DB.URL", "jdbc:mysql://127.0.0.1:3306/log");
        dbUser = getProp().getProperty("log4j.appender.DB.user", "root");
        dbPassword = getProp().getProperty("log4j.appender.DB.password", "root");
      }

      try {
        Configuration configuration = new Configuration().configure("hibernate.cfg.xml").setProperty("hibernate.connection.url", dbAddress)
                                                         .setProperty("hibernate.connection.username", dbUser)
                                                         .setProperty("hibernate.connection.password", dbPassword);
        sessionFactory = configuration.buildSessionFactory();
      } catch (Exception e) {
        if (!prop.containsKey("db_address")) {
          e.printStackTrace();
          System.out.println("Database connection could not be established, logging may not work! Check log4j.properties!");
          Logger.getRootLogger().setLevel(Level.OFF);
        } else {
          throw new ServiceConfigurationError("Database connection could not be established, check app.properties!", e);
        }
      }
    }
  }

  private DatabaseManager() {
  }

  public static synchronized void init() {
    if (instance == null) {
      instance = new DatabaseManager();
    }
  }

  public static synchronized DatabaseManager getInstance() {
    if (instance == null) {
      instance = new DatabaseManager();
    }
    return instance;
  }

  private synchronized SessionFactory getSessionFactory() {
    if (sessionFactory == null) {
      Configuration configuration = new Configuration().configure("hibernate.cfg.xml").setProperty("hibernate.connection.url", dbAddress)
                                                       .setProperty("hibernate.connection.username", dbUser)
                                                       .setProperty("hibernate.connection.password", dbPassword);
      sessionFactory = configuration.buildSessionFactory();
    }
    return sessionFactory;
  }

  public static synchronized void closeSessionFactory() {
    if (sessionFactory != null) {
      sessionFactory.close();
    }
    instance = null;
  }

  private synchronized static TypeSafeProperties getProp() {
    try {
      if (prop == null) {
        prop = new TypeSafeProperties();
        File file = new File("config" + File.separator + "app.properties");
        FileInputStream inputStream = new FileInputStream(file);
        prop.load(inputStream);

        if (!prop.containsKey("db_address")) {
          file = new File("config" + File.separator + "log4j.properties");
          inputStream = new FileInputStream(file);
          prop.load(inputStream);
        }
      }
    } catch (FileNotFoundException ex) {
      throw new ServiceConfigurationError("App.properties file not found, make sure you have the correct working directory set! (directory where "
                                              + "the config folder can be found)", ex);
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
          "There is already an entry in the database with these parameters. Please check the unique fields of the " + object.getClass(),
          Status.BAD_REQUEST.getStatusCode(), e);
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
          "There is already an entry in the database with these parameters. Please check the unique fields of the " + object.getClass(),
          Status.BAD_REQUEST.getStatusCode(), e);
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
          "There is a reference to this object in another table, which prevents the delete operation. (" + object.getClass() + ")",
          Status.BAD_REQUEST.getStatusCode(), e);
    } catch (Exception e) {
      if (transaction != null) {
        transaction.rollback();
      }
      throw e;
    }
  }

  // NOTE this only works well on tables which dont have any connection to any other tables (HQL does not do cascading)
  @SuppressWarnings("unused")
  public void deleteAll(String tableName) {
    Session session = getSessionFactory().openSession();
    String stringQuery = "DELETE FROM " + tableName;
    Query query = session.createQuery(stringQuery);
    query.executeUpdate();
  }

}
