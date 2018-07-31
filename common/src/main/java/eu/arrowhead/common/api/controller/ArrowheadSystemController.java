/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.common.api.controller;

import eu.arrowhead.common.api.service.ArrowheadSystemCRUD;
import eu.arrowhead.common.dto.entity.ArrowheadSystem;
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
@RequestMapping("systems")
public class ArrowheadSystemController {

  private final ArrowheadSystemCRUD serviceLayer;

  @Autowired
  public ArrowheadSystemController(ArrowheadSystemCRUD serviceLayer) {
    this.serviceLayer = serviceLayer;
  }

  @GetMapping
  public List<ArrowheadSystem> getAllSystems(Pageable pageable, @RequestParam(value = "name", required = false) String name) {
    return serviceLayer.getAllSystems(pageable, name);
  }

  @GetMapping("{systemId}")
  public ArrowheadSystem getSystemById(@PathVariable long systemId) {
    return serviceLayer.getSystemById(systemId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ArrowheadSystem saveArrowheadSystem(@Valid @RequestBody ArrowheadSystem system) {
    return serviceLayer.saveArrowheadSystem(system);
  }

  @PutMapping("{systemId}")
  public ArrowheadSystem updateArrowheadSystem(@PathVariable long systemId, @Valid @RequestBody ArrowheadSystem updatedSystem) {
    return serviceLayer.updateArrowheadSystem(systemId, updatedSystem);
  }

  @DeleteMapping("{systemId}")
  public ResponseEntity<?> deleteArrowheadSystem(@PathVariable long systemId) {
    return serviceLayer.deleteArrowheadSystem(systemId);
  }
}
