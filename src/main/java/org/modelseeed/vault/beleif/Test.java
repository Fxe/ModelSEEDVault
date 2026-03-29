package org.modelseeed.vault.beleif;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;

public class Test {
  public List<FunctionHypothesis> getCurrentHypotheses(
      Node proteinNode, int currentVersion) {
    
    List<FunctionHypothesis> results = new ArrayList<>();
    
    for (Relationship rel : proteinNode.getRelationships(
        Direction.OUTGOING, 
        RelationshipType.withName("hypothesis_for"))) {
      
      Node hyp = rel.getEndNode();
      if (hyp.hasLabel(BeleifLabel.FunctionalHypothesis)) {
        String status = (String) hyp.getProperty("status", "active");
        if ("active".equals(status)) {
          double conf = (double) hyp.getProperty("confidence", 0.0);
          results.add(new FunctionHypothesis(hyp, conf));
        }
      }
    }
    
    results.sort((a, b) -> Double.compare(b.confidence, a.confidence));
    return results;
  }
  
  public void addEvidence(Node proteinNode, String sourceType, 
      String functionId, double weight, int version, Transaction tx) {
    
    // 1. create evidence event
    Node evidence = tx.createNode(Label.label("EvidenceEvent"));
    evidence.setProperty("source_type", sourceType);
    evidence.setProperty("version", version);
    evidence.setProperty("timestamp", System.currentTimeMillis());
    
    // 2. create or find belief fact
    Node beliefFact = tx.createNode(Label.label("BeliefFact"));
    beliefFact.setProperty("added_at_version", version);
    beliefFact.setProperty("retired_at_version", -1);
    beliefFact.setProperty("source_type", sourceType);
    beliefFact.setProperty("weight", weight);
    
    proteinNode.createRelationshipTo(
        beliefFact, RelationshipType.withName("has_belief"));
    beliefFact.createRelationshipTo(
        evidence, RelationshipType.withName("supported_by"));
    
    // 3. find or create hypothesis
    Node hypothesis = findOrCreateHypothesis(
        proteinNode, functionId, version, tx);
    beliefFact.createRelationshipTo(
        hypothesis, RelationshipType.withName("contributes_to"));
    
    // 4. recompute confidence from all contributing beliefs
    updateConfidence(hypothesis, version);
  }

  private Node findOrCreateHypothesis(
      Node proteinNode, String functionId, int version, Transaction tx) {
    
    // walk existing hypotheses for this protein
    for (Relationship rel : proteinNode.getRelationships(
        Direction.INCOMING,
        RelationshipType.withName("hypothesis_for"))) {
      
      Node hyp = rel.getStartNode();
      if (hyp.hasLabel(Label.label("FunctionHypothesis"))) {
        String existingFunction = (String) hyp.getProperty(
            "function_id", "");
        if (functionId.equals(existingFunction)) {
          return hyp;  // hypothesis already exists, reuse it
        }
      }
    }
    
    // no existing hypothesis for this function — create one
    Node hyp = tx.createNode(Label.label("FunctionHypothesis"));
    hyp.setProperty("function_id", functionId);
    hyp.setProperty("confidence", 0.0);
    hyp.setProperty("confidence_history", new double[]{0.0});
    hyp.setProperty("created_at_version", version);
    hyp.setProperty("status", "active");
    
    // link back to protein
    hyp.createRelationshipTo(
        proteinNode, RelationshipType.withName("hypothesis_for"));
    
    // link to the actual function node in the knowledge graph
    // (EC number, RHEA reaction, ModelSEED reaction, etc.)
    Node functionNode = findFunctionNode(functionId, tx);
    if (functionNode != null) {
      hyp.createRelationshipTo(
          functionNode, RelationshipType.withName("claims_function"));
    }
    
    return hyp;
  }
  
  private Node findFunctionNode(String functionId, Transaction tx) {
    // resolve against your existing knowledge graph
    // functionId could be "EC:3.1.1.2", "RHEA:12345", "MSRXN:rxn00001"
    String[] parts = functionId.split(":", 2);
    if (parts.length < 2) return null;
    
    String labelName = switch (parts[0]) {
      case "EC"    -> "ECNumber";
      case "RHEA"  -> "RHEAReaction";
      case "MSRXN" -> "ModelSEEDReaction";
      default      -> null;
    };
    
    if (labelName == null) return null;
    
    // use index lookup — you should have an index on these
    return tx.findNode(Label.label(labelName), "id", parts[1]);
  }

  private void updateConfidence(Node hypothesis, int version) {
    double logOdds = 0.0;
    
    for (Relationship rel : hypothesis.getRelationships(
        Direction.INCOMING,
        RelationshipType.withName("contributes_to"))) {
      
      Node belief = rel.getStartNode();
      int retired = (int) belief.getProperty("retired_at_version", -1);
      if (retired == -1 || retired > version) {
        double w = (double) belief.getProperty("weight", 0.0);
        logOdds += w;  // log-odds accumulation
      }
    }
    
    // convert log-odds to probability
    double confidence = 1.0 / (1.0 + Math.exp(-logOdds));
    hypothesis.setProperty("confidence", confidence);
    
    // append to history
    double[] history = (double[]) hypothesis.getProperty(
        "confidence_history", new double[0]);
    double[] updated = Arrays.copyOf(history, history.length + 1);
    updated[history.length] = confidence;
    hypothesis.setProperty("confidence_history", updated);
  }
}
