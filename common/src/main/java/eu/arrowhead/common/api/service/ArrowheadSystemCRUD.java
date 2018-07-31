/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.common.api.service;

import eu.arrowhead.common.api.repository.ArrowheadSystemRepo;
import eu.arrowhead.common.dto.entity.ArrowheadSystem;
import eu.arrowhead.common.exception.EntityNotFoundException;
import java.util.List;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ArrowheadSystemCRUD {

  private final ArrowheadSystemRepo systemRepo;

  @Autowired
  public ArrowheadSystemCRUD(ArrowheadSystemRepo systemRepo) {
    this.systemRepo = systemRepo;
  }

  public List<ArrowheadSystem> getAllSystems(Pageable pageable, String name) {
    if (name != null) {
      return systemRepo.findByName(name);
    }
    return systemRepo.findAll(pageable).getContent();
  }

  public ArrowheadSystem getSystemById(long systemId) {
    return systemRepo.findById(systemId).orElseThrow(() -> new EntityNotFoundException("ArrowheadSystem entity with id " + systemId + " not found"));
  }

  public ArrowheadSystem saveArrowheadSystem(ArrowheadSystem system) {
    return systemRepo.save(system);
  }

  public ArrowheadSystem updateArrowheadSystem(long systemId, ArrowheadSystem updatedSystem) {
    ArrowheadSystem savedSystem = systemRepo.findById(systemId).orElseThrow(
        () -> new EntityNotFoundException("ArrowheadSystem entity with id " + systemId + " not found"));
    BeanUtils.copyProperties(updatedSystem, savedSystem, "id");
    return systemRepo.save(savedSystem);
  }

  public ResponseEntity<?> deleteArrowheadSystem(long systemId) {
    return systemRepo.findById(systemId).map(system -> {
      systemRepo.delete(system);
      return ResponseEntity.ok().build();
    }).orElseThrow(() -> new EntityNotFoundException("ArrowheadSystem entity with id " + systemId + " not found"));
  }
}
