package eu.arrowhead.common.model.messages;

import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class FilterQoSReservations {

  private Map<String, String> filters;

  public FilterQoSReservations() {
    super();
    // TODO Auto-generated constructor stub
  }

  public FilterQoSReservations(Map<String, String> filters) {
    super();
    this.filters = filters;
  }

  public Map<String, String> getFilters() {
    return filters;
  }

  public void setFilters(Map<String, String> filters) {
    this.filters = filters;
  }

}
