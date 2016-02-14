package eu.arrowhead.core.authorization;
 
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
import eu.arrowhead.core.authorization.database.ArrowheadCloud;
import eu.arrowhead.core.authorization.database.ArrowheadService;
import eu.arrowhead.core.authorization.database.ArrowheadSystem;
import eu.arrowhead.core.authorization.database.Systems_Services;
 
public class DatabaseManager {
	
    private static SessionFactory sessionFactory;
   
    public DatabaseManager(){
        if (sessionFactory == null){
            sessionFactory = new Configuration().configure().buildSessionFactory();
        }
    }
   
    public SessionFactory getSessionFactory() {
    	if (sessionFactory != null)
    	return sessionFactory;
    	else {
    		sessionFactory = new Configuration().configure().buildSessionFactory();
    		return sessionFactory;
    	}
    }
    
    @SuppressWarnings("unchecked")
	public List<ArrowheadCloud> getClouds(String operator){
    	List<ArrowheadCloud> cloudList = new ArrayList<ArrowheadCloud>();
    	
    	Session session = getSessionFactory().openSession();
    	Transaction transaction = null;
    	
    	try {
    		transaction = session.beginTransaction();
            Criteria criteria = session.createCriteria(ArrowheadCloud.class);
            criteria.add(Restrictions.eq("operator", operator));
            criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
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
    
    public ArrowheadCloud getCloudByName(String operator, String cloudName){
    	ArrowheadCloud arrowheadCloud;
    	
    	Session session = getSessionFactory().openSession();
    	Transaction transaction = null;
    	
    	try {
    		 transaction = session.beginTransaction();
             Criteria criteria = session.createCriteria(ArrowheadCloud.class);
             criteria.add(Restrictions.eq("operator", operator));
             criteria.add(Restrictions.eq("cloudName", cloudName));
             criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
             arrowheadCloud = (ArrowheadCloud) criteria.uniqueResult();
             if(arrowheadCloud == null){
            	 throw new DataNotFoundException("The consumer Cloud is not in the authorized database.");
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
    	
    	return arrowheadCloud;
    }
    
    public ArrowheadCloud addCloudToAuthorized(ArrowheadCloud arrowheadCloud){
    	Session session = getSessionFactory().openSession();
    	Transaction transaction = null;
    	
    	try {
    		transaction = session.beginTransaction();
    		session.persist(arrowheadCloud);

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
    	
    	return arrowheadCloud;
    }
    
    public void deleteCloudFromAuthorized(String operator, String cloudName){
    	ArrowheadCloud arrowheadCloud = getCloudByName(operator, cloudName);
    	if(arrowheadCloud == null)
    		throw new DataNotFoundException("Cloud not found in the database.");
    	
    	Session session = getSessionFactory().openSession();
    	Transaction transaction = null;
    	
    	try {
    		transaction = session.beginTransaction();
    		session.delete(arrowheadCloud);

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
    
    public void updateAuthorizedCloud(ArrowheadCloud arrowheadCloud){
    	Session session = getSessionFactory().openSession();
    	Transaction transaction = null;
    	
    	try {
    		transaction = session.beginTransaction();
    		session.update(arrowheadCloud);

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
     * Not working.
     */
    public void deleteServices(String operator, String cloudName, List<ArrowheadService> serviceList){
    	ArrowheadCloud arrowheadCloud = getCloudByName(operator, cloudName);
    	arrowheadCloud.getServiceList().removeAll(serviceList);
    	deleteCloudFromAuthorized(operator, cloudName);
    	arrowheadCloud = addCloudToAuthorized(arrowheadCloud);
    }
    
    public ArrowheadSystem getSystemByName(String systemGroup, String systemName){
    	ArrowheadSystem arrowheadSystem;
    	
    	Session session = getSessionFactory().openSession();
    	Transaction transaction = null;
    	
    	try {
    		 transaction = session.beginTransaction();
             Criteria criteria = session.createCriteria(ArrowheadSystem.class);
             criteria.add(Restrictions.eq("systemGroup", systemGroup));
             criteria.add(Restrictions.eq("systemName", systemName));
             criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
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
    
    public List<ArrowheadService> getServiceByName(String serviceGroup, String serviceDefinition){
    	List<ArrowheadService> serviceList = new ArrayList<ArrowheadService>();
    	
    	Session session = getSessionFactory().openSession();
    	Transaction transaction = null;
    	
    	try {
    		 transaction = session.beginTransaction();
             Criteria criteria = session.createCriteria(ArrowheadService.class);
             criteria.add(Restrictions.eq("serviceGroup", serviceGroup));
             criteria.add(Restrictions.eq("serviceDefinition", serviceDefinition));
             criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
             serviceList = (List<ArrowheadService>) criteria.list();
             transaction.commit();
         }
         catch (Exception e) {
             if (transaction!=null) transaction.rollback();
             throw e;
         }
         finally {
             session.close();
         }
    	
    	return serviceList;
    }
    
    public Systems_Services getSS(ArrowheadSystem consumer, ArrowheadSystem provider, ArrowheadService service){
    	Systems_Services ss = new Systems_Services();
    	
    	Session session = getSessionFactory().openSession();
    	Transaction transaction = null;
    	
    	try {
   		 	transaction = session.beginTransaction();
            Criteria criteria = session.createCriteria(Systems_Services.class);
            criteria.add(Restrictions.eq("consumer", consumer));
            criteria.add(Restrictions.eq("provider", provider));
            criteria.add(Restrictions.eq("service", service));
            criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            ss = (Systems_Services) criteria.uniqueResult();
            transaction.commit();
        }
        catch (Exception e) {
            if (transaction!=null) transaction.rollback();
            throw e;
        }
        finally {
            session.close();
        }
    	
   	return ss;
    }
    
    public List<Systems_Services> getRelations(ArrowheadSystem consumer){
    	List<Systems_Services> ssList = new ArrayList<Systems_Services>();
    	
    	Session session = getSessionFactory().openSession();
    	Transaction transaction = null;
    	
    	try {
   		 	transaction = session.beginTransaction();
            Criteria criteria = session.createCriteria(Systems_Services.class);
            criteria.add(Restrictions.eq("consumer", consumer));
            criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            ssList = (List<Systems_Services>) criteria.list();
            transaction.commit();
        }
        catch (Exception e) {
            if (transaction!=null) transaction.rollback();
            throw e;
        }
        finally {
            session.close();
        }
    	
   	return ssList;
    }
    
    public <T> T save(T object){
		Session session = getSessionFactory().openSession();
    	Transaction transaction = null;
    	
    	try {
    		transaction = session.beginTransaction();
    		session.saveOrUpdate(object);
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
	
	public Systems_Services saveRelation(Systems_Services ss){
		Session session = getSessionFactory().openSession();
    	Transaction transaction = null;
    	
    	try {
    		transaction = session.beginTransaction();
    		session.merge(ss);
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
    	
    	return ss;	
	}
    
}