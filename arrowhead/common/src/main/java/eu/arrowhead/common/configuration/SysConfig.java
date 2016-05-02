package eu.arrowhead.common.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;

import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.exception.DuplicateEntryException;
import eu.arrowhead.common.model.ArrowheadCloud;
import eu.arrowhead.common.model.ArrowheadSystem;
import eu.arrowhead.common.model.ArrowheadService;

/**
 * @author umlaufz
 * This class serves as a Database Acces Object for the database,
 * and provides URI information for the core systems.
 */
public class SysConfig {
	 
	private static SysConfig instance = null;
	private static SessionFactory sessionFactory;
	private static final String baseURI = "http://";
	   
	private SysConfig() {
		if (sessionFactory == null){
            sessionFactory = new Configuration().configure().buildSessionFactory();
        }
	}
	
	public static SysConfig getInstance() {
		if (instance == null){
			instance = new SysConfig();
	  	}
		return instance;
	}
	
	public SessionFactory getSessionFactory() {
    	if (sessionFactory == null){
    		sessionFactory = new Configuration().configure().buildSessionFactory();
    	}
    	return sessionFactory;
    }
	
	public <T> T get(Class<T> table, int id){
    	T object = null;
    	
    	Session session = getSessionFactory().openSession();
    	Transaction transaction = null;
    	
    	try {
    		 transaction = session.beginTransaction();
    		 object = session.get(table, id);
             transaction.commit();
         }
         catch (Exception e) {
             if (transaction!=null) transaction.rollback();
             throw e;
         }
         finally {
             session.close();
         }
    	
    	return object;
    }
	
	@SuppressWarnings("unchecked")
	public <T> List<T> getAll(Class<T> type){
		List<T> retrievedList = new ArrayList<T>();
		
		Session session = getSessionFactory().openSession();
    	Transaction transaction = null;
    	
    	try {
    		transaction = session.beginTransaction();
            Criteria criteria = session.createCriteria(type);
            retrievedList = (List<T>) criteria.list();
            transaction.commit();
        }
        catch (Exception e) {
            if (transaction!=null) transaction.rollback();
            throw e;
        }
        finally {
            session.close();
        }
    	
    	return retrievedList;	
	}
	
	@SuppressWarnings("unchecked")
	public List<ArrowheadSystem> getSystemGroup(String systemGroup){
    	List<ArrowheadSystem> systemList = new ArrayList<ArrowheadSystem>();
    	
    	Session session = getSessionFactory().openSession();
    	Transaction transaction = null;
    	
    	try {
    		transaction = session.beginTransaction();
            Criteria criteria = session.createCriteria(ArrowheadSystem.class);
            criteria.add(Restrictions.eq("systemGroup", systemGroup));
            systemList = (List<ArrowheadSystem>) criteria.list();
            transaction.commit();
        }
        catch (Exception e) {
            if (transaction!=null) transaction.rollback();
            throw e;
        }
        finally {
            session.close();
        }
    	
    	return systemList;
    }
	
	public ArrowheadSystem getArrowheadSystem(String systemGroup, String systemName){
    	ArrowheadSystem arrowheadSystem = new ArrowheadSystem();
    	
    	Session session = getSessionFactory().openSession();
    	Transaction transaction = null;
    	
    	try {
    		 transaction = session.beginTransaction();
             Criteria criteria = session.createCriteria(ArrowheadSystem.class);
             criteria.add(Restrictions.eq("systemGroup", systemGroup));
             criteria.add(Restrictions.eq("systemName", systemName));
             arrowheadSystem = (ArrowheadSystem) criteria.uniqueResult();
             transaction.commit();
         }
         catch (Exception e) {
             if (transaction!=null) transaction.rollback();
             throw e;
         }
         finally {
             session.close();
         }
    	
    	return arrowheadSystem;
    }
	
	public ArrowheadService getArrowheadService(String serviceGroup, String serviceDefinition){
    	ArrowheadService service = new ArrowheadService();
    	
    	Session session = getSessionFactory().openSession();
    	Transaction transaction = null;
    	
    	try {
    		 transaction = session.beginTransaction();
             Criteria criteria = session.createCriteria(ArrowheadService.class);
             criteria.add(Restrictions.eq("serviceGroup", serviceGroup));
             criteria.add(Restrictions.eq("serviceDefinition", serviceDefinition));
             service = (ArrowheadService) criteria.uniqueResult();
             transaction.commit();
         }
         catch (Exception e) {
             if (transaction!=null) transaction.rollback();
             throw e;
         }
         finally {
             session.close();
         }
    	
    	return service;
    }
	
    @SuppressWarnings("unchecked")
	public List<ArrowheadCloud> getCloudsFromOperator(String operator){
    	List<ArrowheadCloud> cloudList = new ArrayList<ArrowheadCloud>();
    	
    	Session session = getSessionFactory().openSession();
    	Transaction transaction = null;
    	
    	try {
    		transaction = session.beginTransaction();
            Criteria criteria = session.createCriteria(ArrowheadCloud.class);
            criteria.add(Restrictions.eq("operator", operator));
            cloudList = (List<ArrowheadCloud>) criteria.list();
            transaction.commit();
        }
        catch (Exception e) {
            if (transaction!=null) transaction.rollback();
            throw e;
        }
        finally {
            session.close();
        }
    	
    	return cloudList;
    }
    
    public ArrowheadCloud getArrowheadCloud(String operator, String cloudName){
    	ArrowheadCloud arrowheadCloud = new ArrowheadCloud();
    	
    	Session session = getSessionFactory().openSession();
    	Transaction transaction = null;
    	
    	try {
    		 transaction = session.beginTransaction();
             Criteria criteria = session.createCriteria(ArrowheadCloud.class);
             criteria.add(Restrictions.eq("operator", operator));
             criteria.add(Restrictions.eq("cloudName", cloudName));
             arrowheadCloud = (ArrowheadCloud) criteria.uniqueResult();
             transaction.commit();
         }
         catch (Exception e) {
             if (transaction!=null) transaction.rollback();
             throw e;
         }
         finally {
             session.close();
         }
    	
    	return arrowheadCloud;
    }
	
	public NeighborCloud getNeighborCloud(String operator, String cloudName){
		NeighborCloud retrievedCloud;
		
		Session session = getSessionFactory().openSession();
    	Transaction transaction = null;
    	
    	try {
    		transaction = session.beginTransaction();
            Criteria criteria = session.createCriteria(NeighborCloud.class);
            criteria.add(Restrictions.eq("operator", operator));
            criteria.add(Restrictions.eq("cloudName", cloudName));
            retrievedCloud = (NeighborCloud) criteria.uniqueResult();
            if(retrievedCloud == null){
            	throw new DataNotFoundException("The requested Neighbor Cloud is not "
            		+ "in the configuration database. (OP: " + operator +", CN: " + cloudName + ")");
            }
            transaction.commit();
        }
        catch (Exception e) {
            if (transaction!=null) transaction.rollback();
            throw e;
        }
        finally {
            session.close();
        }
    	
    	return retrievedCloud;	
	}
	
	public CoreSystem getCoreSystem(String systemName){
		CoreSystem retrievedSystem;
		
		Session session = getSessionFactory().openSession();
    	Transaction transaction = null;
    	
    	try {
    		transaction = session.beginTransaction();
            Criteria criteria = session.createCriteria(CoreSystem.class);
            criteria.add(Restrictions.eq("systemName", systemName));
            retrievedSystem = (CoreSystem) criteria.uniqueResult();
            if(retrievedSystem == null){
            	throw new DataNotFoundException("The requested Core System is not "
                	+ "in the configuration database. (SN: " + systemName + ")");
            }
            transaction.commit();
        }
        catch (Exception e) {
            if (transaction!=null) transaction.rollback();
            throw e;
        }
        finally {
            session.close();
        }
    	
    	return retrievedSystem;	
	}
	
	public <T> T save(T object){
		Session session = getSessionFactory().openSession();
    	Transaction transaction = null;
    	
    	try {
    		transaction = session.beginTransaction();
    		session.save(object);
            transaction.commit();
        }
    	catch(ConstraintViolationException e){
    		if (transaction!=null) transaction.rollback();
    		throw new DuplicateEntryException("There is already an entry"
    				+ " in the configuration database with these parameters.");
    	}
        catch (Exception e) {
            if (transaction!=null) transaction.rollback();
            throw e;
        }
        finally {
            session.close();
        }
    	
    	return object;	
	}
	
	public <T> void delete(T object){
		Session session = getSessionFactory().openSession();
    	Transaction transaction = null;
    	
    	try {
    		transaction = session.beginTransaction();
    		session.delete(object);
            transaction.commit();
        }
        catch (Exception e) {
            if (transaction!=null) transaction.rollback();
            throw e;
        }
        finally {
            session.close();
        }	
	}
	
	/*
	 * Some level of flexibility in the URI creation, in order to avoid implementation mistakes.
	 */
	public String getURI(CoreSystem coreSystem){
		UriBuilder ub = null;
		if(coreSystem.getIPAddress().startsWith("http://")){
			if(coreSystem.getPort() != null){
				ub = UriBuilder.fromPath(coreSystem.getIPAddress() + ":" + coreSystem.getPort());
			}
			else{
				ub = UriBuilder.fromPath(coreSystem.getIPAddress());
			}
		}
		else{
			if(coreSystem.getPort() != null){
				ub = UriBuilder.fromPath(baseURI).path(coreSystem.getIPAddress() 
						+ ":" + coreSystem.getPort());
			}
			else{
				ub = UriBuilder.fromPath(baseURI).path(coreSystem.getIPAddress());
			}
		}
		ub.path(coreSystem.getServiceURI());
		
		return ub.toString();
	}
	
	public String getURI(NeighborCloud neighborCloud){
		UriBuilder ub = null;
		if(neighborCloud.getIPAddress().startsWith("http://")){
			if(neighborCloud.getPort() != null){
				ub = UriBuilder.fromPath(neighborCloud.getIPAddress() + ":" + neighborCloud.getPort());
			}
			else{
				ub = UriBuilder.fromPath(neighborCloud.getIPAddress());
			}
		}
		else{
			if(neighborCloud.getPort() != null){
				ub = UriBuilder.fromPath(baseURI).path(neighborCloud.getIPAddress() 
						+ ":" + neighborCloud.getPort());
			}
			else{
				ub = UriBuilder.fromPath(baseURI).path(neighborCloud.getIPAddress());
			}
		}
		ub.path(neighborCloud.getServiceURI());
		
		return ub.toString();
	}
	
	public String getOrchestratorURI(){
		CoreSystem orchestration = getCoreSystem("orchestration");
		return getURI(orchestration);
	}
	
	public String getServiceRegistryURI(){
		CoreSystem serviceRegistry = getCoreSystem("serviceregistry");
		return getURI(serviceRegistry);
	}
	
	public String getAuthorizationURI(){
		CoreSystem authorization = getCoreSystem("authorization");
		return getURI(authorization);
	}
	
	public String getGatekeeperURI(){
		CoreSystem gatekeeper = getCoreSystem("gatekeeper");
		return getURI(gatekeeper);
	}
	
	public String getQoSURI(){
		CoreSystem QoS = getCoreSystem("qos");
		return getURI(QoS);
	}

	public List<String> getCloudURIs(){
		List<NeighborCloud> cloudList = new ArrayList<NeighborCloud>();
		cloudList.addAll(getAll(NeighborCloud.class));
		
		List<String> URIList = new ArrayList<String>();
		for(NeighborCloud cloud : cloudList){
			URIList.add(getURI(cloud));
		}
		
		return URIList;
	}
	
	public ArrowheadCloud getOwnCloud(){
		List<OwnCloud> cloudList = new ArrayList<OwnCloud>();
		cloudList = getAll(OwnCloud.class);
		if(cloudList.isEmpty()){
			throw new DataNotFoundException("No 'Own Cloud' entry in the configuration database." 
					+ "Please make sure to enter one in the 'own_cloud' table."
					+ "This information is needed for the Gatekeeper System.");
		}
		OwnCloud retrievedCloud = cloudList.get(0);
		
		ArrowheadCloud ownCloud = new ArrowheadCloud();
		ownCloud.setOperator(retrievedCloud.getOperator());
		ownCloud.setCloudName(retrievedCloud.getCloudName());
		ownCloud.setAddress(retrievedCloud.getIPAddress());
		ownCloud.setPort(retrievedCloud.getPort());
		ownCloud.setGatekeeperServiceURI(retrievedCloud.getServiceURI());
		ownCloud.setAuthenticationInfo(retrievedCloud.getAuthenticationInfo());
		
		return ownCloud;
	}
	
	
}