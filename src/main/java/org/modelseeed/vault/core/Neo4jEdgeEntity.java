package org.modelseeed.vault.core;

import java.util.Map;

import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

public class Neo4jEdgeEntity {
  
  private Relationship relationship;
  private final RelationshipType label;
  private String uuid;
  
  private final Neo4jNodeReference src;
  private final Neo4jNodeReference dst;
  
  private Map<String, Object> properties;
  
  public Neo4jEdgeEntity(Neo4jNodeReference src, Neo4jNodeReference dst, 
                         RelationshipType label, Map<String, Object> properties) {
    this.dst = dst;
    this.src = src;
    this.label = label;
    this.properties = properties;
  }
  
  public Neo4jEdgeEntity(String srcElementId, String dstElementId, 
      String label, Map<String, Object> properties) {
    this(new Neo4jNodeReference(null, null, srcElementId), 
         new Neo4jNodeReference(null, null, dstElementId), 
         RelationshipType.withName(label), properties);
  }
  
  public Neo4jEdgeEntity(Relationship relationship) {
    this(Neo4jNodeReference.fromNode(relationship.getStartNode()), 
         Neo4jNodeReference.fromNode(relationship.getEndNode()), 
         relationship.getType(), relationship.getAllProperties());
    this.uuid = relationship.getElementId();
  }

  public Neo4jNodeReference getSrc() {
    return src;
  }

  public Neo4jNodeReference getDst() {
    return dst;
  }

  public Relationship getRelationship() {
    return relationship;
  }

  public void setRelationship(Relationship relationship) {
    this.relationship = relationship;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public RelationshipType getLabel() {
    return label;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, Object> properties) {
    this.properties = properties;
  }
}
