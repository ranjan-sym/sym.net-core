package net.symplifier.core.util.attribute;

import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by ranjan on 6/14/15.
 */
public class AttributeGroup {

  private Map<String, Attribute> attributes = new LinkedHashMap<String, Attribute>();

  public void addAttribute(String name, Attribute attribute) {
    assert(!attributes.containsKey(name));
    attributes.put(name, attribute);
  }

  public Attribute getAttribute(String name) {
    return attributes.get(name);
  }

  public Set<String> getAttributes() {
    return attributes.keySet();
  }

  JSONObject getJSON() {
    JSONObject obj = new JSONObject();
    for(Map.Entry<String, Attribute> entry:attributes.entrySet()) {
      obj.put(entry.getKey(), entry.getValue());
    }
    return obj;
  }

  void setJSON(JSONObject obj) {
    for(Map.Entry<String, Attribute> entry:attributes.entrySet()) {
      entry.getValue().set((String) obj.optString(entry.getKey()));
    }
  }

  void clearAll() {
    for(Map.Entry<String, Attribute> entry:attributes.entrySet()) {
      entry.getValue().set((Object)null);
    }
  }

}
