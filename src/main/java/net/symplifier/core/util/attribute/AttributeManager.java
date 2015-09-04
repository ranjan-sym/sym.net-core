package net.symplifier.core.util.attribute;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by ranjan on 6/14/15.
 */
public class AttributeManager {
  private Map<String, AttributeGroup> allAttributes = new LinkedHashMap<>();

  public void add(String group, String name, Attribute attribute) {
    AttributeGroup g = allAttributes.get(group);
    if (g == null) {
      g = new AttributeGroup();
      allAttributes.put(group, g);
    }

    g.addAttribute(name, attribute);
  }

  public Attribute getAttribute(String group, String name) {
    AttributeGroup g = allAttributes.get(group);
    if (g == null) {
      return null;
    }

    return g.getAttribute(name);
  }

  public String serialize() {
    JSONObject obj = new JSONObject();

    for(Map.Entry<String, AttributeGroup> entry:allAttributes.entrySet()) {
      obj.put(entry.getKey(), entry.getValue().getJSON());
    }

    return obj.toString();
  }

  public void deserialize(String json) {
    JSONTokener tokener = new JSONTokener(json);
    JSONObject object = new JSONObject(tokener);

    for(Map.Entry<String, AttributeGroup> entry:allAttributes.entrySet()) {
      JSONObject grp = object.optJSONObject(entry.getKey());
      if (grp == null) {
        entry.getValue().clearAll();
      } else {
        entry.getValue().setJSON(grp);
      }
    }


  }

}
