package org.modelseeed.vault.biodb.biochem;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.driver.exceptions.value.ValueException;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

public class GenericReactionFactory {
  
  public static GenericReaction<String> build(Node node) {
    //System.out.println(node.getLabels());
    //System.out.println(node.getAllProperties());
    //System.out.println(node.hasLabel(OntologyBiochemReaction.ModelSEEDReaction));
    
    Map<String, Double> lhs = new HashMap<>();
    Map<String, Double> rhs = new HashMap<>();
    
    if (node.hasLabel(OntologyBiochemReaction.ModelSEEDReaction)) {
      for (Relationship relationship: node.getRelationships(
          Direction.OUTGOING, RelationshipType.withName("has_stoichiometry_coefficient"))) {
        
        Node other = relationship.getOtherNode(node);
        double value = Double.valueOf(relationship.getProperty("coefficient").toString());
        if (value > 0) {
          rhs.put((String) other.getProperty("key"), value);
        } else if (value < 0) {
          lhs.put((String) other.getProperty("key"), -1 * value);
        } else {
          throw new ValueException("coefficient zero");
        }
      }
    } else {
      System.err.println("node implemented for " + node.getLabels());
      return null;
    }
    
    return new GenericReaction<String>((String) node.getProperty("key"), lhs, rhs);
  }
}
