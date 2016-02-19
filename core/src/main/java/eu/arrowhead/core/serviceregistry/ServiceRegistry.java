package eu.arrowhead.core.serviceregistry;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.github.danieln.dnssdjava.DnsSDBrowser;
import com.github.danieln.dnssdjava.DnsSDDomainEnumerator;
import com.github.danieln.dnssdjava.DnsSDException;
import com.github.danieln.dnssdjava.DnsSDFactory;
import com.github.danieln.dnssdjava.DnsSDRegistrator;
import com.github.danieln.dnssdjava.ServiceData;
import com.github.danieln.dnssdjava.ServiceName;
import com.github.danieln.dnssdjava.ServiceType;

import eu.arrowhead.common.model.ArrowheadSystem;
import eu.arrowhead.common.model.messages.ProvidedService;
import eu.arrowhead.common.model.messages.ServiceQueryForm;
import eu.arrowhead.common.model.messages.ServiceQueryResult;
import eu.arrowhead.common.model.messages.ServiceRegistryEntry;

public class ServiceRegistry {

	private static ServiceRegistry instance;
	private static Properties prop;

	public static synchronized ServiceRegistry getInstance() {
		if (instance == null) {
			instance = new ServiceRegistry();
			loadDNSProperties();
		}
		return instance;
	}

	public void register(String serviceGroup, String serviceName, String interf, ServiceRegistryEntry entry) {

		try {

			// Get the DNS specific settings
			/*
			 * String dnsIpAddress = prop.getProperty("dns.ip",
			 * "192.168.184.128"); String dnsDomain =
			 * prop.getProperty("dns.domain", "srv.evoin.arrowhead.eu."); int
			 * dnsPort = 53; try { dnsPort = new
			 * Integer(prop.getProperty("dns.port", "53")); } catch
			 * (NumberFormatException ex) { // TODO log }
			 * 
			 * InetSocketAddress dnsserverAddress = new
			 * InetSocketAddress(dnsIpAddress, dnsPort); DnsSDRegistrator reg =
			 * DnsSDFactory.getInstance().createRegistrator(dnsDomain,
			 * dnsserverAddress);
			 * 
			 * // set TSIG from settings setTSIGKey(reg);
			 */

		} catch (Exception e) {
			e.printStackTrace();
		}

		// TODO
		/*
		 * DnsSDDomainEnumerator dom =
		 * DnsSDFactory.getInstance().createDomainEnumerator(); ServiceData
		 * data; try { DnsSDRegistrator reg =
		 * DnsSDFactory.getInstance().createRegistrator(dom); // TODO name get
		 * from properties // reg.setTSIGKey(name,
		 * DnsSDRegistrator.TSIG_ALGORITHM_HMAC_MD5, // entry.getTSigKey());
		 * 
		 * // ServiceName name = //
		 * reg.makeServiceName(serviceGroup+"."+serviceName ", //
		 * ServiceType.valueOf(interf)); ServiceName name =
		 * reg.makeServiceName("My\\Test.Service",
		 * ServiceType.valueOf("_http._tcp,_printer")); // String nameString =
		 * name.toString(); data = new ServiceData(name, reg.getLocalHostName(),
		 * 8080);
		 * 
		 * setServiceDataProperties(entry, data);
		 * 
		 * if (reg.registerService(data)) {
		 * System.out.println("Service registered: " + name); } else {
		 * System.out.println("Service already exists: " + name); }
		 * 
		 * } catch (DnsSDException e) { e.printStackTrace(); } catch
		 * (UnknownHostException e) { e.printStackTrace(); }
		 */
	}

	private void setTSIGKey(DnsSDRegistrator reg) {
		String tsigKey = prop.getProperty("tsig.key", "RIuxP+vb5GjLXJo686NvKQ==");
		String tsigKeyName = prop.getProperty("tsig.name", "key.evoin.arrowhead.eu.");
		String tsigAlgorithm = prop.getProperty("tsig.algorithm", DnsSDRegistrator.TSIG_ALGORITHM_HMAC_MD5);
		reg.setTSIGKey(tsigKeyName, tsigAlgorithm, tsigKey);
	}

	private void setServiceDataProperties(ServiceRegistryEntry entry, ServiceData data) {
		Map<String, String> properties = data.getProperties();
		properties.put("ahsysgrp", entry.getProvider().getSystemGroup());
		properties.put("ahsysname", entry.getProvider().getSystemName());
		properties.put("ahsysauthinfo", entry.getProvider().getAuthenticationInfo());
		properties.put("path", entry.getServiceURI());
		properties.put("ahsrvmetad", entry.getServiceMetadata());
		properties.put("txtvers", entry.getVersion());
	}

	public void unRegister(String serviceGroup, String serviceName, String interf, ServiceRegistryEntry entry) {
		// TODO
		/*
		 * DnsSDDomainEnumerator dom =
		 * DnsSDFactory.getInstance().createDomainEnumerator(); ServiceName name
		 * = reg.makeServiceName("My\\Test.Service",
		 * ServiceType.valueOf("_http._tcp,_printer")); try { DnsSDRegistrator
		 * reg = DnsSDFactory.getInstance().createRegistrator(dom);
		 * 
		 * if (reg.unregisterService(name)) {
		 * System.out.println("Service unregistered: " + name); } else {
		 * System.out.println("No service to remove: " + name); } } catch
		 * (DnsSDException e) { e.printStackTrace(); }
		 */
	}

	public ServiceQueryResult provideServices(String serviceGroup, String serviceName, ServiceQueryForm queryForm) {

		// TODO
		/*
		 * DnsSDDomainEnumerator dom =
		 * DnsSDFactory.getInstance().createDomainEnumerator(); DnsSDBrowser
		 * dnssd = DnsSDFactory.getInstance().createBrowser(dom);
		 * Collection<ServiceType> types = dnssd.getServiceTypes();
		 * System.out.println(types); for (ServiceType type : types) {
		 * Collection<ServiceName> instances = dnssd.getServiceInstances(type);
		 * System.out.println(instances); for (ServiceName instance : instances)
		 * { ServiceData service = dnssd.getServiceData(instance);
		 * System.out.println(service); } }
		 */

		ServiceQueryResult result = new ServiceQueryResult();

		List<ProvidedService> list = new ArrayList<ProvidedService>();
		ProvidedService provider = new ProvidedService();

		ArrowheadSystem arrowheadSystem = new ArrowheadSystem();
		arrowheadSystem.setAuthenticationInfo("testAuth");
		arrowheadSystem.setIPAddress("127.0.0.1");
		arrowheadSystem.setPort("8443");
		arrowheadSystem.setSystemGroup("testSystemGroup");
		arrowheadSystem.setSystemName("testSystemName");

		provider.setProvider(arrowheadSystem);

		String serviceURI = "testServiceURI";
		provider.setServiceURI(serviceURI);
		String serviceInterface = "testInterface";
		provider.setServiceInterface(serviceInterface);

		list.add(provider);
		list.add(provider);
		result.setServiceQueryData(list);
		return result;
	}

	private static void loadDNSProperties() {
		if (prop == null) {
			prop = new Properties();
		}
		try {
			prop.load(ServiceRegistry.class.getResourceAsStream("dns.properties"));
		} catch (IOException e) {
			// TODO log
		}
	}

}
