/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.common.controller;

import eu.arrowhead.common.dto.entity.ArrowheadService;
import eu.arrowhead.common.service.ArrowheadServiceCRUD;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("services")
public class ArrowheadServiceController {

  private final ArrowheadServiceCRUD serviceLayer;

  @Autowired
  public ArrowheadServiceController(ArrowheadServiceCRUD serviceLayer) {
    this.serviceLayer = serviceLayer;
  }

  @GetMapping
  public List<ArrowheadService> getAllServices(Pageable pageable, @RequestParam(value = "definition", required = false) String definition) {
    return serviceLayer.getAllServices(pageable, definition);
  }

  @GetMapping("{serviceId}")
  public ArrowheadService getServiceById(@PathVariable long serviceId) {
    return serviceLayer.getServiceById(serviceId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ArrowheadService saveArrowheadService(@Valid @RequestBody ArrowheadService service) {
    return serviceLayer.saveArrowheadService(service);
  }

  @PutMapping("{serviceId}")
  public ArrowheadService updateArrowheadService(@PathVariable long serviceId, @Valid @RequestBody ArrowheadService updatedService) {
    return serviceLayer.updateArrowheadService(serviceId, updatedService);
  }

  @PatchMapping("{serviceId}")
  public ArrowheadService updateArrowheadServicePartially(@PathVariable long serviceId, @RequestBody ArrowheadService updatedService) {
    return serviceLayer.updateArrowheadServicePartially(serviceId, updatedService);
  }

  @DeleteMapping("{serviceId}")
  public ResponseEntity<?> deleteArrowheadService(@PathVariable long serviceId) {
    return serviceLayer.deleteArrowheadService(serviceId);
  }
}
