/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.service_registry.api.service;

import eu.arrowhead.common.dto.entity.ServiceRegistryEntry;
import eu.arrowhead.common.exception.EntityNotFoundException;
import eu.arrowhead.service_registry.api.repository.ServiceRegistryRepo;
import java.util.List;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ServiceRegistryCRUD {

  private final ServiceRegistryRepo srRepo;

  @Autowired
  public ServiceRegistryCRUD(ServiceRegistryRepo srRepo) {
    this.srRepo = srRepo;
  }

  public List<ServiceRegistryEntry> getAllEntries(Pageable pageable, String serviceDef, String systemName) {
    if (serviceDef != null && systemName != null) {
      return srRepo.findByService_DefinitionAndProvider_Name(serviceDef, systemName);
    }
    return srRepo.findAll(pageable).getContent();
  }

  public ServiceRegistryEntry getServiceRegistryEntry(long srId) {
    return srRepo.findById(srId).orElseThrow(() -> new EntityNotFoundException("ServiceRegistry entity with id " + srId + " not found"));
  }

  public ServiceRegistryEntry getServiceRegistryEntry(ServiceRegistryEntry entry) {
    return srRepo.findByServiceAndProvider(entry.getService(), entry.getProvider())
                 .orElseThrow(() -> new EntityNotFoundException("ServiceRegistry entity not found: " + entry.toString()));
  }

  public ServiceRegistryEntry saveServiceRegistryEntry(ServiceRegistryEntry entry) {
    return srRepo.save(entry);
  }

  public ServiceRegistryEntry updateServiceRegistryEntry(long srId, ServiceRegistryEntry updatedEntry) {
    ServiceRegistryEntry savedEntry = srRepo.findById(srId)
                                            .orElseThrow(() -> new EntityNotFoundException("ServiceRegistry entity with id " + srId + " not found"));
    //TODO test if all of the id properties will be ignored, or a different id inside the service for example can result in unexpected behaviour
    BeanUtils.copyProperties(updatedEntry, savedEntry, "id");
    return srRepo.save(savedEntry);
  }

  public ResponseEntity<?> deleteServiceRegistryEntry(long srId) {
    return srRepo.findById(srId).map(entry -> {
      srRepo.delete(entry);
      return ResponseEntity.ok().build();
    }).orElseThrow(() -> new EntityNotFoundException("ServiceRegistry entity with id " + srId + " not found"));
  }
}
