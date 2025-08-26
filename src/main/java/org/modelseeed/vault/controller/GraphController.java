package org.modelseeed.vault.controller;

import java.util.List;
import java.util.Map;

import org.modelseeed.vault.core.Neo4jNodeEntity;
import org.modelseeed.vault.service.GraphService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/graph")
public class GraphController {

    private GraphService graphService;

    public GraphController(GraphService graphService) {
        this.graphService = graphService;
    }
    
    @PostMapping("/node/register")
    public void registerNode(String type) {
      this.graphService.registerNode(type);
    }
    
    @GetMapping("/node/register")
    public Object getAllConstraints() {
      return this.graphService.listConstraints();
    }

    @PostMapping("/node/{type}/{id}")
    public Neo4jNodeEntity addNode(@PathVariable String type, @PathVariable String id,
                          @RequestParam(defaultValue = "{}") Map<String, Object> properties, 
                          @RequestParam(required = false) List<String> labels) {
      return this.graphService.addNode(type, id, properties);
    }
    


    @PostMapping("/edge")
    public String addEdge(@RequestParam Map<String, Object> data) {
      //this.graphService.addNode(null, data);
        return null;
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
