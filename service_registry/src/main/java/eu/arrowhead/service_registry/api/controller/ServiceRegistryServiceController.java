/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.service_registry.api.controller;

import eu.arrowhead.common.dto.entity.ServiceRegistryEntry;
import eu.arrowhead.service_registry.api.service.ServiceRegistryCRUD;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("service_registry")
public class ServiceRegistryServiceController {

  private final ServiceRegistryCRUD serviceLayer;

  @Autowired
  public ServiceRegistryServiceController(ServiceRegistryCRUD serviceLayer) {
    this.serviceLayer = serviceLayer;
  }

  @GetMapping
  public List<ServiceRegistryEntry> getAllEntries(Pageable pageable, @RequestParam(value = "serviceDef", required = false) String serviceDef,
                                                  @RequestParam(value = "systemName", required = false) String systemName) {
    return serviceLayer.getAllEntries(pageable, serviceDef, systemName);
  }

  @GetMapping("{srId}")
  public ServiceRegistryEntry getEntryById(@PathVariable long srId) {
    return serviceLayer.getServiceRegistryEntry(srId);
  }

  @PutMapping
  public ServiceRegistryEntry getServiceRegistryEntry(@Valid @RequestBody ServiceRegistryEntry entry) {
    return serviceLayer.getServiceRegistryEntry(entry);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ServiceRegistryEntry saveServiceRegistryEntry(@Valid @RequestBody ServiceRegistryEntry entry) {
    return serviceLayer.saveServiceRegistryEntry(entry);
  }

  @PutMapping("{srId}")
  public ServiceRegistryEntry updateServiceRegistryEntry(@PathVariable long srId, @Valid @RequestBody ServiceRegistryEntry entry) {
    return serviceLayer.updateServiceRegistryEntry(srId, entry);
  }

  @DeleteMapping("{srId}")
  public ResponseEntity<?> deleteServiceRegistryEntry(@PathVariable long srId) {
    return serviceLayer.deleteServiceRegistryEntry(srId);
  }
}
