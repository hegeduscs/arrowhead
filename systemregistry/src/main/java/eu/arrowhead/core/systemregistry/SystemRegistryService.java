/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.core.systemregistry;

import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.core.systemregistry.model.AHSystem;
import eu.arrowhead.core.systemregistry.model.HttpEndpoint;
import eu.arrowhead.core.systemregistry.model.SystemIdentity;
import eu.arrowhead.core.systemregistry.model.SystemInformation;
import eu.arrowhead.core.systemregistry.model.SystemRegistry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;

public class SystemRegistryService {
	private static final Logger log = Logger.getLogger(SystemRegistryService.class.getName());
	private static final DatabaseManager dm = DatabaseManager.getInstance();
	private static final HashMap<String, Object> restrictionMap = new HashMap<>();
	private static SessionFactory factory;

	public SystemRegistryService() throws Exception {
		try {
			factory = new Configuration().configure().buildSessionFactory();
		} catch (Throwable ex) {
			System.err.println("Failed to create sessionFactory object." + ex);
			throw new ExceptionInInitializerError(ex);
		}
	}

	@SuppressWarnings({ "deprecation", "unchecked", "rawtypes" })
	public List<AHSystem> Lookup(String id) throws Exception {
		Session session = factory.openSession();
		Transaction tx = null;
		List<AHSystem> ret_systems = new ArrayList<AHSystem>();

		try {
			tx = session.beginTransaction();
			Criteria cr = session.createCriteria(SystemRegistry.class);

			if (id != null) {
				cr.add(Restrictions.like("id", "%" + id + "%"));
			}

			List<SystemRegistry> systems = cr.list();

			for (Iterator iterator = systems.iterator(); iterator.hasNext();) {
				SystemRegistry ahSystem = (SystemRegistry) iterator.next();
				SystemInformation information = new SystemInformation(
						new SystemIdentity(ahSystem.getId(), ahSystem.getType()), new HttpEndpoint(ahSystem.getHost(), ahSystem.getPort(), ahSystem.getPath(),
																																											 ahSystem.getSecure()),
						null, null);

				ret_systems.add(new AHSystem(information));
			}

			tx.commit();
		} catch (HibernateException e) {
			if (tx != null) {
				tx.rollback();
			}

			e.printStackTrace();
		} finally {
			session.close();
		}

		return ret_systems;
	}

	public Status Publish(String id) throws Exception {
		List<AHSystem> systems = Lookup(id);
		Status retStatus = null;
		Session session = null;
		Transaction tx = null;

		if (!systems.isEmpty()) {
			// ID already exists
			retStatus = Response.Status.CONFLICT;
		} else {
			// create new system
			session = factory.openSession();

			try {
				tx = session.beginTransaction();

				SystemRegistry newSystem = new SystemRegistry("OS", "test-host", 7000, "test-path", false, "", "Device1");
				newSystem.setId(id);

				session.save(newSystem);
				tx.commit();

				retStatus = Response.Status.CREATED;
			} catch (HibernateException e) {
				if (tx != null) {
					tx.rollback();
				}

				e.printStackTrace();
			} finally {
				session.close();
			}
		}

		return retStatus;
	}

	public Status Unpublish(String id) throws Exception {
		List<AHSystem> systems = Lookup(id);
		Status retStatus = null;
		Session session = null;
		Transaction tx = null;

		if (!systems.isEmpty()) {
			// delete the found system
			session = factory.openSession();

			try {
				// create transaction
				tx = session.beginTransaction();

				// find system
				SystemRegistry sys = (SystemRegistry) session.get(SystemRegistry.class, id);

				// delete system
				session.delete(sys);

				// commit work
				tx.commit();

				// create response
				retStatus = Response.Status.OK;
			} catch (HibernateException e) {
				if (tx != null) {
					tx.rollback();
				}

				e.printStackTrace();
			} finally {
				session.close();
			}
		} else {
			// ID does not exist
			retStatus = Response.Status.NOT_FOUND;
		}

		return retStatus;
	}

}
