/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.common.api.service;

import eu.arrowhead.common.api.repository.ArrowheadServiceRepo;
import eu.arrowhead.common.dto.entity.ArrowheadService;
import eu.arrowhead.common.exception.EntityNotFoundException;
import java.util.List;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ArrowheadServiceCRUD {

  private final ArrowheadServiceRepo serviceRepo;

  @Autowired
  public ArrowheadServiceCRUD(ArrowheadServiceRepo serviceRepo) {
    this.serviceRepo = serviceRepo;
  }

  //TODO javadoc and rest documentation should contain the default page size!! At all of the getAll methods!!
  public List<ArrowheadService> getAllServices(Pageable pageable, String definition) {
    if (definition != null) {
      return serviceRepo.findByDefinition(definition);
    }
    return serviceRepo.findAll(pageable).getContent();
  }

  public ArrowheadService getServiceById(long serviceId) {
    return serviceRepo.findById(serviceId)
                      .orElseThrow(() -> new EntityNotFoundException("ArrowheadService entity with id " + serviceId + " not found"));
  }

  public ArrowheadService saveArrowheadService(ArrowheadService service) {
    return serviceRepo.save(service);
  }

  public ArrowheadService updateArrowheadService(long serviceId, ArrowheadService updatedService) {
    ArrowheadService savedService = serviceRepo.findById(serviceId).orElseThrow(
        () -> new EntityNotFoundException("ArrowheadService entity with id " + serviceId + " not found"));
    BeanUtils.copyProperties(updatedService, savedService, "id");
    return serviceRepo.save(savedService);
  }

  public ResponseEntity<?> deleteArrowheadService(long serviceId) {
    return serviceRepo.findById(serviceId).map(service -> {
      serviceRepo.delete(service);
      return ResponseEntity.ok().build();
    }).orElseThrow(() -> new EntityNotFoundException("ArrowheadService entity with id " + serviceId + " not found"));
  }
}
