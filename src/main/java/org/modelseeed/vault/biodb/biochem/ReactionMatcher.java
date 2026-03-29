package org.modelseeed.vault.biodb.biochem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.modelseeed.vault.biodb.biochem.ReactionMapper.MatchResult;
import org.modelseeed.vault.neo4j.cobra.Neo4jMetabolite;
import org.modelseeed.vault.neo4j.cobra.Neo4jReaction;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;

public class ReactionMatcher {
  
  protected Set<String> exclude;
  
  public ReactionMatcher(Set<String> exclude) {
    this.exclude = exclude;
  }

  public static Map<Neo4jMetabolite, Node> mapCompound(Iterable<Neo4jMetabolite> metabolites, Label label,
      boolean requireAll) {
    Map<Neo4jMetabolite, Node> mapping = new HashMap<>();
    for (Neo4jMetabolite metabolite : metabolites) {
      Node annotatedCompound = metabolite.annotation(label);
      if (annotatedCompound == null && requireAll) {
        return null;
      } else {
        mapping.put(metabolite, annotatedCompound);
      }
    }
    return mapping;
  }
  
  public ReactionMatcherResult match(Neo4jReaction reactionNode, Transaction tx) {
    
    Map<Neo4jMetabolite, Node> mapping = mapCompound(
        reactionNode.metaboliteNodes.values(), Label.label("ModelSEEDCompound"), true);
    Node currentAnnotation = reactionNode.annotation(OntologyBiochemReaction.ModelSEEDReaction);
    System.out.println(currentAnnotation);
    
    if (mapping == null) {
      return null;
    }
    
    Map<String, Integer> degreeMap = new HashMap<>();
    Map<Integer, List<Node>> degreeToCompound = new HashMap<>();
    for (Entry<Neo4jMetabolite, Node> e : mapping.entrySet()) {
      Node annotatedCompound = e.getValue();
        int degree = annotatedCompound.getDegree(RelationshipType.withName("has_stoichiometry_coefficient"));
        degreeMap.put(annotatedCompound.getElementId(), degree);
        //nodeMap.put(annotatedCompound.getElementId(), annotatedCompound);
        degreeToCompound.putIfAbsent(degree, new ArrayList<>());
        degreeToCompound.get(degree).add(annotatedCompound);
        // System.out.println(annotatedCompound.getAllProperties() + " " +
        // annotatedCompound.getLabels());
    }
    
    List<Integer> sortedDegreeList = degreeMap.values().stream().sorted().distinct().collect(
        Collectors.toList());
    
  //System.out.println(intersection);
    Set<String> intersection = null;
    
    Map<String, Node> eidToReactionNode = new HashMap<>();
    for (int degree : sortedDegreeList) {
      for (Node annotatedCompound : degreeToCompound.get(degree)) {

        // Collect all generic reaction elementIds connected to this compound
        Set<String> reactionIds = new HashSet<>();
        for (Relationship relStoich : annotatedCompound
            .getRelationships(RelationshipType.withName("has_stoichiometry_coefficient"))) {
          Node genericReaction = relStoich.getOtherNode(annotatedCompound);
          //int stoichDegree = genericReaction.getDegree(RelationshipType.withName("has_stoichiometry_coefficient"));
          //if (stoichDegree == reactionSize) {
          
          eidToReactionNode.put(genericReaction.getElementId(), genericReaction);
            reactionIds.add(genericReaction.getElementId());
          //}
        }

        if (intersection == null) {
          // Seed with the first (smallest-degree) compound's reaction set
          intersection = reactionIds;
          // System.out.println(degree + " " + annotatedCompound.getProperty("key") + " i
          // " + intersection.size());
        } else {
          // Intersect incrementally — fail fast if already empty
          intersection.retainAll(reactionIds);
          // System.out.println(degree + " " + annotatedCompound.getProperty("key") + " i
          // " + intersection.size());
          if (intersection.isEmpty()) {
            //System.out.println("stop!!!!!!!!");
            return null;
          }
        }
      }
    }
    
    Map<String, String> t = new HashMap<>();
    mapping.forEach((k, v) -> t.put(k.getId(), (String) v.getProperty("key")));
    Map<String, Double> r1 = reactionNode.getMetabolites();
    
    //System.out.println("Translation t: " + t);
    //System.out.println("Reaction r1: " + r1);
    
    Map<Node, MatchResult> ret = new HashMap<>();
    
    for (String eid: intersection) {
      Node otherReactionNode = eidToReactionNode.get(eid);

      if (currentAnnotation != null && 
          otherReactionNode.getElementId().equals(currentAnnotation.getElementId())) {
        ret.put(otherReactionNode, MatchResult.MAPPED);
      } else {
        GenericReaction<String> reaction2 = GenericReactionFactory.build(otherReactionNode);
        Map<String, Double> r2 = reaction2.getStoichiometry();
        //System.out.println(r2);
        ReactionMapper reactionMapper = new ReactionMapper(r1, r2, t, exclude);
        MatchResult result = reactionMapper.match();
        //System.out.println(reaction2.getId() + " " + result);
        ret.put(otherReactionNode, result);
      }
    }
    
    return new ReactionMatcherResult(mapping, ret, this.exclude);
  }
}
