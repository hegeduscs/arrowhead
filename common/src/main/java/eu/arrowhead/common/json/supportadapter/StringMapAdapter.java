package eu.arrowhead.common.json.supportadapter;

import eu.arrowhead.common.json.supportadapter.StringMapAdapter.MapLike;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class StringMapAdapter extends XmlAdapter<MapLike, Map<String, String>> {

  public static class Entry {

    public String key;
    public String value;

    public Entry() {
    }

    public Entry(String key, String value) {
      this.key = key;
      this.value = value;
    }
  }

  public static class MapLike {

    public List<Entry> entry;
  }

  @Override
  public Map<String, String> unmarshal(MapLike v) {
    Map<String, String> map = new HashMap<>();
    for (Entry entry : v.entry) {
      map.put(entry.key, entry.value);
    }
    return map;
  }

  @Override
  public MapLike marshal(Map<String, String> v) {
    MapLike mapLike = new MapLike();
    mapLike.entry = new ArrayList<>();
    for (Map.Entry<String, String> entry : v.entrySet()) {
      mapLike.entry.add(new Entry(entry.getKey(), entry.getValue()));
    }
    return mapLike;
  }
}