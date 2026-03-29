package org.modelseeed.vault.beleif;

import org.neo4j.graphdb.Node;

public class FunctionHypothesis {
  
  public double confidence;

  public FunctionHypothesis(Node hyp, double confidence) {
    this.confidence = confidence;
  }
  //on FunctionHypothesis nodes  
  //double confidence;         // current posterior
  //double[] confidence_history; // array indexed by version
  //int created_at_version;
  //String status;             // "active", "contradicted", "retired"
}
