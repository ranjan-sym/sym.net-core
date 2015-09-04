package net.symplifier.core.util.attribute;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by ranjan on 6/14/15.
 */
public abstract class Attribute<T> {
  protected T value;

  Map<java.lang.String, T> options;

  public Attribute(HasAttribute owner, java.lang.String group, java.lang.String name, T value, AttributeOptions<T> options) {
    owner.getAttributeManager().add(group, name, this);
  }

  public T get() {
    return value;
  }

  public void addOption(T value, java.lang.String text) {
    if(options == null) {
      options = new LinkedHashMap<>();
    }
    options.put(text, value);
  }

  public java.lang.String toString() {
    if (value == null) {
      return null;
    }
    return value.toString();
  }

  public Attribute<T> setOption(String option) {
    if (this.options != null && options.containsKey(option)) {
      value = options.get(option);
    } else {
      value = null;
    }

    return this;
  }

  public Attribute<T> set(T value) {
    this.value = value;
    return this;
  }

  public final Attribute<T> setFromString(java.lang.String value) {
    if (value == null) {
      this.value = null;
    }
    this.value = parse(value);
    return this;
  }

  public final Attribute<T> set(Attribute other) {
    this.value = parse(other.toString());
    return this;
  }


  public abstract T parse(java.lang.String value);


  public static class String extends Attribute<java.lang.String> {
    public String(HasAttribute owner, java.lang.String group, java.lang.String name, java.lang.String value, AttributeOptions<java.lang.String> options) {
      super(owner, group, name, value, options);
    }

    @Override
    public java.lang.String parse(java.lang.String value) {
      return value;
    }
  }

  public static class Integer extends Attribute<java.lang.Integer> {


    public Integer(HasAttribute owner, java.lang.String group, java.lang.String name, java.lang.Integer value, AttributeOptions<java.lang.Integer> options) {
      super(owner, group, name, value, options);
    }

    @Override
    public java.lang.Integer parse(java.lang.String value) {
      return java.lang.Integer.parseInt(value);
    }
  }



}
