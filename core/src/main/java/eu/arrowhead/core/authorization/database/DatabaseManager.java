package eu.arrowhead.core.authorization.database;

import java.util.ArrayList;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class DatabaseManager {

	public static void main(String[] args) {
		
		ArrowheadCloud testCloud = new ArrowheadCloud("OperatorA", "CloudNameB", "has certificate");
		ArrowheadSystem testSystem = new ArrowheadSystem("string1", "string2", "string3", "string4", "string5");
		
		ArrayList<String> interfaces = new ArrayList<String>();
		interfaces.add("json");
		interfaces.add("xml");
		
		ArrowheadService testService = new ArrowheadService("ServiceGroupA", "awesome service definition", interfaces, "so meta much wow");
		
		SessionFactory sessionFactory=new Configuration().configure().buildSessionFactory();
        
		//arrowheadcloud test commit
        Session session=sessionFactory.openSession();
        session.beginTransaction();
        session.save(testCloud);
        session.getTransaction().commit();
        session.close();
        
        //arrowheadsystem test commit
        session=sessionFactory.openSession();
        session.beginTransaction();
        session.save(testSystem);
        session.getTransaction().commit();
        session.close();
        
        //arrowheadservice test commit
        session=sessionFactory.openSession();
        session.beginTransaction();
        session.save(testService);
        session.getTransaction().commit();
        session.close();
	}

}
