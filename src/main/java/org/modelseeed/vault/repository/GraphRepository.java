package org.modelseeed.vault.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.modelseeed.vault.core.Neo4jNodeEntity;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.springframework.stereotype.Repository;

@Repository
public class GraphRepository {
  
  protected final GraphDatabaseService db;
  
  public GraphRepository(GraphDatabaseService db) {
    this.db = db;
  }
  
  public Transaction beginTx() {
    return this.db.beginTx();
  }
  
  public Map<Set<String>, Integer> count(Transaction tx) {
    Map<Set<String>, Integer> count = new HashMap<>();
    for (Node node: tx.getAllNodes()) {
      Set<String> labels = new HashSet<>();
      for (Label label: node.getLabels()) {
        labels.add(label.name());
      }
      Integer c = count.get(labels);
      if (c == null) {
        count.put(labels, 1);
      } else {
        count.put(labels, c + 1);
      }
    }
    return count;
  }
  
  public Neo4jNodeEntity getNode(String elementID, Transaction tx) {
      Node node = tx.getNodeByElementId(elementID);
      return new Neo4jNodeEntity(node);
  }
  
  public Neo4jNodeEntity getNode(String entry, String label, Transaction tx) {
    Node node = tx.findNode(Label.label(label), "key", entry);
    if (node == null) {
      return null;
    }
    return new Neo4jNodeEntity(node);
  }
  
  public Neo4jNodeEntity getNode(Neo4jNodeEntity e, Transaction tx) {
    if (e.getElementId() != null) {
      return this.getNode(e.getElementId(), tx);
    } else {
      return this.getNode(e.getEntry(), e.getType(), tx);
    }
  }
  
  public List<Neo4jNodeEntity> listNodeByLabel(Label label, int limit, Transaction tx) {
    List<Neo4jNodeEntity> res = new ArrayList<>();
    Iterator<Node> it = tx.findNodes(label);
    while (it.hasNext() && res.size() < limit) {
      res.add(new Neo4jNodeEntity(it.next()));
    }
    return res;
  }
  
  public Map<String, Map<String, Object>> getNodeRelationships(Neo4jNodeEntity node, Direction direction, Integer limit) {
    Map<String, Map<String, Object>> res = new HashMap<>();
    for (Relationship r: node.getNode().getRelationships(direction)) {
      Map<String, Object> props = Map.copyOf(r.getAllProperties());
      props.put("relationship_type", r.getType().toString());
      res.put(r.getElementId(), props);
      if (limit != null && res.size() >= limit) {
        break;
      }
    }
    
    return res;
  }
  
  public Neo4jNodeEntity addNode(Neo4jNodeEntity node, Transaction tx) {
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
    return res;
  }
  
  public void addNode(String entry, String label, Map<String, Object> properties, Transaction tx) {
    System.out.println("addNode GraphRepo " + properties);
    Node node = tx.createNode(Label.label(label));
    node.setProperty("key", entry);
    node.setProperty("_created_at", System.currentTimeMillis());
    node.setProperty("_updated_at", System.currentTimeMillis());
    if (properties != null) {
      for (Map.Entry<String, Object> property : properties.entrySet()) {
        node.setProperty(property.getKey(), property.getValue());
      }  
    }
  }
  
  public void registerEntity(String label, Transaction tx) {
    String query = "CREATE CONSTRAINT key_" + label + " FOR (n:" + label + ") REQUIRE n.key IS UNIQUE";
    tx.execute(query);
  }

  public List<Map<String, Object>> getUniqueConstraint(Transaction tx) {
    String query = "SHOW CONSTRAINTS YIELD *";
    Result result = tx.execute(query);
    List<Map<String, Object>> res = new ArrayList<>();
    while (result.hasNext()) {
      Map<String, Object> o = result.next();
      res.add(o);
    }
    return res;
  }
  
  public String addEdge(String srcElementId, String dstElementId, String type, Map<String, Object> properties, Transaction tx) {
    //System.out.println(srcElementId);
    //System.out.println(dstElementId);
    //System.out.println(type);
    
    Neo4jNodeEntity src = this.getNode(srcElementId, tx);
    Neo4jNodeEntity dst = this.getNode(dstElementId, tx);
    
    Node nSrc = tx.getNodeByElementId(src.getElementId());
    Node nDst = tx.getNodeByElementId(dst.getElementId());
    //System.out.println(nSrc);
    //System.out.println(nDst);
    RelationshipType rType = RelationshipType.withName(type);
    Relationship r = nSrc.createRelationshipTo(nDst, rType);
    
    if (properties != null) {
      for (Map.Entry<String, Object> property : properties.entrySet()) {
        r.setProperty(property.getKey(), property.getValue());
      }
    }
    //nSrc.getRelationships(Direction.OUTGOING, rType);
    
    r.setProperty("_created_at", System.currentTimeMillis());
    r.setProperty("_updated_at", System.currentTimeMillis());
    
    String eId = r.getElementId();
    //System.out.println(r);
    //System.out.println(eId);

    return eId;    
  }
  
  public List<List<Object>> getChilds(Node node, RelationshipType relationshipType, Transaction tx) {
    return this.getConnectedNodes(node.getElementId(), Direction.OUTGOING, relationshipType, tx);
  }
  
  public List<List<Object>> getParents(Node node, RelationshipType relationshipType, Transaction tx) {

    return this.getConnectedNodes(node.getElementId(), Direction.INCOMING, relationshipType, tx);
}
  
  public List<List<Object>> getConnectedNodes(String eId, Direction dir, RelationshipType type, Transaction tx) {
    //Map<Object, Neo4jNodeEntity> res = new HashMap<>();
    //System.out.println(eId + " " + dir + " " + type);
    List<List<Object>> res = new ArrayList<>();

    Node node = tx.getNodeByElementId(eId);
    ResourceIterable<Relationship> it = null;
    if (type != null) {
      it = node.getRelationships(dir, type);
    } else {
      it = node.getRelationships(dir);
    }
        
    for (Relationship r: it) {
        String rElemId = r.getElementId();
        String rType = r.getType().name();
        Map<String, Object> props = r.getAllProperties();
        Map<String, Object> rData = new HashMap<>();
        rData.put("elementId", rElemId);
        rData.put("t", rType);
        rData.put("properties", props);
        Neo4jNodeEntity other = new Neo4jNodeEntity(r.getOtherNode(node));
        //sTuple2<Object, Neo4jNodeEntity> e = new Tuple2<>(rData, other);
        res.add(List.of(rData, other));
    }
    
    
    return res;
  }
  
  public void addEdge(Neo4jNodeEntity v1, Neo4jNodeEntity v2, String type, Transaction tx) {

      Node n1 = v1.getNode();
      if (n1 == null) {
        n1 = this.getNode(v1, tx).getNode();
      }
      
      Node n2 = v2.getNode();
      if (n2 == null) {
        n2 = this.getNode(v2, tx).getNode();
      }
      
      Relationship r = n1.createRelationshipTo(n2, RelationshipType.withName(type));
      r.setProperty("_created_at", System.currentTimeMillis());
      r.setProperty("_updated_at", System.currentTimeMillis());
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
                                            String sortColumn, String sortDirection, String nodeType, Transaction tx) {

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
  
  /**
   * Count total nodes in the database with optional filtering
   * @param searchValue Optional search term for filtering
   * @param nodeType Optional node type/label filter
   * @return Total count of matching nodes
   */
  public long countNodes(String searchValue, String nodeType, Transaction tx) {
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
