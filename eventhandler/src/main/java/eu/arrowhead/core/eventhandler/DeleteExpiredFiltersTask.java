/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.core.eventhandler;

import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.database.EventFilter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.TimerTask;
import org.apache.log4j.Logger;

public class DeleteExpiredFiltersTask extends TimerTask {

  private static final DatabaseManager dm = DatabaseManager.getInstance();
  private static final Logger log = Logger.getLogger(DeleteExpiredFiltersTask.class.getName());

  @Override
  public void run() {
    List<EventFilter> filterList = dm.getAll(EventFilter.class, null);
    for (EventFilter filter : filterList) {
      if (filter.getEndDate() != null && filter.getEndDate().isBefore(LocalDateTime.now())) {
        dm.delete(filter);
        log.debug(filter.toString() + " removed do to expired end date.");
      }
    }
  }

}
