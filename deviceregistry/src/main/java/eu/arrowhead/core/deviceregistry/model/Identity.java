package eu.arrowhead.core.deviceregistry.model;

public class Identity implements Comparable<Identity> {

  protected String id;

  public Identity() {

  }

  public Identity(String id) {
    this.id = id;
  }

  public String getId() {
    return this.id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public int compareTo(Identity o) {
    // TODO Auto-generated method stub
    return 0;
  }
}
