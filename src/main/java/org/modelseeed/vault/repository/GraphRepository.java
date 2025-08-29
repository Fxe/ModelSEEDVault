package org.modelseeed.vault.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
      
      for (Entry<String, Object> e: node.getProperties().entrySet()) {
        newNode.setProperty(e.getKey(), e.getValue());
      }
      
      //for (String k: node.get)
      Neo4jNodeEntity res = new Neo4jNodeEntity(newNode);
      res.setType(node.getType());
      tx.commit();
      return res;
    }
  }
  
  public void addNode(String entry, String label, Map<String, Object> properties) {
    System.out.println("addNode GraphRepo " + properties);
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

  /**
   * Get paginated nodes from Neo4j database with optional filtering and sorting
   * @param start Starting index for pagination
   * @param length Number of records to return
   * @param searchValue Optional search term for filtering
   * @param sortColumn Column to sort by (default: key)
   * @param sortDirection Sort direction (ASC or DESC)
   * @param nodeType Optional node type/label filter
   * @return List of Neo4jNodeEntity objects
   */
  public List<Neo4jNodeEntity> getPagedNodes(int start, int length, String searchValue, 
                                            String sortColumn, String sortDirection, String nodeType) {
    try (Transaction tx = db.beginTx()) {
      StringBuilder cypher = new StringBuilder("MATCH (n");
      
      // Add label filter if specified
      if (nodeType != null && !nodeType.trim().isEmpty()) {
        cypher.append(":").append(nodeType);
      }
      
      cypher.append(") ");
      
      // Add search filter if specified
      if (searchValue != null && !searchValue.trim().isEmpty()) {
        cypher.append("WHERE toLower(toString(n.key)) CONTAINS toLower($searchValue) ");
      }
      
      cypher.append("RETURN n ");
      
      // Add sorting
      if (sortColumn != null && !sortColumn.trim().isEmpty()) {
        cypher.append("ORDER BY n.").append(sortColumn).append(" ");
        if ("DESC".equalsIgnoreCase(sortDirection)) {
          cypher.append("DESC ");
        } else {
          cypher.append("ASC ");
        }
      } else {
        cypher.append("ORDER BY n.key ASC ");
      }
      
      // Add pagination
      cypher.append("SKIP $skip LIMIT $limit");
      
      Map<String, Object> parameters = new HashMap<>();
      parameters.put("skip", start);
      parameters.put("limit", length);
      if (searchValue != null && !searchValue.trim().isEmpty()) {
        parameters.put("searchValue", searchValue.trim());
      }
      
      Result result = tx.execute(cypher.toString(), parameters);
      List<Neo4jNodeEntity> nodes = new ArrayList<>();
      
      while (result.hasNext()) {
        Map<String, Object> row = result.next();
        Node node = (Node) row.get("n");
        nodes.add(new Neo4jNodeEntity(node));
      }
      
      return nodes;
    }
  }
  
  /**
   * Count total nodes in the database with optional filtering
   * @param searchValue Optional search term for filtering
   * @param nodeType Optional node type/label filter
   * @return Total count of matching nodes
   */
  public long countNodes(String searchValue, String nodeType) {
    try (Transaction tx = db.beginTx()) {
      StringBuilder cypher = new StringBuilder("MATCH (n");
      
      // Add label filter if specified
      if (nodeType != null && !nodeType.trim().isEmpty()) {
        cypher.append(":").append(nodeType);
      }
      
      cypher.append(") ");
      
      // Add search filter if specified
      if (searchValue != null && !searchValue.trim().isEmpty()) {
        cypher.append("WHERE toLower(toString(n.key)) CONTAINS toLower($searchValue) ");
      }
      
      cypher.append("RETURN count(n) as total");
      
      Map<String, Object> parameters = new HashMap<>();
      if (searchValue != null && !searchValue.trim().isEmpty()) {
        parameters.put("searchValue", searchValue.trim());
      }
      
      Result result = tx.execute(cypher.toString(), parameters);
      if (result.hasNext()) {
        return (Long) result.next().get("total");
      }
      return 0;
    }
  }


}
