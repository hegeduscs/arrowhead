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

import eu.arrowhead.common.exception.DuplicateEntryException;
import eu.arrowhead.core.authorization.database.ArrowheadCloud;
import eu.arrowhead.core.authorization.database.ArrowheadService;
import eu.arrowhead.core.authorization.database.ArrowheadSystem;
import eu.arrowhead.core.authorization.database.Clouds_Services;
import eu.arrowhead.core.authorization.database.Systems_Services;
 
/**
 * @author umlaufz
 * Database Acces Object with CRUD methods on the authorization tables of the database.
 * (These tables are found in the *.autorization.database package.)
 */
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
    
    public ArrowheadSystem getSystemByName(String systemGroup, String systemName){
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
    
    public ArrowheadService getServiceByName(String serviceGroup, String serviceDefinition){
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
    
    public Systems_Services getSS(ArrowheadSystem consumer, ArrowheadSystem provider, 
    		ArrowheadService service){
    	Systems_Services ss = new Systems_Services();
    	
    	Session session = getSessionFactory().openSession();
    	Transaction transaction = null;
    	
    	try {
   		 	transaction = session.beginTransaction();
            Criteria criteria = session.createCriteria(Systems_Services.class);
            criteria.add(Restrictions.eq("consumer", consumer));
            criteria.add(Restrictions.eq("provider", provider));
            criteria.add(Restrictions.eq("service", service));
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
    
    @SuppressWarnings("unchecked")
    public List<Systems_Services> getSystemRelations(ArrowheadSystem consumer){
    	List<Systems_Services> ssList = new ArrayList<Systems_Services>();
    	
    	Session session = getSessionFactory().openSession();
    	Transaction transaction = null;
    	
    	try {
   		 	transaction = session.beginTransaction();
            Criteria criteria = session.createCriteria(Systems_Services.class);
            criteria.add(Restrictions.eq("consumer", consumer));
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
    
    @SuppressWarnings("unchecked")
	public List<ArrowheadCloud> getClouds(String operator){
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
    
    public ArrowheadCloud getCloudByName(String operator, String cloudName){
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
    
    public Clouds_Services getCS(ArrowheadCloud cloud, ArrowheadService service){
    	Clouds_Services cs = new Clouds_Services();
    	
    	Session session = getSessionFactory().openSession();
    	Transaction transaction = null;
    	
    	try {
   		 	transaction = session.beginTransaction();
            Criteria criteria = session.createCriteria(Clouds_Services.class);
            criteria.add(Restrictions.eq("cloud", cloud));
            criteria.add(Restrictions.eq("service", service));
            cs = (Clouds_Services) criteria.uniqueResult();
            transaction.commit();
        }
        catch (Exception e) {
            if (transaction!=null) transaction.rollback();
            throw e;
        }
        finally {
            session.close();
        }
    	
    	return cs;
    }
    
    @SuppressWarnings("unchecked")
    public List<Clouds_Services> getCloudRelations(ArrowheadCloud cloud){
    	List<Clouds_Services> csList = new ArrayList<Clouds_Services>();
    	
    	Session session = getSessionFactory().openSession();
    	Transaction transaction = null;
    	
    	try {
   		 	transaction = session.beginTransaction();
            Criteria criteria = session.createCriteria(Clouds_Services.class);
            criteria.add(Restrictions.eq("cloud", cloud));
            csList = (List<Clouds_Services>) criteria.list();
            transaction.commit();
        }
        catch (Exception e) {
            if (transaction!=null) transaction.rollback();
            throw e;
        }
        finally {
            session.close();
        }
    	
    	return csList;
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
    		throw new DuplicateEntryException("There is already an entry in the "
    				+ "authorization database with these parameters.");
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
    
    public <T> T saveRelation(T object){
		Session session = getSessionFactory().openSession();
    	Transaction transaction = null;
    	
    	try {
    		transaction = session.beginTransaction();
    		session.merge(object);
            transaction.commit();
        }
    	catch(ConstraintViolationException e){
    		if (transaction!=null) transaction.rollback();
    		throw new DuplicateEntryException("There is already an entry in the "
    				+ "authorization database with these parameters.");
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
    
	
}