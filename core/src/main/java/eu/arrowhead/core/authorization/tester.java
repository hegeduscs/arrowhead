package eu.arrowhead.core.authorization;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import eu.arrowhead.core.authorization.database.ArrowheadCloud;
import eu.arrowhead.core.authorization.database.ArrowheadService;

public class tester {

	public static void main(String[] args) {
		
        ArrowheadCloud testCloud = new ArrowheadCloud("A", "b", "has certificate");
        ArrowheadCloud testCloud2 = new ArrowheadCloud("A", "c", "has certificate");
        ArrowheadCloud testCloud3 = new ArrowheadCloud("A", "b", "has certificate");
        
        List<ArrowheadService> serviceList = new ArrayList<ArrowheadService>();
        ArrowheadService testService = new ArrowheadService("sg", "sd", null, "metadata");
        ArrowheadService testService2= new ArrowheadService("sg","sd2",null,"metadata2");
        ArrowheadService testService3= new ArrowheadService("sg","sd3",null,"metadata3");
        
        List<String> interfaceList = new ArrayList<String>();
        interfaceList.add("json");
        interfaceList.add("xml");
        testService2.setInterfaces(interfaceList);
        
        
        serviceList.add(testService);
        serviceList.add(testService2);
        serviceList.add(testService3);
        
        testCloud.setServiceList(serviceList);
       
        DatabaseManager databaseManager=new DatabaseManager();
        
        Session session=databaseManager.getSessionFactory().openSession();
        session.beginTransaction();
        session.save(testService);
        session.save(testService2);
        session.save(testService3);
        session.save(testCloud);
        session.save(testCloud2);
        session.save(testCloud3);
        session.getTransaction().commit();
        session.close();
        
        session=null;
        session=databaseManager.getSessionFactory().openSession();
        Transaction transaction=session.beginTransaction();
        List<ArrowheadCloud> cloudList = new ArrayList<ArrowheadCloud>();
         try {
             Criteria criteria = session.createCriteria(ArrowheadCloud.class);
             criteria.add(Restrictions.eq("operator", "A"));
             criteria.add(Restrictions.eq("cloudName", "b"));
             criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
             cloudList = (List<ArrowheadCloud>) criteria.list();
             transaction.commit();
             for (int i=0;i<cloudList.size();i++) {
             	System.out.println("Cloud of the operator:"+cloudList.get(i).getCloudName());
             	List<ArrowheadService> retrievedcloudServices=(List<ArrowheadService>)cloudList.get(i).getServiceList();
             	for (int j=0;j<retrievedcloudServices.size();j++)
             		System.out.println(retrievedcloudServices.get(j).getServiceDefinition());
             }
         }
         catch (Exception e) {
             if (transaction!=null) transaction.rollback();
             e.printStackTrace();
         }
         finally {
             session.close();
         }
        

	}

}
