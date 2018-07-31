/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.common.api.repository;

import eu.arrowhead.common.dto.entity.ArrowheadSystem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArrowheadSystemRepo extends JpaRepository<ArrowheadSystem, Long> {

  List<ArrowheadSystem> findByName(String name);

  List<ArrowheadSystem> findByAddress(String address);

  List<ArrowheadSystem> findByPortIsBetween(int min, int max);
}
