package eu.arrowhead.core.serviceregistry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.github.danieln.dnssdjava.DnsSDBrowser;
import com.github.danieln.dnssdjava.DnsSDDomainEnumerator;
import com.github.danieln.dnssdjava.DnsSDException;
import com.github.danieln.dnssdjava.DnsSDFactory;
import com.github.danieln.dnssdjava.DnsSDRegistrator;
import com.github.danieln.dnssdjava.ServiceData;
import com.github.danieln.dnssdjava.ServiceName;
import com.github.danieln.dnssdjava.ServiceType;

import eu.arrowhead.common.exception.DnsException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.model.ArrowheadSystem;
import eu.arrowhead.common.model.messages.ProvidedService;
import eu.arrowhead.common.model.messages.ServiceQueryForm;
import eu.arrowhead.common.model.messages.ServiceQueryResult;
import eu.arrowhead.common.model.messages.ServiceRegistryEntry;

public class ServiceRegistry {

	private static Logger log = Logger.getLogger(ServiceRegistry.class.getName());

	private static ServiceRegistry instance;
	private static Properties prop;

	public static synchronized ServiceRegistry getInstance() {
		try {
			if (instance == null) {
				instance = new ServiceRegistry();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return instance;
	}

	public synchronized Properties getProp() {
		try {
			if (prop == null) {
				prop = new Properties();
				InputStream inputStream = getClass().getClassLoader().getResourceAsStream("dns.properties");
				if (inputStream != null) {
					prop.load(inputStream);
					initSystemProperties();
				} else {
					throw new FileNotFoundException("property file 'dns.properties' not found in the classpath");
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return prop;
	}

	// This is require for dnssdjava,dnsjava
	private void initSystemProperties() {
		System.setProperty("dns.server", getProp().getProperty("dns.ip"));
		System.setProperty("dnssd.domain", getProp().getProperty("dns.domain"));
		System.setProperty("dnssd.hostname", getProp().getProperty("dns.host"));
	}

	public void register(String serviceGroup, String serviceName, String interf, ServiceRegistryEntry entry) {
		if (!parametersIsValid(serviceGroup, serviceName, interf)) {
			throw new InvalidParameterException("Invalid parameters in URL!");
		}

		try {
			if (entry != null && entry.getProvider() != null) {

				DnsSDRegistrator reg = createRegistrator();

				String serviceType = "_" + serviceGroup + "_" + serviceName + "_" + interf + "._tcp";
				// Unique service name
				String uniqueServiceName = entry.getProvider().getSystemName();
				String localName = entry.getProvider().getIPAddress() + ".";
				int port = new Integer(entry.getProvider().getPort());

				ServiceName name = reg.makeServiceName(uniqueServiceName, ServiceType.valueOf(serviceType));
				ServiceData data = new ServiceData(name, localName, port);

				// set TSIG from settings
				setTSIGKey(reg, entry.gettSIG_key());

				setServiceDataProperties(entry, data);

				if (reg.registerService(data)) {
					log.info("Service registered: " + name);
					System.out.println("Service registered: " + name);
				} else {
					log.info("Service already exists: " + name);
					System.out.println("Service already exists: " + name);
				}

			}
		} catch (DnsSDException ex) {
			log.error(ex);
			ex.printStackTrace();
			throw new DnsException(ex.getMessage());
		}

	}

	public void unRegister(String serviceGroup, String serviceName, String interf, ServiceRegistryEntry entry) {
		if (!parametersIsValid(serviceGroup, serviceName, interf)) {
			throw new InvalidParameterException("Invalid parameters in URL!");
		}

		try {
			DnsSDRegistrator reg = createRegistrator();
			String serviceType = "_" + serviceGroup + "_" + serviceName + "_" + interf + "._tcp";
			String uniqueServiceName = entry.getProvider().getSystemName();
			ServiceName name = reg.makeServiceName(uniqueServiceName, ServiceType.valueOf(serviceType));

			setTSIGKey(reg, entry.gettSIG_key());

			if (reg.unregisterService(name)) {
				log.info("Service unregistered: " + name);
				System.out.println("Service unregistered: " + name);
			} else {
				log.info("No service to remove: " + name);
				System.out.println("No service to remove: " + name);
			}
		} catch (DnsSDException ex) {
			log.error(ex);
			ex.printStackTrace();
			throw new DnsException(ex.getMessage());
		}

	}

	public ServiceQueryResult provideServices(String serviceGroup, String serviceName, ServiceQueryForm queryForm) {

		if (queryForm.getServiceInterfaces() != null && !queryForm.getServiceInterfaces().isEmpty()) {
			try {

				String computerDomain = getProp().getProperty("dns.domain", "evoin.arrowhead.eu");

				DnsSDDomainEnumerator de = DnsSDFactory.getInstance().createDomainEnumerator();

				if (computerDomain != null) {
					System.out.println("DNS-SD overriding computer domain: " + computerDomain);
					de = DnsSDFactory.getInstance().createDomainEnumerator(computerDomain);
				} else {
					de = DnsSDFactory.getInstance().createDomainEnumerator();
				}

				DnsSDBrowser browser = DnsSDFactory.getInstance().createBrowser(de.getBrowsingDomains());

				Collection<ServiceType> types = browser.getServiceTypes();
				List<ProvidedService> list = new ArrayList<ProvidedService>();
				Collection<ServiceName> needToRemoveInstances = new ArrayList<ServiceName>();
				for (ServiceType type : types) {
					Collection<ServiceName> instances = browser.getServiceInstances(type);
					System.out.println(instances);
					for (ServiceName instance : instances) {
						ServiceData service = browser.getServiceData(instance);
						if (service != null) {
							for (String serviceInterface : queryForm.getServiceInterfaces()) {
								ProvidedService providerService = buildProviderService(service, serviceGroup, serviceName,
										serviceInterface);
								if (providerService != null) {
									Map<String, String> properties = service.getProperties();
									System.out.println("queryForm.isMetadataSearch(): " + queryForm.isMetadataSearch());
									System.out.println("queryForm.isPingProviders(): " + queryForm.isPingProviders());

									if (queryForm.isMetadataSearch() || queryForm.isPingProviders()) {

										boolean replied = true;
										if (queryForm.isPingProviders()) {
											replied = pingService(needToRemoveInstances, instance, service, properties, replied);
										}

										if (replied && queryForm.isMetadataSearch()) {

											for (Map.Entry<String, String> entry : queryForm.getServiceMetaData().entrySet()) {
												String metaData = properties.get("ahsrvmetad_" + entry.getKey());
												if (metaData != null && metaData.equals(entry.getValue())) {
													System.out
															.println("Service is found by interface and metadata and added to ServiceQueryResult, interface and name and metadata are : "
																	+ providerService.getServiceInterface()
																	+ ", "
																	+ providerService.getProvider().getSystemName()
																	+ ", "
																	+ metaData);
													log.info("Service is found by interface and metadata and added to ServiceQueryResult, interface and name and metadata are : "
															+ providerService.getServiceInterface()
															+ ", "
															+ providerService.getProvider().getSystemName() + ", " + metaData);
													list.add(providerService);
												}
											}
										} else if (replied && !queryForm.isMetadataSearch()) {
											System.out
													.println("Service is found by interface and pinged and added to ServiceQueryResult, interface and name are : "
															+ providerService.getServiceInterface()
															+ ", "
															+ providerService.getProvider().getSystemName());
											log.info("Service is found by interface and metadata and added to ServiceQueryResult, interface and name are : "
													+ providerService.getServiceInterface()
													+ ", "
													+ providerService.getProvider().getSystemName());
											list.add(providerService);
										}

									} else {
										System.out
												.println("Service is found by interface and added to ServiceQueryResult, interface and name are : "
														+ providerService.getServiceInterface()
														+ ", "
														+ providerService.getProvider().getSystemName());
										log.info("Service is found by interface and added to ServiceQueryResult, interface and name are : "
												+ providerService.getServiceInterface()
												+ ", "
												+ providerService.getProvider().getSystemName());
										list.add(providerService);
									}
								}
							}

						}
						System.out.println(service);
					}
				}

				if (!needToRemoveInstances.isEmpty()) {
					removeService(needToRemoveInstances);
				}

				ServiceQueryResult result = new ServiceQueryResult();
				result.setServiceQueryData(list);
				return result;
			} catch (DnsSDException ex) {
				log.error(ex);
				ex.printStackTrace();
				throw new DnsException(ex.getMessage());
			}
		}
		return null;
	}

	public ServiceQueryResult provideAllServices() {

		try {

			String computerDomain = getProp().getProperty("dns.domain", "evoin.arrowhead.eu");

			DnsSDDomainEnumerator de = DnsSDFactory.getInstance().createDomainEnumerator();

			if (computerDomain != null) {
				System.out.println("DNS-SD overriding computer domain: " + computerDomain);
				de = DnsSDFactory.getInstance().createDomainEnumerator(computerDomain);
			} else {
				de = DnsSDFactory.getInstance().createDomainEnumerator();
			}

			DnsSDBrowser browser = DnsSDFactory.getInstance().createBrowser(de.getBrowsingDomains());

			Collection<ServiceType> types = browser.getServiceTypes();
			List<ProvidedService> list = new ArrayList<ProvidedService>();
			Collection<ServiceName> needToRemoveInstances = new ArrayList<ServiceName>();

			if (types != null) {

				for (ServiceType type : types) {
					Collection<ServiceName> instances = browser.getServiceInstances(type);
					System.out.println(instances);
					for (ServiceName instance : instances) {
						ServiceData service = browser.getServiceData(instance);
						ProvidedService providerService = null;
						if (service != null) {

							String interfaceType = null;

							String serviceType = service.getName().getType().toString();
							int dotIndex = serviceType.indexOf(".");
							if (dotIndex != -1) {
								serviceType = serviceType.substring(0, dotIndex);
								String[] array = serviceType.split("_");
								if (array.length == 4) {
									interfaceType = array[3];
								}
							}

							providerService = createProvidedService(service, interfaceType);

							if (providerService != null) {
								list.add(providerService);
							}

						}
						System.out.println(service);
					}
				}

				if (!needToRemoveInstances.isEmpty()) {
					removeService(needToRemoveInstances);
				}

				ServiceQueryResult result = new ServiceQueryResult();
				result.setServiceQueryData(list);
				return result;
			}
		} catch (DnsSDException ex) {
			log.error(ex);
			ex.printStackTrace();
			throw new DnsException(ex.getMessage());
		}
		return null;
	}

	private ProvidedService createProvidedService(ServiceData service, String interfaceType) {
		ProvidedService providerService;
		providerService = new ProvidedService();
		ArrowheadSystem arrowheadSystem = new ArrowheadSystem();

		Map<String, String> properties = service.getProperties();
		String systemGroup = properties.get("ahsysgrp");
		String systemName = properties.get("ahsysname");
		String authInfo = properties.get("ahsysauthinfo");
		String serviceURI = properties.get("path");

		String ipAddress = service.getHost();
		ipAddress = removeLastChar(ipAddress, '.');

		String port = new Integer(service.getPort()).toString();

		arrowheadSystem.setAuthenticationInfo(authInfo);
		arrowheadSystem.setIPAddress(ipAddress);
		arrowheadSystem.setPort(port);
		arrowheadSystem.setSystemGroup(systemGroup);
		arrowheadSystem.setSystemName(systemName);

		providerService.setProvider(arrowheadSystem);
		providerService.setServiceURI(serviceURI);
		providerService.setServiceInterface(interfaceType);
		return providerService;
	}

	private boolean pingService(Collection<ServiceName> needToRemoveInstances, ServiceName instance, ServiceData service,
			Map<String, String> properties, boolean replied) {
		String path = properties.get("path");
		String targetUrl = "http://" + service.getHost() + ":" + service.getPort() + path;
		int timeout = new Integer(prop.getProperty("ping.timeout", "10000")).intValue();
		int port = new Integer(service.getPort()).intValue();
		String host = removeLastChar(service.getHost(), '.');
		if (!pingHost(host, port, timeout)) {
			System.out.println("Can't access the service in the following URL " + targetUrl + ", in " + timeout + "millisec");
			log.info("Can't access the service in the following URL" + targetUrl + ", in " + timeout + "millisec");
			needToRemoveInstances.add(instance);
			replied = false;
		}
		return replied;
	}

	private void removeService(Collection<ServiceName> needToRemoveInstances) throws DnsSDException {
		DnsSDRegistrator reg = createRegistrator();
		String tsigKey = getProp().getProperty("tsig.key", "RIuxP+vb5GjLXJo686NvKQ==");
		setTSIGKey(reg, tsigKey);
		for (ServiceName sn : needToRemoveInstances) {
			if (reg.unregisterService(sn)) {
				log.info("Service unregistered: " + instance);
				System.out.println("Service unregistered: " + instance);
			} else {
				log.info("No service to remove: " + instance);
				System.out.println("No service to remove: " + instance);
			}
		}
	}

	private ProvidedService buildProviderService(ServiceData service, String serviceGroup, String serviceName,
			String serviceInterface) {
		ProvidedService providerService = null;

		if (serviceInterface != null && !serviceInterface.isEmpty() && serviceGroup != null && serviceName != null) {
			String interfaceType = null;
			String serviceGroupDns = null;
			String serviceNameDns = null;

			String serviceType = service.getName().getType().toString();
			int dotIndex = serviceType.indexOf(".");
			if (dotIndex != -1) {
				serviceType = serviceType.substring(0, dotIndex);
				String[] array = serviceType.split("_");
				if (array.length == 4) {
					serviceGroupDns = array[1];
					serviceNameDns = array[2];
					interfaceType = array[3];
				}
			}

			if (interfaceType != null && interfaceType.equals(serviceInterface) && serviceGroupDns != null
					&& serviceGroupDns.equals(serviceGroup) && serviceNameDns != null && serviceNameDns.equals(serviceName)) {
				providerService = createProvidedService(service, interfaceType);
			}
		}
		return providerService;
	}

	private DnsSDRegistrator createRegistrator() throws DnsSDException {
		// Get the DNS specific settings
		String dnsIpAddress = getProp().getProperty("dns.ip", "192.168.184.128");
		String dnsDomain = getProp().getProperty("dns.registerDomain", "srv.evoin.arrowhead.eu") + ".";
		int dnsPort = new Integer(getProp().getProperty("dns.port", "53"));

		InetSocketAddress dnsserverAddress = new InetSocketAddress(dnsIpAddress, dnsPort);
		DnsSDRegistrator reg = DnsSDFactory.getInstance().createRegistrator(dnsDomain, dnsserverAddress);
		return reg;
	}

	private void setTSIGKey(DnsSDRegistrator reg, String tsigKey) {
		System.out.println("TSIG Key: " + tsigKey);
		String tsigKeyName = getProp().getProperty("tsig.name", "key.evoin.arrowhead.eu.");
		String tsigAlgorithm = getProp().getProperty("tsig.algorithm", DnsSDRegistrator.TSIG_ALGORITHM_HMAC_MD5);
		reg.setTSIGKey(tsigKeyName, tsigAlgorithm, tsigKey);
	}

	private void setServiceDataProperties(ServiceRegistryEntry registryEntry, ServiceData data) {
		Map<String, String> properties = data.getProperties();
		properties.put("ahsysgrp", registryEntry.getProvider().getSystemGroup());
		properties.put("ahsysname", registryEntry.getProvider().getSystemName());
		properties.put("ahsysauthinfo", registryEntry.getProvider().getAuthenticationInfo());
		properties.put("path", registryEntry.getServiceURI());
		for (Map.Entry<String, String> entry : registryEntry.getServiceMetadata().entrySet()) {
			properties.put("ahsrvmetad_" + entry.getKey(), entry.getValue());
		}
	}

	private boolean parametersIsValid(String serviceGroup, String serviceName, String interf) {
		boolean result = true;
		if (serviceGroup == null || serviceName == null || interf == null || serviceGroup.contains("_")
				|| serviceName.contains("_") || interf.contains("_")) {
			result = false;
		}
		return result;
	}

	public static boolean pingHost(String host, int port, int timeout) {
		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress(host, port), timeout);
			return true;
		} catch (IOException e) {
			System.out.println("Exception Message : " + e.getMessage());
			e.printStackTrace();
			return false; // Either timeout or unreachable or failed DNS lookup.
		}
	}

	private static String removeLastChar(String host, char charachter) {
		if (host != null && host.length() > 0 && host.charAt(host.length() - 1) == charachter) {
			host = host.substring(0, host.length() - 1);
		}
		return host;
	}
}
