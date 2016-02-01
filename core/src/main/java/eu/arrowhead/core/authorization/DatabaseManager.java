package eu.arrowhead.core.authorization;
 
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;

import eu.arrowhead.core.authorization.database.ArrowheadCloud;
 
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
    
    public List<ArrowheadCloud> getCloudByName(String operator, String cloudName){
    	Session session = getSessionFactory().openSession();
    	Transaction transaction = session.beginTransaction();
    	
    	List<ArrowheadCloud> cloudList = new ArrayList<ArrowheadCloud>();
    	
    	try {
             Criteria criteria = session.createCriteria(ArrowheadCloud.class);
             criteria.add(Restrictions.eq("operator", operator));
             criteria.add(Restrictions.eq("cloudName", cloudName));
             criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
             cloudList = (List<ArrowheadCloud>) criteria.list();
             transaction.commit();
         }
         catch (Exception e) {
             if (transaction!=null) transaction.rollback();
             e.printStackTrace();
         }
         finally {
             session.close();
         }
    	return cloudList;
    }
    
    /*
     * At the moment duplicate entries possible because of the generated primary key.
     */
    public ArrowheadCloud addCloudToAuthorized(ArrowheadCloud arrowheadCloud){
    	Session session = getSessionFactory().openSession();
    	Transaction transaction = session.beginTransaction();
    	try {
            session.persist(arrowheadCloud);
            transaction.commit();
        }
        catch (Exception e) {
            if (transaction!=null) transaction.rollback();
            e.printStackTrace();
        }
        finally {
            session.close();
        }
    	
    	return arrowheadCloud;
    }
   
}