package eu.arrowhead.core.authorization;
 
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;

import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.exception.DuplicateEntryException;
import eu.arrowhead.core.authorization.database.ArrowheadCloud;
import eu.arrowhead.core.authorization.database.ArrowheadService;
 
public class DatabaseManager {
	
    private static SessionFactory sessionFactory;
   
    public DatabaseManager(){
        if (sessionFactory==null){
            sessionFactory=new Configuration().configure().buildSessionFactory();
        }
    }
   
    public SessionFactory getSessionFactory() {
    	if (sessionFactory!=null)
    	return sessionFactory;
    	else {
    		sessionFactory=new Configuration().configure().buildSessionFactory();
    		return sessionFactory;
    	}
    }
    
    @SuppressWarnings("unchecked")
	public List<ArrowheadCloud> getClouds(String operator){
    	List<ArrowheadCloud> cloudList = new ArrayList<ArrowheadCloud>();
    	
    	Session session = getSessionFactory().openSession();
    	Transaction transaction = session.beginTransaction();
    	
    	try {
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
    	Session session = getSessionFactory().openSession();
    	Transaction transaction = session.beginTransaction();
    	
    	ArrowheadCloud arrowheadCloud;
    	
    	try {
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
    	Transaction transaction = session.beginTransaction();
    	try {
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
    	Transaction transaction = session.beginTransaction();
    	try {
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
    	Transaction transaction = session.beginTransaction();
    	try {
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
    
    public void deleteServices(String operator, String cloudName, List<ArrowheadService> serviceList){
    	ArrowheadCloud arrowheadCloud = getCloudByName(operator, cloudName);
    	arrowheadCloud.getServiceList().removeAll(serviceList);
    	deleteCloudFromAuthorized(operator, cloudName);
    	arrowheadCloud = addCloudToAuthorized(arrowheadCloud);
    }
    
}