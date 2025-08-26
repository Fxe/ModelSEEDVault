package org.modelseeed.vault.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.modelseeed.vault.core.Neo4jNodeEntity;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.springframework.stereotype.Repository;

@Repository
public class GraphRepository {
  
  private final GraphDatabaseService db;
  
  public GraphRepository(GraphDatabaseService db) {
    this.db = db;
  }
  
  public Neo4jNodeEntity getNode(String elementID) {
    try (Transaction tx = db.beginTx()) {
      Node node = tx.getNodeByElementId(elementID);
      return new Neo4jNodeEntity(node);
    }
  }
  
  public Neo4jNodeEntity getNode(String entry, String label) {
    try (Transaction tx = db.beginTx()) {
      Node node = tx.findNode(Label.label(label), "key", entry);
      return new Neo4jNodeEntity(node);
    }
  }
  
  public Neo4jNodeEntity getNode(Neo4jNodeEntity e) {
    try (Transaction tx = db.beginTx()) {
      if (e.getElementId() != null) {
        return this.getNode(e.getElementId());
      } else {
        return this.getNode(e.getEntry(), e.getType());
      }
    }
  }
  
  public Neo4jNodeEntity addNode(Neo4jNodeEntity node) {
    try (Transaction tx = db.beginTx()) {
      Node newNode = tx.createNode(node.getLabel());
      newNode.setProperty("key", node.getEntry());
      newNode.setProperty("_created_at", System.currentTimeMillis());
      newNode.setProperty("_updated_at", System.currentTimeMillis());
      for (String label: node.getLabels()) {
        newNode.addLabel(Label.label(label));        
      }
      
      //for (String k: node.get)
      Neo4jNodeEntity res = new Neo4jNodeEntity(newNode);
      res.setType(node.getType());
      tx.commit();
      return res;
    }
  }
  
  public void addNode(String entry, String label, Map<String, Object> properties) {
    try (Transaction tx = db.beginTx()) {
      Node node = tx.createNode(Label.label(label));
      node.setProperty("key", entry);
      node.setProperty("_created_at", System.currentTimeMillis());
      node.setProperty("_updated_at", System.currentTimeMillis());
      if (properties != null) {
        for (Map.Entry<String, Object> property : properties.entrySet()) {
          node.setProperty(property.getKey(), property.getValue());
        }  
      }
      tx.commit();
    }
  }
  
  public void registerEntity(String label) {
    try (Transaction tx = db.beginTx()) {
      String query = "CREATE CONSTRAINT key_" + label + " FOR (n:" + label + ") REQUIRE n.key IS UNIQUE";
      tx.execute(query);
      tx.commit();
    }
  }

  public List<Map<String, Object>> getUniqueConstraint() {
    try (Transaction tx = db.beginTx()) {
      String query = "SHOW CONSTRAINTS YIELD *";
      Result result = tx.execute(query);
      List<Map<String, Object>> res = new ArrayList<>();
      while (result.hasNext()) {
        Map<String, Object> o = result.next();
        res.add(o);
      }
      return res;
    }
  }
  
  public void addEdge(Neo4jNodeEntity v1, Neo4jNodeEntity v2, String type) {
    try (Transaction tx = db.beginTx()) {
      Node n1 = v1.getNode();
      if (n1 == null) {
        n1 = this.getNode(v1).getNode();
      }
      
      Node n2 = v2.getNode();
      if (n2 == null) {
        n2 = this.getNode(v2).getNode();
      }
      
      Relationship r = n1.createRelationshipTo(n2, RelationshipType.withName(type));
      r.setProperty("_created_at", System.currentTimeMillis());
      r.setProperty("_updated_at", System.currentTimeMillis());
    }
  }


}
