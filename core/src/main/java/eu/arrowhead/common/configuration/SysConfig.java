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

/**
 * @author umlaufz
 *
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
	
	public NeighborCloud getCloud(String operator, String cloudName){
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
	
	public CoreSystem getSystem(String systemName){
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
			ub = UriBuilder.fromUri(coreSystem.getIPAddress());
		}
		else{
			ub = UriBuilder.fromUri(baseURI);
		}
		if(coreSystem.getPort() != null){
			ub.path(":").path(coreSystem.getPort());
		}
		if(!coreSystem.getServiceURI().startsWith("/")){
			ub.path("/");
		}
		ub.path(coreSystem.getServiceURI());
		
		return ub.toString();
	}
	
	public String getURI(NeighborCloud neighborCloud){
		UriBuilder ub = null;
		if(neighborCloud.getIPAddress().startsWith("http://")){
			ub = UriBuilder.fromUri(neighborCloud.getIPAddress());
		}
		else{
			ub = UriBuilder.fromUri(baseURI);
		}
		if(neighborCloud.getPort() != null){
			ub.path(":").path(neighborCloud.getPort());
		}
		if(!neighborCloud.getServiceURI().startsWith("/")){
			ub.path("/");
		}
		ub.path(neighborCloud.getServiceURI());
		
		return ub.toString();
	}
	
	public String getOrchestratorURI(){
		CoreSystem orchestration = getSystem("orchestration");
		return getURI(orchestration);
	}
	
	public String getServiceRegistryURI(){
		CoreSystem serviceRegistry = getSystem("serviceregistry");
		return getURI(serviceRegistry);
	}
	
	public String getAuthorizationURI(){
		CoreSystem authorization = getSystem("authorization");
		return getURI(authorization);
	}
	
	public String getGatekeeperURI(){
		CoreSystem gatekeeper = getSystem("gatekeeper");
		return getURI(gatekeeper);
	}
	
	public String getQoSURI(){
		CoreSystem QoS = getSystem("qos");
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
		ownCloud.setName(retrievedCloud.getCloudName());
		ownCloud.setGatekeeperIP(retrievedCloud.getIPAddress());
		ownCloud.setGatekeeperPort(retrievedCloud.getPort());
		ownCloud.setGatekeeperURI(retrievedCloud.getServiceURI());
		ownCloud.setAuthenticationInfo(retrievedCloud.getAuthenticationInfo());
		
		return ownCloud;
	}
	
	
}