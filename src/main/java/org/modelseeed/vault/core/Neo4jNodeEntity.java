package org.modelseeed.vault.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Neo4jNodeEntity extends AbstractEntity {

  private Label label;
  private Node node;
  
  public Neo4jNodeEntity(String entry, String type) {
    super(entry, type);
    this.label = Label.label(this.getType());
    if (this.getEntry().length() > 4000) {
      throw new IllegalArgumentException("key exceeds 4000 length");
    }
  }
  
  public Neo4jNodeEntity(Node node) {
    super(node.getProperty("key").toString(), "");
    this.node = node;
    if (this.getEntry().length() > 4000) {
      throw new IllegalArgumentException("key exceeds 4000 length");
    }
    
    Set<String> labels = new HashSet<>();
    for (Label label: this.node.getLabels()) {
      labels.add(label.name());
    }
    this.setLabels(labels);
    
    
    for (String k: node.getPropertyKeys()) {
      this.addProperty(k, node.getProperty(k));
    }
  }
  
  public Neo4jNodeEntity(String key, String type, Map<String, Object> properties) {
    this(key, type);
    for (String k: properties.keySet()) {
      this.addProperty(k, properties.get(k));
    } 
  }

  @Override
  public void setType(String type) {
    super.setType(type);
    this.label = Label.label(this.getType());
  }
  
  public Long getCreatedAt() {
    Object v = this.properties.get("_created_at");
    if (v != null) {
      return (long) v;
    } else {
      return null;
    }
  }
  
  public Long getUpdatedAt() {
    Object v = this.properties.get("_updated_at");
    if (v != null) {
      return (long) v;
    } else {
      return null;
    }
  }
  
  @Override
  public Map<String, Object> getProperties() {
    Map<String, Object> filter = new HashMap<>();
    for (Entry<String, Object> e: this.properties.entrySet()) {
      String key = e.getKey();
      if (key.equals("key") || key.equals("_created_at") || key.equals("_updated_at")) {
        System.out.println("skip " + key);
      } else {
        System.out.println("adding propertey: " + key + " " + e.getValue());
        filter.put(key, e.getValue());
      }
    }
    return filter;
  }
  
  public Label getLabel() {
    return this.label;
  }
  
  @JsonIgnore
  public Node getNode() {
    return this.node;
  }

  public String getElementId() {
    if (node == null) {
      return null;
    }
    return node.getElementId();
  }
}
