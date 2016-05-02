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
import eu.arrowhead.common.model.ArrowheadCloud;
import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;
import eu.arrowhead.core.authorization.database.InterCloudAuthorization;
import eu.arrowhead.core.authorization.database.IntraCloudAuthorization;
 
/**
 * @author umlaufz, hegeduscs
 * Database Acces Object for managing the authorization tables.
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
    
    public IntraCloudAuthorization getIntraAuthRight(ArrowheadSystem consumer, ArrowheadSystem provider, 
    		ArrowheadService service){
    	IntraCloudAuthorization authRight = new IntraCloudAuthorization();
    	
    	Session session = getSessionFactory().openSession();
    	Transaction transaction = null;
    	
    	try {
   		 	transaction = session.beginTransaction();
            Criteria criteria = session.createCriteria(IntraCloudAuthorization.class);
            criteria.add(Restrictions.eq("consumer", consumer));
            criteria.add(Restrictions.eq("provider", provider));
            criteria.add(Restrictions.eq("arrowheadService", service));
            authRight = (IntraCloudAuthorization) criteria.uniqueResult();
            transaction.commit();
        }
        catch (Exception e) {
            if (transaction!=null) transaction.rollback();
            throw e;
        }
        finally {
            session.close();
        }
    	
    	return authRight;
    }
    
    @SuppressWarnings("unchecked")
    public List<IntraCloudAuthorization> getSystemRelations(ArrowheadSystem consumer){
    	List<IntraCloudAuthorization> authRightsList = new ArrayList<IntraCloudAuthorization>();
    	
    	Session session = getSessionFactory().openSession();
    	Transaction transaction = null;
    	
    	try {
   		 	transaction = session.beginTransaction();
            Criteria criteria = session.createCriteria(IntraCloudAuthorization.class);
            criteria.add(Restrictions.eq("consumer", consumer));
            authRightsList = (List<IntraCloudAuthorization>) criteria.list();
            transaction.commit();
        }
        catch (Exception e) {
            if (transaction!=null) transaction.rollback();
            throw e;
        }
        finally {
            session.close();
        }
    	
    	return authRightsList;
    }
    
    public InterCloudAuthorization getInterAuthRight(ArrowheadCloud cloud, ArrowheadService service){
    	InterCloudAuthorization authRight = new InterCloudAuthorization();
    	
    	Session session = getSessionFactory().openSession();
    	Transaction transaction = null;
    	
    	try {
   		 	transaction = session.beginTransaction();
            Criteria criteria = session.createCriteria(InterCloudAuthorization.class);
            criteria.add(Restrictions.eq("cloud", cloud));
            criteria.add(Restrictions.eq("arrowheadService", service));
            authRight = (InterCloudAuthorization) criteria.uniqueResult();
            transaction.commit();
        }
        catch (Exception e) {
            if (transaction!=null) transaction.rollback();
            throw e;
        }
        finally {
            session.close();
        }
    	
    	return authRight;
    }
    
    @SuppressWarnings("unchecked")
    public List<InterCloudAuthorization> getCloudRelations(ArrowheadCloud cloud){
    	List<InterCloudAuthorization> authRightsList = new ArrayList<InterCloudAuthorization>();
    	
    	Session session = getSessionFactory().openSession();
    	Transaction transaction = null;
    	
    	try {
   		 	transaction = session.beginTransaction();
            Criteria criteria = session.createCriteria(InterCloudAuthorization.class);
            criteria.add(Restrictions.eq("cloud", cloud));
            authRightsList = (List<InterCloudAuthorization>) criteria.list();
            transaction.commit();
        }
        catch (Exception e) {
            if (transaction!=null) transaction.rollback();
            throw e;
        }
        finally {
            session.close();
        }
    	
    	return authRightsList;
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
    
	
}