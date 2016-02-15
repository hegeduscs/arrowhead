package eu.arrowhead.core.serviceregistry;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.github.danieln.dnssdjava.DnsSDBrowser;
import com.github.danieln.dnssdjava.DnsSDDomainEnumerator;
import com.github.danieln.dnssdjava.DnsSDException;
import com.github.danieln.dnssdjava.DnsSDFactory;
import com.github.danieln.dnssdjava.DnsSDRegistrator;
import com.github.danieln.dnssdjava.ServiceData;
import com.github.danieln.dnssdjava.ServiceName;
import com.github.danieln.dnssdjava.ServiceType;

import eu.arrowhead.common.model.serviceregistry.ArrowheadSystem;
import eu.arrowhead.common.model.serviceregistry.Provider;
import eu.arrowhead.common.model.serviceregistry.ServiceRegistryEntry;

public class ServiceRegistry {

	private static ServiceRegistry instance;

	public static synchronized ServiceRegistry getInstance() {
		if (instance == null) {
			instance = new ServiceRegistry();
		}
		return instance;
	}

	public void register(String serviceGroup, String serviceName, String interf, ServiceRegistryEntry entry) {
		// TODO
		/*DnsSDDomainEnumerator dom = DnsSDFactory.getInstance().createDomainEnumerator();
		ServiceData data;
		try {
			DnsSDRegistrator reg = DnsSDFactory.getInstance().createRegistrator(dom);
			// TODO name get from properties
			// reg.setTSIGKey(name, DnsSDRegistrator.TSIG_ALGORITHM_HMAC_MD5,
			// entry.getTSigKey());

			// ServiceName name =
			// reg.makeServiceName(serviceGroup+"."+serviceName ",
			// ServiceType.valueOf(interf));
			ServiceName name = reg.makeServiceName("My\\Test.Service", ServiceType.valueOf("_http._tcp,_printer"));
			// String nameString = name.toString();
			data = new ServiceData(name, reg.getLocalHostName(), 8080);

			setServiceDataProperties(entry, data);

			if (reg.registerService(data)) {
				System.out.println("Service registered: " + name);
			} else {
				System.out.println("Service already exists: " + name);
			}

		} catch (DnsSDException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}*/
	}

	private void setServiceDataProperties(ServiceRegistryEntry entry, ServiceData data) {
		Map<String, String> properties = data.getProperties();
		properties.put("ahsysgrp", entry.getArrowheadSystem().getSystemGroup());
		properties.put("ahsysname", entry.getArrowheadSystem().getSystemName());
		properties.put("ahsysauthinfo", entry.getArrowheadSystem().getAuthenticationInfo());
		properties.put("path", entry.getServiceURI());
		properties.put("ahsrvmetad", entry.getServiceMeataData());
		properties.put("txtvers", entry.getVersion());
	}

	public void unRegister(String serviceGroup, String serviceName, String interf) {
		// TODO
		/*DnsSDDomainEnumerator dom = DnsSDFactory.getInstance().createDomainEnumerator();
		String nameString = null;
		try {
			DnsSDRegistrator reg = DnsSDFactory.getInstance().createRegistrator(dom);
			ServiceName name = ServiceName.valueOf(nameString);
			if (reg.unregisterService(name)) {
				System.out.println("Service unregistered: " + name);
			} else {
				System.out.println("No service to remove: " + name);
			}
		} catch (DnsSDException e) {
			e.printStackTrace();
		}*/
	}

	public List<Provider> provideServices(String serviceGroup, String serviceName, String serviceMetadata, boolean pingProviders,
			List<String> serviceInterfaces, String tSIG_key) {

		// TODO
		/*DnsSDDomainEnumerator dom = DnsSDFactory.getInstance().createDomainEnumerator();
		DnsSDBrowser dnssd = DnsSDFactory.getInstance().createBrowser(dom);
		Collection<ServiceType> types = dnssd.getServiceTypes();
		System.out.println(types);
		for (ServiceType type : types) {
			Collection<ServiceName> instances = dnssd.getServiceInstances(type);
			System.out.println(instances);
			for (ServiceName instance : instances) {
				ServiceData service = dnssd.getServiceData(instance);
				System.out.println(service);
			}
		}*/

		
		List<Provider> list = new ArrayList<Provider>();
		Provider provider = new Provider();

		ArrowheadSystem arrowheadSystem = new ArrowheadSystem();
		arrowheadSystem.setArrowheadCloud(null);
		arrowheadSystem.setAuthenticationInfo("testAuth");
		arrowheadSystem.setIpAddress("127.0.0.1");
		arrowheadSystem.setPort("8443");
		arrowheadSystem.setSystemGroup("testSystemGroup");
		arrowheadSystem.setSystemName("testSystemName");

		provider.setArrowheadSystem(arrowheadSystem);

		String serviceURI = "testServiceURI";
		provider.setServiceURI(serviceURI);

		list.add(provider);
		list.add(provider);

		return list;
	}

}
