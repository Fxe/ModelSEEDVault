package org.modelseeed.vault.neo4j.cobra;

import org.modelseeed.vault.core.cobra.Metabolite;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

public class Neo4jMetabolite extends Metabolite {
  
  protected String prefix;
  protected Node node;
  
  public Neo4jMetabolite(String id, String name, String compartment, Node node, String prefix) {
    super(id, name, compartment);
    if (!node.hasLabel(LabelCOBRA.SBMLSpecies)) {
      throw new IllegalArgumentException("Invalid node: " + node.getLabels());
    }
    this.prefix= prefix;
    this.node = node;
  }
  
  public static Neo4jMetabolite build(Node node, String prefix, Transaction tx) {
    int prefixStrip = prefix.length() + 1;
    String speciesId = node.getProperty("key").toString().substring(prefixStrip);
    String name = node.getProperties().getOrDefault("name", speciesId).toString();
    
    // this is returning always ""
    //String speciesCompartment = node.getProperties().getOrDefault("compartment", "").toString();
    
    String speciesCompartment = (String) node.getProperty("compartment");
    return new Neo4jMetabolite(speciesId, name, speciesCompartment, node, prefix);
  }
  
  public Node getNode() {
    return this.node;
  }
  
  public Node annotation(Label ontology) {
    long tLast = 0;
    Node result = null;
    for (Relationship rel: this.node.getRelationships(
        Direction.OUTGOING, RelationshipCOBRA.has_annotation_event)) {
      long createdAt = (long) rel.getProperty("_created_at");
      Node other = rel.getOtherNode(this.node);
      
      if (other.hasLabel(ontology) && createdAt > tLast) {
        tLast = createdAt;
        result = other;
      }
    }
    return result;
  }
}
