/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

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
