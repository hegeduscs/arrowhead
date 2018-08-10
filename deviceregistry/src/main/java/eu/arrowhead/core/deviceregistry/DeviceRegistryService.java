/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.core.deviceregistry;

import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.core.deviceregistry.model.AHDevice;
import eu.arrowhead.core.deviceregistry.model.DeviceIdentity;
import eu.arrowhead.core.deviceregistry.model.DeviceInformation;
import eu.arrowhead.core.deviceregistry.model.DeviceRegistry;
import eu.arrowhead.core.deviceregistry.model.HttpEndpoint;
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

public class DeviceRegistryService {
	private static final Logger log = Logger.getLogger(DeviceRegistryService.class.getName());
	private static final DatabaseManager dm = DatabaseManager.getInstance();
	private static final HashMap<String, Object> restrictionMap = new HashMap<>();
	private static SessionFactory factory;

	public DeviceRegistryService() throws Exception {
		try {
			factory = new Configuration().configure().buildSessionFactory();
		} catch (Throwable ex) {
			System.err.println("Failed to create sessionFactory object." + ex);
			throw new ExceptionInInitializerError(ex);
		}
	}

	@SuppressWarnings({ "deprecation", "unchecked", "rawtypes" })
	public List<AHDevice> Lookup(String id) throws Exception {
		Session session = factory.openSession();
		Transaction tx = null;
		List<AHDevice> ret_devices = new ArrayList<AHDevice>();

		try {
			tx = session.beginTransaction();
			Criteria cr = session.createCriteria(DeviceRegistry.class);

			if (id != null) {
				cr.add(Restrictions.like("id", "%" + id + "%"));
			}

			List<DeviceRegistry> devices = cr.list();

			for (Iterator iterator = devices.iterator(); iterator.hasNext();) {
				DeviceRegistry ahDevice = (DeviceRegistry) iterator.next();
				DeviceInformation information = new DeviceInformation(
						new DeviceIdentity(ahDevice.getId(), ahDevice.getMac()), new HttpEndpoint(ahDevice.getHost(), ahDevice.getPort(), ahDevice.getPath(),
																																											ahDevice.getSecure()),
						null, null);

				ret_devices.add(new AHDevice(information));
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

		return ret_devices;
	}

	public Status Publish(String id) throws Exception {
		List<AHDevice> devices = Lookup(id);
		Status retStatus = null;
		Session session = null;
		Transaction tx = null;

		if (!devices.isEmpty()) {
			// ID already exists
			retStatus = Response.Status.CONFLICT;
		} else {
			// create new system
			session = factory.openSession();

			try {
				tx = session.beginTransaction();

				DeviceRegistry newDevice = new DeviceRegistry("00:11:22:33:44:55", "test-host", 7000, "test-path",
						false, "");
				newDevice.setId(id);

				session.save(newDevice);
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
		List<AHDevice> devices = Lookup(id);
		Status retStatus = null;
		Session session = null;
		Transaction tx = null;

		if (!devices.isEmpty()) {
			// delete the found device
			session = factory.openSession();

			try {
				// create transaction
				tx = session.beginTransaction();

				// find device
				DeviceRegistry dev = (DeviceRegistry) session.get(DeviceRegistry.class, id);

				// delete device
				session.delete(dev);

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
