package org.modelseeed.vault.controller;

import org.modelseeed.vault.service.BeliefService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/belief")
@CrossOrigin
public class BeliefController {
  
  private BeliefService beliefService;
  
  public static record StartBeliefRequest(
      String elementId, 
      String beliefAttribute, 
      int knowledgeDepth
      
      //evidenceVersion ??
      ) {}

  public BeliefController(BeliefService beliefService) {
      this.beliefService = beliefService;
  }
  
  @PostMapping("/start")
  public Object registerBelief(@RequestBody StartBeliefRequest request) {
    // request: entityId, entityType, attribute, maxDepth, evidenceVersion
    // 1. Validate attribute against controlled vocabulary
    // 2. Extract subgraph from GraphService
    // 3. Compute EvidenceScore
    // 4. Create BeliefNode in Neo4j
    // 5. Persist snapshot to MongoDB (belief_snapshots)
    // 6. Return belief identifier + evidence score summary
    return null;
  }
}
