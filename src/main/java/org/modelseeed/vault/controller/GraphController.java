package org.modelseeed.vault.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.modelseeed.vault.core.Neo4jNodeEntity;
import org.modelseeed.vault.dto.DataTablesResponse;
import org.modelseeed.vault.dto.NodePageRequest;
import org.modelseeed.vault.service.GraphService;
import org.neo4j.graphdb.Direction;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/graph")
@CrossOrigin
public class GraphController {

    private GraphService graphService;

    public GraphController(GraphService graphService) {
        this.graphService = graphService;
    }
    
    @GetMapping("/node/count")
    public Map<Set<String>, Integer> count() {
      return this.graphService.countNodes();
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
      return this.graphService.addNode(type, id, properties);
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
