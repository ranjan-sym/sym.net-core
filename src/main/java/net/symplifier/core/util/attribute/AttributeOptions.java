package net.symplifier.core.util.attribute;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by ranjan on 6/14/15.
 */
public class AttributeOptions<T> {
  private Map<String, T> options = new LinkedHashMap<>();

  public AttributeOptions<T> add(T value, String text) {
    assert(value != null && text != null);
    assert(!options.containsKey(text));

    options.put(text, value);
    return this;
  }

}
