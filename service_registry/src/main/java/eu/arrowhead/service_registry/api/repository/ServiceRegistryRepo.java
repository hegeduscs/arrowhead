/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.service_registry.api.repository;

import eu.arrowhead.common.dto.entity.ArrowheadService;
import eu.arrowhead.common.dto.entity.ArrowheadSystem;
import eu.arrowhead.common.dto.entity.ServiceRegistryEntry;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRegistryRepo extends JpaRepository<ServiceRegistryEntry, Long> {

  //TODO test needed here (does it use equals? what happens if only bare minimum is provided, is it always 1 entry at max?)
  Optional<ServiceRegistryEntry> findByServiceAndProvider(ArrowheadService service, ArrowheadSystem provider);

  List<ServiceRegistryEntry> findByService_DefinitionAndProvider_Name(String serviceDef, String systemName);
}
