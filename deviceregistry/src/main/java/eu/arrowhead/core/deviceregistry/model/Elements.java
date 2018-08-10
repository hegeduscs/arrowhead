package eu.arrowhead.core.deviceregistry.model;

import java.util.ArrayList;

public class Elements<T> {

  private ArrayList<T> elements;

  public Elements() {
    elements = new ArrayList<T>();
  }

  public void addElement(T identity) throws Exception {
    this.elements.add(identity);
  }

  public void removeElement(T identity) throws Exception {
    this.elements.remove(identity);
  }

  public ArrayList<T> getElements() {
    return this.elements;
  }

  public String toString() {
    String r = "";

    for (T identity : elements) {
      r = r + identity.toString();
    }

    return r;
  }
}
