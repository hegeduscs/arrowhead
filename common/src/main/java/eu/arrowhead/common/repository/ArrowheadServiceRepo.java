/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.common.repository;

import eu.arrowhead.common.dto.entity.ArrowheadService;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for the {@link eu.arrowhead.common.dto.entity.ArrowheadService} class.
 *
 * @author uzoltan
 * @since 4.2
 */
@Repository
public interface ArrowheadServiceRepo extends JpaRepository<ArrowheadService, Long> {

  /**
   * Find all services with the given name.
   *
   * @param definition The name of the service
   * @return List of services with the given name
   */
  List<ArrowheadService> findByDefinition(String definition);
}
