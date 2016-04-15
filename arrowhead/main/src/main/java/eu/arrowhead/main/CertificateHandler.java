package eu.arrowhead.main;

import java.io.File;

import org.glassfish.grizzly.ssl.SSLContextConfigurator;

public class CertificateHandler {
	private String baseDirectory;
	//private String cloudName;
	private String trustStoreFile;
	private String defaultPassword;
	
	public CertificateHandler(String basedir) {
		this.baseDirectory = basedir;
		//this.cloudName = cloud;
	}
	
	public CertificateHandler setTrustStore(String truststore) throws Exception {		
		this.trustStoreFile = baseDirectory+"/"+truststore+".jks";
		File tempFile = new File(trustStoreFile);
		if(!tempFile.exists()) throw new Exception("Cannot find file: " + trustStoreFile);
		if(tempFile.isDirectory()) throw new Exception("File: " + trustStoreFile + " is a directory!");
		
		return this;
	}
	
	public CertificateHandler setDefaultPassword(String pass) {
		this.defaultPassword = pass;
		return this;
	}
	
	public SSLContextConfigurator getSSLContext(String certfile) throws Exception {
		String keyStoreFile = baseDirectory+"/"+certfile+".jks";
		File tempFile = new File(keyStoreFile);
		
		if(!tempFile.exists()) throw new Exception("Cannot find certificate file: " + keyStoreFile);
		if(tempFile.isDirectory()) throw new Exception("File: " + keyStoreFile + " is a directory!");
		
		SSLContextConfigurator sslCon = new SSLContextConfigurator();
		sslCon.setKeyStoreFile(keyStoreFile);
        sslCon.setKeyStorePass(defaultPassword);
        
        sslCon.setTrustStoreFile(trustStoreFile);
        sslCon.setTrustStorePass(defaultPassword);
        return sslCon;
	}
}
