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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
//@RequestMapping("test")
public class ArrowheadServiceController {

  private final ArrowheadServiceRepo serviceRepo;

  @Autowired
  public ArrowheadServiceController(ArrowheadServiceRepo serviceRepo) {
    this.serviceRepo = serviceRepo;
  }

  @GetMapping("auto")
  public String test() {
    Set<String> interfaces = new HashSet<>(Arrays.asList("asd", "asdas21"));
    Map<String, String> metadata = new HashMap<>();
    metadata.put("sajt", "bor");
    serviceRepo.save(new ArrowheadService("theService", interfaces, metadata));
    return LocalDateTime.now().toString();
  }
}
