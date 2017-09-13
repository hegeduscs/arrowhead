package eu.arrowhead.common.database;

import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlTransient;

@Entity
@Table(name = "service_registry", uniqueConstraints = {@UniqueConstraint(columnNames = {"provided_service", "provider"})})
public class ServiceRegistryEntry {

  @Column(name = "id")
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private int id;

  //mandatory fields
  @Column(name = "provided_service")
  private ArrowheadService providedService;
  @Column(name = "provider")
  private ArrowheadSystem provider;

  //non-mandatory fields
  @Column(name = "service_uri")
  private String serviceURI;
  @Transient
  private int version = 1;
  @Transient
  private boolean isUDP = false;

  //only for backwards compatibility, non-mandatory fields
  @Transient
  private List<ServiceMetadata> serviceMetadata;
  @Transient
  private List<String> interfaces;
  @Transient
  private String TSIG_key;

  public ServiceRegistryEntry() {
  }

  public ServiceRegistryEntry(ArrowheadService providedService, ArrowheadSystem provider, String serviceURI) {
    this.providedService = providedService;
    this.provider = provider;
    this.serviceURI = serviceURI;
  }

  @XmlTransient
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public ArrowheadService getProvidedService() {
    return providedService;
  }

  public void setProvidedService(ArrowheadService providedService) {
    this.providedService = providedService;
  }

  public ArrowheadSystem getProvider() {
    return provider;
  }

  public void setProvider(ArrowheadSystem provider) {
    this.provider = provider;
  }

  public String getServiceURI() {
    return serviceURI;
  }

  public void setServiceURI(String serviceURI) {
    this.serviceURI = serviceURI;
  }

  public List<ServiceMetadata> getServiceMetadata() {
    return serviceMetadata;
  }

  public void setServiceMetadata(List<ServiceMetadata> serviceMetadata) {
    this.serviceMetadata = serviceMetadata;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public List<String> getInterfaces() {
    return interfaces;
  }

  public void setInterfaces(List<String> interfaces) {
    this.interfaces = interfaces;
  }

  public boolean isUDP() {
    return isUDP;
  }

  public void setUDP(boolean UDP) {
    isUDP = UDP;
  }

  public String getTSIG_key() {
    return TSIG_key;
  }

  public void setTSIG_key(String TSIG_key) {
    this.TSIG_key = TSIG_key;
  }

  public boolean isValid() {
    return provider != null && provider.isValid();
  }

  public boolean isValidFully () {
    return provider != null && provider.isValid() && providedService != null && providedService.isValid();
  }

}
