package org.modelseeed.vault.service;

import org.modelseeed.vault.core.Neo4jNodeEntity;
import org.modelseeed.vault.repository.GraphRepository;
import org.neo4j.graphdb.Transaction;
import org.springframework.stereotype.Service;

@Service
public class BeliefService {
  
  private final GraphRepository graphRepository;
  
  public BeliefService(GraphRepository graphRepository) {
    this.graphRepository = graphRepository;
  }
  
  public void initializeBelief(String elementId, String attribute) {
    try (Transaction tx = graphRepository.beginTx()) {
      Neo4jNodeEntity node = graphRepository.getNode(elementId, tx);
      
      Neo4jNodeEntity beliefNode = new Neo4jNodeEntity(elementId, attribute)
      //graphRepository.addNode(node, tx)
    }
  }

}
