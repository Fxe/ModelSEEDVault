package org.modelseeed.vault.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.modelseeed.vault.core.Neo4jEdgeEntity;
import org.modelseeed.vault.core.Neo4jNodeEntity;
import org.modelseeed.vault.core.Neo4jNodeReference;
import org.modelseeed.vault.dto.DataTablesResponse;
import org.modelseeed.vault.dto.NodePageRequest;
import org.modelseeed.vault.service.GraphService;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Label;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/graph")
@CrossOrigin
public class GraphController {

    private GraphService graphService;

    public GraphController(GraphService graphService) {
        this.graphService = graphService;
    }
    
    @GetMapping("/node/count")
    public Map<Set<String>, Integer> countNodes() {
      return this.graphService.countNodes();
    }
    
    @GetMapping("/relationship/count")
    public Map<String, Integer> countRelationships() {
      return this.graphService.countRelationships();
    }
    
    @PostMapping("/node/constraint")
    public void registerNode(String type) {
      this.graphService.registerNode(type);
    }
    
    @GetMapping("/node/constraint")
    public Object getAllConstraints() {
      return this.graphService.listConstraints();
    }

    @PostMapping("/node/{type}/{id}")
    public Neo4jNodeEntity addNode(@PathVariable String type, @PathVariable String id, 
                          @RequestParam(required = false) List<String> labels,
                          @RequestBody(required = false) Map<String, Object> properties) {
      if (properties == null) {
        properties = new HashMap<>();
    }
      if (labels == null) {
        labels = new ArrayList<>();
      }
      if (labels.contains(type)) {
        throw new IllegalArgumentException("bad type/labels");
      }
      return this.graphService.addNode(type, id, labels, properties);
    }
    
    public record ParamBulkNode (
        String type, String id, List<String> labels,  Map<String, Object> properties
        ) {};
        
    public record ParamBulkEdge (
            String srcElementId, String dstElementId, String type,  Map<String, Object> properties
            ) {};
    
            
    @PostMapping("/bulk/nodes/get/elementId")
    public List<Neo4jNodeReference> getNodeEid(@RequestBody List<Neo4jNodeReference> references) {
      return this.graphService.getNodeEids(references);
    }
    
    @PostMapping("/bulk/nodes/get/elementIdPStream")
    public List<Neo4jNodeReference> getNodeEidPStream(@RequestBody List<Neo4jNodeReference> references) {
      return this.graphService.getNodeEidsPStream(references);
    }
    
    @PostMapping("/bulk/nodes")
    public Map<String, String> addBulkNodes(@RequestBody(required = false) List<ParamBulkNode> nodes) {
      Map<String, String> res = new HashMap<>();
      //find all nodes that exists
      for (ParamBulkNode node: nodes) {
        Neo4jNodeEntity dbNode = this.graphService.getNode(node.id, node.type);
        
        if (dbNode == null) {
          dbNode = this.graphService.addNode(node.type, node.id, node.labels, node.properties);
        }
        
        res.put(String.format("%s/%s", dbNode.getType(), dbNode.getEntry()), dbNode.getElementId());
      }
      //load all remaining nodes
      
      return res;
    }
    
    @PostMapping("/bulk/nodes2")
    public Map<String, String> addBulkNodes2(
        @RequestParam(defaultValue = "true") boolean safe,
        @RequestBody(required = false) List<ParamBulkNode> nodes) {
      if (nodes == null || nodes.isEmpty()) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Request body 'nodes' must not be null or empty"
            );
      }

      Map<String, String> res = new HashMap<>();
      
      Set<String> toAdd = new HashSet<>();
      if (safe) {
      //find all nodes that exists
        List<Neo4jNodeReference> pairs = nodes.stream()
            .map(node -> new Neo4jNodeReference(node.id, Label.label(node.type), null))
            .toList();
        List<Neo4jNodeReference> references = this.graphService.getNodeEids(pairs);
        for (Neo4jNodeReference ref: references) {
          String eid = ref.elementId();
          if (eid != null) {
            res.put(String.format("%s/%s", ref.type().name(), ref.key()), eid);
          } else {
            toAdd.add(String.format("%s/%s", ref.type().name(), ref.key()));
          }
        }
      }

      
      //add all nodes not in DB
      List<Neo4jNodeEntity> payload = new ArrayList<>();
      for (ParamBulkNode node: nodes) {
        String combined = String.format("%s/%s", node.type, node.id);
        if (!safe || toAdd.contains(combined)) {
          List<String> labels = node.labels;
          if (labels == null) {
            labels = new ArrayList<>();
          }
          Map<String, Object> properties = node.properties;
          if (properties == null) {
            properties = new HashMap<>();
          }
          payload.add(new Neo4jNodeEntity(node.id, Label.label(node.type), labels, properties));
        }
      }
      
      List<Neo4jNodeEntity> addedNodes = this.graphService.addNodes(payload);
      for (Neo4jNodeEntity node: addedNodes) {
        res.put(String.format("%s/%s", node.getType(), node.getEntry()), node.getElementId());
      }
      
      return res;
    }
    
    @PostMapping("/bulk/edges")
    public Map<List<String>, String> addBulkEdges(@RequestBody(required = false) List<ParamBulkEdge> edges) {
      Map<List<String>, String> res = new HashMap<>();
      for (ParamBulkEdge edge: edges) {
        String edgeElementId =  this.graphService.addEdge(edge.srcElementId, edge.dstElementId, 
            edge.type, edge.properties);
        List<String> l = List.of(edge.srcElementId, edge.type, edge.dstElementId);
        res.put(l, edgeElementId);
      }
      return res;
    }
    
    @PostMapping("/bulk/edges2")
    public Map<List<String>, String> addBulkEdges2(@RequestBody(required = false) List<ParamBulkEdge> edges) {
      
      List<Neo4jEdgeEntity> payload = new ArrayList<>();
      for (ParamBulkEdge edge: edges) {
        payload.add(new Neo4jEdgeEntity(edge.srcElementId, edge.dstElementId, edge.type, edge.properties));
      }
      //Map<List<String>, String> res = new HashMap<>();
      Map<List<String>, String> res = this.graphService.addEdges(payload);
      
      /**
      for (ParamBulkEdge edge: edges) {
        String edgeElementId =  this.graphService.addEdge(edge.srcElementId, edge.dstElementId, 
            edge.type, edge.properties);
        List<String> l = List.of(edge.srcElementId, edge.type, edge.dstElementId);
        res.put(l, edgeElementId);
      }
      **/
      return res;
    }
    
    @GetMapping("/node/{type}")
    public List<Neo4jNodeEntity> getNodes(@PathVariable String type, 
        @RequestParam(required = false) Integer limit) {
      System.out.println(type + " " + limit);
      return this.graphService.listNodeByType(type, limit);
    }
    
    @GetMapping("/node/{type}/{id}/parent")
    public List<List<Object>> getNodeParents(@PathVariable String type, 
        @PathVariable String id,
        @RequestParam(required = false) String edgeType) {
      //System.out.println("!!!!!!!!!!!!!" + type + "" + id);
      Neo4jNodeEntity node = this.graphService.getNode(id, type);
      return this.graphService.getParents(node.getNode(), edgeType);
    }
    
    @GetMapping("/node/{type}/{id}/child")
    public List<List<Object>> getNodeChilds(@PathVariable String type, 
        @PathVariable String id,
        @RequestParam(required = false) String edgeType) {
      //sSystem.out.println("XXXXXXXXXXXXXXXXX" + type + "" + id);
      Neo4jNodeEntity node = this.graphService.getNode(id, type);
      return this.graphService.getChilds(node.getNode(), edgeType);
    }
    
    @GetMapping("/node/page")
    public DataTablesResponse<Neo4jNodeEntity> pageNodes(
            @RequestParam(defaultValue = "1") int draw,
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "10") int length,
            @RequestParam(value = "search[value]", required = false) String searchValue,
            @RequestParam(value = "order[0][column]", required = false) String sortColumnIndex,
            @RequestParam(value = "order[0][dir]", defaultValue = "asc") String sortDirection,
            @RequestParam(value = "columns[0][data]", defaultValue = "key") String sortColumn,
            @RequestParam(required = false) String nodeType) {
        
        NodePageRequest request = new NodePageRequest(draw, start, length);
        request.setSearchValue(searchValue);
        request.setSortColumn(sortColumn);
        request.setSortDirection(sortDirection);
        request.setNodeType(nodeType);
        
        return this.graphService.pageNodes(request);
    }

    @PostMapping("/edge/{src}/{dst}/{type}")
    public String addEdge(@PathVariable String src, @PathVariable String dst, @PathVariable String type, 
        @RequestBody(required = false) Map<String, Object> properties) {
      if (properties == null) {
        properties = new HashMap<>();
    }
      //System.out.println("addEdge /edge/{src}/{dst}/{type}");
      //System.out.println(src);
      //System.out.println(dst);
      //System.out.println(type);
      //System.out.println(properties);
      return this.graphService.addEdge(src, dst, type, properties);
    }
    
    
    
    @GetMapping("/node/elementId/{eId}")
    public Neo4jNodeEntity getNodeByElementId(@PathVariable String eId) {
      return this.graphService.getNode(eId);
    }

    @GetMapping("/node/{type}/{id}")
    public Neo4jNodeEntity getNode(@PathVariable String type, 
                          @PathVariable String id) {
      return this.graphService.getNode(id, type);
    }
    
    @GetMapping("/node-edge/{eId}")
    public Map<String, Map<String, Object>> getNodeEdges(@PathVariable String eId, @RequestParam Integer limit) {
      return this.graphService.getNodeRelationships(eId, Direction.BOTH, limit);
    }
    
    @GetMapping("/node-edge/{eId}/incoming")
    public Map<String, Map<String, Object>> getNodeEdgesIn(@PathVariable String eId, @RequestParam Integer limit) {
      return this.graphService.getNodeRelationships(eId, Direction.INCOMING, limit);
    }
    
    @GetMapping("/node-edge/{eId}/outgoing")
    public Map<String, Map<String, Object>> getNodeEdgesOut(@PathVariable String eId, @RequestParam Integer limit) {
      return this.graphService.getNodeRelationships(eId, Direction.OUTGOING, limit);
    }

    @DeleteMapping("/node/{type}/{id}")
    public boolean deleteNode(@PathVariable String type, 
                              @PathVariable String id) {
        return false;
    }

    @DeleteMapping("/edge/{type}/{id}")
    public boolean deleteEdge(@PathVariable String type, 
                              @PathVariable String id) {
        return false;
    }
}
