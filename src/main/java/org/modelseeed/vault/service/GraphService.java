package org.modelseeed.vault.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelseeed.vault.core.Neo4jEdgeEntity;
import org.modelseeed.vault.core.Neo4jNodeEntity;
import org.modelseeed.vault.core.Neo4jNodeReference;
import org.modelseeed.vault.dto.DataTablesResponse;
import org.modelseeed.vault.dto.NodePageRequest;
import org.modelseeed.vault.repository.GraphRepository;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.springframework.stereotype.Service;

@Service
public class GraphService {

    private final GraphRepository graphRepository;

    public GraphService(GraphRepository graphRepository) {
        this.graphRepository = graphRepository;
    }
    
    public void registerNode(String type) {
      try (Transaction tx = this.graphRepository.beginTx()) {
        this.graphRepository.registerEntity(type, tx);
        
        tx.commit();
      }
    }
    
    public Map<Set<String>, Integer> countNodes() {
      try (Transaction tx = this.graphRepository.beginTx()) {
        return this.graphRepository.countNodes(tx);
      }
    }
    
    public Map<String, Integer> countRelationships() {
      try (Transaction tx = this.graphRepository.beginTx()) {
        return this.graphRepository.countRelationships(tx);
      }
    }
    
    public List<Map<String, Object>> listConstraints() {
      try (Transaction tx = this.graphRepository.beginTx()) {
        return this.graphRepository.getUniqueConstraint(tx);
      }
    }
    
    public List<List<Object>> getChilds(Node node, String relType) {
      try (Transaction tx = this.graphRepository.beginTx()) {
      RelationshipType relationshipType = null;
      if (relType != null) {
        relationshipType = RelationshipType.withName(relType);
      }
      return this.graphRepository.getChilds(node, relationshipType, tx);
      }
      
    }
    
    public List<List<Object>> getParents(Node node, String relType) {
      try (Transaction tx = this.graphRepository.beginTx()) {
      RelationshipType relationshipType = null;
      if (relType != null) {
        relationshipType = RelationshipType.withName(relType);
      }
      return this.graphRepository.getParents(node, relationshipType, tx);
      }
  }

    public Neo4jNodeEntity addNode(String type, String key, List<String> labels, Map<String, Object> properties) {
      try (Transaction tx = this.graphRepository.beginTx()) {
      if (properties == null) {
        properties = new HashMap<>();
      }
      //System.out.println(properties);
      Neo4jNodeEntity node = new Neo4jNodeEntity(key, type, properties);
      Neo4jNodeEntity ret = graphRepository.addNode(node, tx);
      
      tx.commit();
      return ret;
      }
    }
    
    public List<Neo4jNodeEntity> addNodes(Iterable<Neo4jNodeEntity> nodes) {
      try (Transaction tx = this.graphRepository.beginTx()) {
        List<Neo4jNodeEntity> result = new ArrayList<>();
        for (Neo4jNodeEntity node: nodes) {
          Neo4jNodeEntity ret = graphRepository.addNode(node, tx);
          result.add(ret);
        }
        tx.commit();
        return result;
      }
    }
    
    public Map<String, Map<String, Object>> getNodeRelationships(String eId, Direction direction, Integer limit) {
      try (Transaction tx = this.graphRepository.beginTx()) {
      Neo4jNodeEntity node = this.graphRepository.getNode(eId, tx);
      System.out.println(node);
      return this.graphRepository.getNodeRelationships(node, direction, limit);
      }
    }
    
    public List<Neo4jNodeEntity> listNodeByType(String type, Integer limit) {
      try (Transaction tx = this.graphRepository.beginTx()) {
      if (limit != null) {
        return this.graphRepository.listNodeByLabel(Label.label(type), limit, tx);
      } else {
        return this.graphRepository.listNodeByLabel(Label.label(type), 1000000, tx);        
      }
      }
    }
    
    public Neo4jNodeEntity getNode(String key, String type) {
      try (Transaction tx = this.graphRepository.beginTx()) {
      return this.graphRepository.getNode(key, type, tx);
      }
    }
    
    public List<Neo4jNodeReference> getNodeEids(List<Neo4jNodeReference> listReferences) {
      try (Transaction tx = this.graphRepository.beginTx()) {
        List<Neo4jNodeReference> result = listReferences.stream().map(
            r -> new Neo4jNodeReference(
                r.key(), 
                r.type(), 
                this.graphRepository.getElementIdFromKeyLabel(r.key(), r.type(), tx)
                )).toList();
        /**
                .collect(Collectors.toMap(
                    p -> p, // key = the original pair
                    p -> this.graphRepository.getElementIdFromKeyLabel(
                            p.get(0),
                            Label.label(p.get(1)),
                            tx)
                ));
**/
        return result;
      }
    }
    
    public Neo4jNodeEntity getNode(String elementId) {
      try (Transaction tx = this.graphRepository.beginTx()) {
      return this.graphRepository.getNode(elementId, tx);
      }
    }
    
    public String addEdge(String srcElementId, String dstElementId, String type, Map<String, Object> properties) {
      try (Transaction tx = this.graphRepository.beginTx()) {
        String eid = this.graphRepository.addEdge(srcElementId, dstElementId, type, properties, tx);
        tx.commit();
      return eid;
      }
    }
    
    public Map<List<String>, String> addEdges(Iterable<Neo4jEdgeEntity> edges) {
      Map<List<String>, String> res = new HashMap<>();
      try (Transaction tx = this.graphRepository.beginTx()) {
        for (Neo4jEdgeEntity edge: edges) {
          String eid = this.graphRepository.addEdge(edge.getSrc().elementId(), 
                                                    edge.getDst().elementId(),
                                                    edge.getLabel().name(),
                                                    edge.getProperties(),
                                                    tx);
          List<String> l = List.of(edge.getSrc().elementId(), 
                                   edge.getLabel().name(), 
                                   edge.getDst().elementId());
          res.put(l, eid);
        }
        tx.commit();
      }
      return res;
    }

    public void addEdge(String type, Map<String, Object> data) {
        //graphRepository.addEdge(type, data);
    }
    
    public void addContrainst() {
      
    }
    
    /**
     * Get paginated nodes compatible with DataTables server-side processing
     * @param request NodePageRequest containing pagination parameters
     * @return DataTablesResponse with paginated node data
     */
    public DataTablesResponse<Neo4jNodeEntity> pageNodes(NodePageRequest request) {
      
      try (Transaction tx = this.graphRepository.beginTx()) {
            // Get total count without filtering
            long totalRecords = this.graphRepository.countNodes(null, request.getNodeType(), tx);
            
            // Get filtered count
            long filteredRecords = this.graphRepository.countNodes(request.getSearchValue(), request.getNodeType(), tx);
            
            // Get paginated data
            List<Neo4jNodeEntity> nodes = this.graphRepository.getPagedNodes(
                request.getStart(),
                request.getLength(),
                request.getSearchValue(),
                request.getSortColumn(),
                request.getSortDirection(),
                request.getNodeType(),
                tx
            );
            
            return new DataTablesResponse<>(
                request.getDraw(),
                totalRecords,
                filteredRecords,
                nodes
            );
            
        } catch (Exception e) {
            return new DataTablesResponse<>(request.getDraw(), "Error retrieving nodes: " + e.getMessage());
        }
    }
    
}
