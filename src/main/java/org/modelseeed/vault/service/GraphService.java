package org.modelseeed.vault.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.modelseeed.vault.core.Neo4jNodeEntity;
import org.modelseeed.vault.dto.DataTablesResponse;
import org.modelseeed.vault.dto.NodePageRequest;
import org.modelseeed.vault.repository.GraphRepository;
import org.springframework.stereotype.Service;

@Service
public class GraphService {

    private final GraphRepository graphRepository;

    public GraphService(GraphRepository graphRepository) {
        this.graphRepository = graphRepository;
    }
    
    public void registerNode(String type) {
        this.graphRepository.registerEntity(type);
    }
    
    public List<Map<String, Object>> listConstraints() {
        return this.graphRepository.getUniqueConstraint();
    }

    public Neo4jNodeEntity addNode(String type, String key, Map<String, Object> properties) {
      if (properties == null) {
        properties = new HashMap<>();
      }
      System.out.println(properties);
      Neo4jNodeEntity node = new Neo4jNodeEntity(key, type, properties);
      return graphRepository.addNode(node);
    }
    
    public Neo4jNodeEntity getNode(String key, String type) {
      return this.graphRepository.getNode(key, type);
    }
    
    public Neo4jNodeEntity getNode(String elementId) {
      return this.graphRepository.getNode(elementId);
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
        try {
            // Get total count without filtering
            long totalRecords = this.graphRepository.countNodes(null, request.getNodeType());
            
            // Get filtered count
            long filteredRecords = this.graphRepository.countNodes(request.getSearchValue(), request.getNodeType());
            
            // Get paginated data
            List<Neo4jNodeEntity> nodes = this.graphRepository.getPagedNodes(
                request.getStart(),
                request.getLength(),
                request.getSearchValue(),
                request.getSortColumn(),
                request.getSortDirection(),
                request.getNodeType()
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
