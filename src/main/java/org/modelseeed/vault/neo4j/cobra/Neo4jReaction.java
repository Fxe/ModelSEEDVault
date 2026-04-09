package org.modelseeed.vault.neo4j.cobra;

import java.util.HashMap;
import java.util.Map;

import org.modelseeed.vault.biodb.biochem.TranslocationStoichiometry;
import org.modelseeed.vault.core.cobra.Reaction;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

public class Neo4jReaction extends Reaction {
  
  protected String prefix;
  protected Node node;
  public Map<String, Neo4jMetabolite> metaboliteNodes;
  public TranslocationStoichiometry<String, String> stoichiometry;
  
  public Neo4jReaction(String id, Node node, String prefix, Map<Neo4jMetabolite, Double> metabolites) {
    super();
    if (!node.hasLabel(LabelCOBRA.SBMLReaction)) {
      throw new IllegalArgumentException("Invalid node: " + node.getLabels());
    }
    this.node = node;
    this.prefix= prefix;
    this.metaboliteNodes = new HashMap<>();
    this.setMetabolites(new HashMap<>());
    this.stoichiometry = new TranslocationStoichiometry<>();
    for (Neo4jMetabolite metabolite: metabolites.keySet()) {
      double value = metabolites.get(metabolite);
      this.metaboliteNodes.put(metabolite.getId(), metabolite);
      this.getMetabolites().put(metabolite.getId(), value); 
      this.stoichiometry.addMetabolite(metabolite.getId(), metabolite.getCompartment(), value);
    }
    this.setId(id);
  }
  
  public boolean isTranslocation() {
    return this.stoichiometry.isTranslocation();
  }
  
  @Override
  public String toString() {
    return String.format("%s: %s", this.getId(), stoichiometry.toString());
  }
  
  public Node getNode() {
    return this.node;
  }
  
  public static Neo4jReaction build(Node node, String prefix, 
      Transaction tx) {
    int prefixStrip = prefix.length() + 1;
    String id = node.getProperty("key").toString().substring(prefixStrip);
    Map<Neo4jMetabolite, Double> metabolites = Neo4jReaction.getReactionMetabolites(
        node, prefix, tx);
    return new Neo4jReaction(id, node, prefix, metabolites);
  }
  
  public static Neo4jReaction build(Node node, 
      Transaction tx) {
    String prefix = null;
    for (Relationship r: node.getRelationships(
        Direction.INCOMING, RelationshipCOBRA.has_reaction)) {
      Node maybeModelNode = r.getOtherNode(node);
      if (maybeModelNode.hasLabel(LabelCOBRA.SBMLModel)) {
        if (prefix == null) {
          prefix = (String) maybeModelNode.getProperty("key"); 
        } else {
          //find a better exception for this
          throw new IllegalArgumentException("unable to resolve prefix multiple");
        }
      }
    }
    
    if (prefix == null) {
    //find a better exception for this
      throw new IllegalArgumentException("unable to resolve prefix");
    }
    
    int prefixStrip = prefix.length() + 1;
    String id = node.getProperty("key").toString().substring(prefixStrip);
    Map<Neo4jMetabolite, Double> metabolites = Neo4jReaction.getReactionMetabolites(
        node, prefix, tx);
    return new Neo4jReaction(id, node, prefix, metabolites);
  }
  
  public static Map<Neo4jMetabolite, Double> getReactionMetabolites(Node node, 
      String prefix,Transaction tx) {
    //int prefixStrip = prefix.length() + 1;
    Map<Neo4jMetabolite, Double> metabolites = new HashMap<>();
    
    for (Relationship r: node.getRelationships(Direction.OUTGOING, 
        RelationshipCOBRA.has_reactant)) {
      Map<String, Object> relationshipProperties = r.getAllProperties();
      Node other = r.getOtherNode(node);
      
      double value = Double.valueOf(relationshipProperties.getOrDefault("stoichiometry", "1.0").toString());
      //String species = other.getProperty("key").toString().substring(prefixStrip);
      //this.metaboliteNodes.put(species, Neo4jMetabolite.build(other, this.prefix, tx));
      //String species = "x"; // other.getProperty("entry").substring(prefix_strip);
      metabolites.put(Neo4jMetabolite.build(other, prefix, tx), -1 * value);
    }
    
    for (Relationship r: node.getRelationships(Direction.OUTGOING, 
        RelationshipCOBRA.has_product)) {
      Map<String, Object> relationshipProperties = r.getAllProperties();
      Node other = r.getOtherNode(node);
      
      double value = Double.valueOf(relationshipProperties.getOrDefault("stoichiometry", "1.0").toString());
      //String species = other.getProperty("key").toString().substring(prefixStrip);
     // this.metaboliteNodes.put(species, Neo4jMetabolite.build(other, this.prefix, tx));
      //String species = "x"; // other.getProperty("entry").substring(prefix_strip);
      metabolites.put(Neo4jMetabolite.build(other, prefix, tx), value);
    }
    
    /**
    List<List<Object>> react = this.getChilds(reactionNode, RelationshipType.withName("has_reactant"), tx);
    for (List<Object> stoich: react) {
      @SuppressWarnings("unchecked")
      Map<String, Object> eRel = (Map<String, Object>) stoich.get(0);
      @SuppressWarnings("unchecked")
      Map<String, Object> eRelProperties = (Map<String, Object>) eRel.get("properties");
      Neo4jNodeEntity eNode = (Neo4jNodeEntity) stoich.get(1);
      
      double value = Double.valueOf(eRelProperties.getOrDefault("stoichiometry", "1.0").toString());
      String species = eNode.getEntry().substring(prefix_strip);
      
      metabolites.put(translationSpecies.getOrDefault(species, species), -1 * value);
    }
    List<List<Object>> products = this.getParents(reactionNode, RelationshipType.withName("has_product"), tx);
    
    for (List<Object> stoich: products) {
      @SuppressWarnings("unchecked")
      Map<String, Object> eRel = (Map<String, Object>) stoich.get(0);
      @SuppressWarnings("unchecked")
      Map<String, Object> eRelProperties = (Map<String, Object>) eRel.get("properties");
      Neo4jNodeEntity eNode = (Neo4jNodeEntity) stoich.get(1);
      
      double value = Double.valueOf(eRelProperties.getOrDefault("stoichiometry", "1.0").toString());
      String species = eNode.getEntry().substring(prefix_strip);
      
      metabolites.put(translationSpecies.getOrDefault(species, species), value);
    }
    **/
    
    return metabolites;
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
