package org.modelseeed.vault.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractEntity {
  
  protected String entry;
  protected String type;
  protected Set<String> labels;
  protected Map<String, Object> properties;
  
  public AbstractEntity(String key, String type) {
    if (key == null) {
      throw new IllegalArgumentException("key must not be null");
    }
    if (type == null) {
      throw new IllegalArgumentException("type must not be null");
    }
    this.entry = key;
    this.type = type;
    this.properties = new HashMap<>();
    this.labels = new HashSet<>();

  }

  public String getEntry() {
    return entry;
  }

  public void setEntry(String entry) {
    this.entry = entry;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Set<String> getLabels() {
    return labels;
  }

  public void setLabels(Set<String> labels) {
    this.labels = labels;
  }
  
  public void addProperty(String property, Object value) {
    if (property == "key") {
      throw new IllegalArgumentException("key is reserved - not allowed property");
    }
    this.properties.put(property, value);
  }
  
  public Map<String, Object> getProperties() {
    return new HashMap<>(this.properties);
  }
}
