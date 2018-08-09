/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.common.json.support;

import eu.arrowhead.common.database.ArrowheadService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

public class ArrowheadServiceSupport {

  private String serviceGroup;
  private String serviceDefinition;
  private List<String> interfaces = new ArrayList<>();
  @XmlJavaTypeAdapter(StringMapAdapter.class)
  private Map<String, String> serviceMetadata = new HashMap<>();

  public ArrowheadServiceSupport() {
  }

  public ArrowheadServiceSupport(ArrowheadService service) {
    if (service.getServiceDefinition().contains("_")) {
      String[] parts = service.getServiceDefinition().split("_");
      this.serviceGroup = parts[0];
      this.serviceDefinition = parts[1];
    } else {
      this.serviceDefinition = service.getServiceDefinition();
    }
    this.interfaces = new ArrayList<>(service.getInterfaces());
    this.serviceMetadata = service.getServiceMetadata();
  }

  public ArrowheadServiceSupport(String serviceGroup, String serviceDefinition, List<String> interfaces, Map<String, String> serviceMetadata) {
    this.serviceGroup = serviceGroup;
    this.serviceDefinition = serviceDefinition;
    this.interfaces = interfaces;
    this.serviceMetadata = serviceMetadata;
  }

  public String getServiceGroup() {
    return serviceGroup;
  }

  public void setServiceGroup(String serviceGroup) {
    this.serviceGroup = serviceGroup;
  }

  public String getServiceDefinition() {
    return serviceDefinition;
  }

  public void setServiceDefinition(String serviceDefinition) {
    this.serviceDefinition = serviceDefinition;
  }

  public List<String> getInterfaces() {
    return interfaces;
  }

  public void setInterfaces(List<String> interfaces) {
    this.interfaces = interfaces;
  }

  public Map<String, String> getServiceMetadata() {
    return serviceMetadata;
  }

  public void setServiceMetadata(Map<String, String> serviceMetadata) {
    this.serviceMetadata = serviceMetadata;
  }
}
