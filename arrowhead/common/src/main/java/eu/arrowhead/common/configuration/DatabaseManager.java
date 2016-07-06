package eu.arrowhead.common.configuration;

import java.util.ArrayList;
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

import eu.arrowhead.common.exception.DuplicateEntryException;

public class DatabaseManager {
	
	private static DatabaseManager instance = null;
	private static SessionFactory sessionFactory;

	private DatabaseManager() {
		if (sessionFactory == null) {
			sessionFactory = new Configuration().configure().buildSessionFactory();	
		}
	}

	public static DatabaseManager getInstance() {
		if (instance == null) {
			instance = new DatabaseManager();
		}
		return instance;
	}

	public SessionFactory getSessionFactory() {
		if (sessionFactory == null) {
			sessionFactory = new Configuration().configure().buildSessionFactory();
		}
		return sessionFactory;
	}

	public <T> T get(Class<T> queryClass, int id) {
		T object = null;

		Session session = getSessionFactory().openSession();
		Transaction transaction = null;

		try {
			transaction = session.beginTransaction();
			object = session.get(queryClass, id);
			transaction.commit();
		} catch (Exception e) {
			if (transaction != null)
				transaction.rollback();
			throw e;
		} finally {
			session.close();
		}

		return object;
	}

	@SuppressWarnings("unchecked")
	public <T> T get(Class<T> queryClass, Map<String, Object> restrictionMap) {
		T object = null;

		Session session = getSessionFactory().openSession();
		Transaction transaction = null;

		try {
			transaction = session.beginTransaction();
			Criteria criteria = session.createCriteria(queryClass);
			if (null != restrictionMap && !restrictionMap.isEmpty()) {
				for (Entry<String, Object> entry : restrictionMap.entrySet()) {
					criteria.add(Restrictions.eq(entry.getKey(), entry.getValue()));
				}
			}
			object = (T) criteria.uniqueResult();
			transaction.commit();
		} catch (Exception e) {
			if (transaction != null)
				transaction.rollback();
			throw e;
		} finally {
			session.close();
		}

		return object;
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> getAll(Class<T> queryClass, Map<String, Object> restrictionMap){
		List<T> retrievedList = new ArrayList<T>();

		Session session = getSessionFactory().openSession();
		Transaction transaction = null;

		try {
			transaction = session.beginTransaction();
			Criteria criteria = session.createCriteria(queryClass);
			if (null != restrictionMap && !restrictionMap.isEmpty()) {
				for (Entry<String, Object> entry : restrictionMap.entrySet()) {
					criteria.add(Restrictions.eq(entry.getKey(), entry.getValue()));
				}
			}
			retrievedList = (List<T>) criteria.list();
			transaction.commit();
		} catch (Exception e) {
			if (transaction != null)
				transaction.rollback();
			throw e;
		} finally {
			session.close();
		}

		return retrievedList;
	}

	public <T> T save(T object) {
		Session session = getSessionFactory().openSession();
		Transaction transaction = null;

		try {
			transaction = session.beginTransaction();
			session.save(object);
			transaction.commit();
		} catch (ConstraintViolationException e) {
			if (transaction != null)
				transaction.rollback();
			throw new DuplicateEntryException(
					"DuplicateEntryException: there is already an entry in the database with these parameters. "
					+ "Please check the unique fields of the " + object.getClass());
		} catch (Exception e) {
			if (transaction != null)
				transaction.rollback();
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
			if (transaction != null)
				transaction.rollback();
			throw new DuplicateEntryException(
					"DuplicateEntryException: there is already an entry in the database with these parameters. "
							+ "Please check the unique fields of the " + object.getClass());
		} catch (Exception e) {
			if (transaction != null)
				transaction.rollback();
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
		} catch (Exception e) {
			if (transaction != null)
				transaction.rollback();
			throw e;
		} finally {
			session.close();
		}
	}

}
