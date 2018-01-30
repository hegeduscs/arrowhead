package eu.arrowhead.common.json.support;

import eu.arrowhead.common.json.support.BooleanMapAdapter.MapLike;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class BooleanMapAdapter extends XmlAdapter<MapLike, Map<String, Boolean>> {

  public static class Entry {

    public String key;
    public Boolean value;

    public Entry() {
    }

    public Entry(String key, Boolean value) {
      this.key = key;
      this.value = value;
    }
  }

  public static class MapLike {

    public List<Entry> entry;
  }

  @Override
  public Map<String, Boolean> unmarshal(MapLike v) {
    Map<String, Boolean> map = new HashMap<>();
    for (Entry entry : v.entry) {
      map.put(entry.key, entry.value);
    }
    return map;
  }

  @Override
  public MapLike marshal(Map<String, Boolean> v) {
    MapLike mapLike = new MapLike();
    mapLike.entry = new ArrayList<>();
    for (Map.Entry<String, Boolean> entry : v.entrySet()) {
      mapLike.entry.add(new Entry(entry.getKey(), entry.getValue()));
    }
    return mapLike;
  }
}
