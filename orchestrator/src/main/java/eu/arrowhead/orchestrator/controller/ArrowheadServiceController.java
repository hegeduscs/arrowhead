/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.orchestrator.controller;

import eu.arrowhead.common.dto.entity.ArrowheadService;
import eu.arrowhead.common.repository.ArrowheadServiceRepo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("test")
public class ArrowheadServiceController {

  private final ArrowheadServiceRepo serviceRepo;

  @Autowired
  public ArrowheadServiceController(ArrowheadServiceRepo serviceRepo) {
    this.serviceRepo = serviceRepo;
  }

  @GetMapping("auto")
  public String test() {
    Set<String> interfaces = new HashSet<>(Arrays.asList("a123sd", "asdas2121"));
    Map<String, String> metadata = new HashMap<>();
    metadata.put("sajt", "bokecsker");

    ArrowheadService service = new ArrowheadService("theService", interfaces, metadata);
    service.setId(1);

    List<ArrowheadService> list = new ArrayList<>();
    list = serviceRepo.findByDefinitionAndInterfacesIn("theService", interfaces);

    serviceRepo.save(service);
    if (list.isEmpty()) {
      return "200";
    } else {
      return "500";
    }
  }
}
