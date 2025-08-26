package org.modelseeed.vault.core;

import java.util.Map;

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
