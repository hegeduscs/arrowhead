package eu.arrowhead.common.configuration;

import java.util.ArrayList;
import java.util.List;

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

public class SysConfig {
	 
	private static SysConfig instance = null;
	private static SessionFactory sessionFactory;
	   
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
	
	public <T> List<T> getAll(Class<T> type){
		List<T> retrievedList = new ArrayList<T>();
		
		Session session = getSessionFactory().openSession();
    	Transaction transaction = null;
    	
    	try {
    		transaction = session.beginTransaction();
            Criteria criteria = session.createCriteria(type);
            criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
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
	
	public NeighborCloud getCloud(String operator, String cloudName){
		NeighborCloud retrievedCloud;
		
		Session session = getSessionFactory().openSession();
    	Transaction transaction = null;
    	
    	try {
    		transaction = session.beginTransaction();
            Criteria criteria = session.createCriteria(NeighborCloud.class);
            criteria.add(Restrictions.eq("operator", operator));
            criteria.add(Restrictions.eq("cloudName", cloudName));
            criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            retrievedCloud = (NeighborCloud) criteria.uniqueResult();
            if(retrievedCloud == null){
            	throw new DataNotFoundException("This Cloud is not in the database.");
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
	
	public CoreSystem getSystem(String systemName){
		CoreSystem retrievedSystem;
		
		Session session = getSessionFactory().openSession();
    	Transaction transaction = null;
    	
    	try {
    		transaction = session.beginTransaction();
            Criteria criteria = session.createCriteria(CoreSystem.class);
            criteria.add(Restrictions.eq("systemName", systemName));
            criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            retrievedSystem = (CoreSystem) criteria.uniqueResult();
            if(retrievedSystem == null){
              	 throw new DataNotFoundException("This System is not in the database.");
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
    		throw new DuplicateEntryException("There is already an entry in the database with these parameters.");
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
	
	//TODO make available these methods via rest calls too
	
	public String getOrchestratorURI(){
		CoreSystem orchestration = getSystem("orchestration");
		return orchestration.getIPAddress() + orchestration.getPort() + orchestration.getServiceURI();
	}
	
	public String getServiceRegistryURI(){
		CoreSystem serviceRegistry = getSystem("serviceregistry");
		return serviceRegistry.getIPAddress() + serviceRegistry.getPort() + serviceRegistry.getServiceURI();
	}
	
	public String getAuthorizationURI(){
		CoreSystem authorization = getSystem("authorization");
		return authorization.getIPAddress() + authorization.getPort() + authorization.getServiceURI();
	}
	
	public String getGatekeeperURI(){
		CoreSystem gatekeeper = getSystem("gatekeeper");
		return gatekeeper.getIPAddress() + gatekeeper.getPort() + gatekeeper.getServiceURI();
	}
	
	public List<String> getCloudURIs(){
		List<NeighborCloud> cloudList = new ArrayList<NeighborCloud>();
		cloudList.addAll(getAll(NeighborCloud.class));
		
		List<String> URIList = new ArrayList<String>();
		for(NeighborCloud cloud : cloudList){
			URIList.add(cloud.getIPAddress() + cloud.getPort() + cloud.getServiceURI());
		}
		
		return URIList;
	}
	
	public ArrowheadCloud getInternalCloud(){
		List<OwnCloud> cloudList = new ArrayList<OwnCloud>();
		cloudList = getAll(OwnCloud.class);
		OwnCloud retrievedCloud = cloudList.get(0);
		
		ArrowheadCloud internalCloud = new ArrowheadCloud();
		internalCloud.setOperator(retrievedCloud.getOperator());
		internalCloud.setName(retrievedCloud.getCloudName());
		internalCloud.setGatekeeperIP(retrievedCloud.getIPAddress());
		internalCloud.setGatekeeperPort(retrievedCloud.getPort());
		internalCloud.setGatekeeperURI(retrievedCloud.getServiceURI());
		internalCloud.setAuthenticationInfo(retrievedCloud.getAuthenticationInfo());
		
		return internalCloud;
	}
	
}