package eu.arrowhead.common.model.messages;

import java.util.HashMap;
import java.util.Map;

public class FilterQoSReservations {

  private Map<String, String> filters = new HashMap<>();

  public FilterQoSReservations() {
  }

  public FilterQoSReservations(Map<String, String> filters) {
    this.filters = filters;
  }

  public Map<String, String> getFilters() {
    return filters;
  }

  public void setFilters(Map<String, String> filters) {
    this.filters = filters;
  }

}
