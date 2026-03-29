package org.modelseeed.vault.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.StreamSupport;

import org.modelseeed.vault.biodb.biochem.GenericReaction;
import org.modelseeed.vault.biodb.biochem.GenericReactionFactory;
import org.modelseeed.vault.biodb.biochem.ReactionMatcher;
import org.modelseeed.vault.biodb.biochem.ReactionMatcherResult;
import org.modelseeed.vault.biodb.biochem.ReactionMapper.MatchResult;
import org.modelseeed.vault.config.CorsConfig;
import org.modelseeed.vault.core.Neo4jNodeEntity;
import org.modelseeed.vault.core.cobra.Model;
import org.modelseeed.vault.neo4j.cobra.LabelCOBRA;
import org.modelseeed.vault.neo4j.cobra.Neo4jMetabolite;
import org.modelseeed.vault.neo4j.cobra.Neo4jReaction;
import org.modelseeed.vault.repository.CobraModelRepository;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.springframework.stereotype.Service;

@Service
public class CobraModelService {

    private final CorsConfig corsConfig;
  
  private final CobraModelRepository repository;
  
  public CobraModelService(CobraModelRepository repository, CorsConfig corsConfig) {
    this.repository = repository;
    this.corsConfig = corsConfig;
  }
  
  public Model getModel(String modelId, String translate, Map<String, String> translationCompartment) {
    try (Transaction tx = this.repository.beginTx()) {
      Node modelNode = tx.findNode(LabelCOBRA.SBMLModel, "key", modelId);
      Model model = this.repository.getCobraModel(modelNode, translate, translationCompartment, 0, tx);
      return model;
    }
  }
  
  public static record ReactionInferecenResult(
      String target,
      Map<String, List<Object>> elementInfo,
      Map<String, String> translation,
      Map<String, MatchResult> match,
      Set<String> exclude
  ) {
    
    public ReactionInferecenResult(Node target) {
      this(
          target.getElementId(),
          new HashMap<>(),
          new HashMap<>(),
          new HashMap<>(),
          new HashSet<>()
      );
      this.addElement(target);
    }
    
    public void addElement(Node node) {
      this.elementInfo.put(node.getElementId(), List.of(node.getProperty("key"), 
          StreamSupport.stream(node.getLabels().spliterator(), false)
          .map(Label::name).toList()));
    }
    
    public void addReactionElement(Node node, Map<String, Double> s) {
      this.elementInfo.put(node.getElementId(), 
          List.of(
              node.getProperty("key"), 
              StreamSupport.stream(node.getLabels().spliterator(), false)
                           .map(Label::name).toList(), 
              s)
          );
    }
  }
  
  public ReactionInferecenResult reactionInference(String eid, Set<String> exclude) {
    
    try (Transaction tx = this.repository.beginTx()) {
      Node reactionNode = tx.getNodeByElementId(eid);
      Neo4jReaction reaction = Neo4jReaction.build(reactionNode, tx);
      
      ReactionMatcher matcher = new ReactionMatcher(exclude);
      //final Map<String, MatchResult> match = new HashMap<>();
      //final Map<String, List<Object>> elementInfo = new HashMap<>();
      final ReactionInferecenResult result = new ReactionInferecenResult(reactionNode);
      result.addReactionElement(reactionNode, reaction.getMetabolites());
      result.exclude.addAll(exclude);
      
      if (!reaction.isTranslocation()) {
        ReactionMatcherResult matcherResult = matcher.match(reaction, tx);
        
        if (matcherResult != null) {
          for (Entry<Neo4jMetabolite, Node> e: matcherResult.getTranslation().entrySet()) {
            Node node1 = e.getKey().getNode();
            Node node2 = e.getValue();
            result.addElement(node1);
            result.addElement(node2);
            result.translation.put(node1.getElementId(), node2.getElementId());
          }
          
          Map<Node, MatchResult> matchMap = matcherResult.getMatch();
          if (matchMap != null) {
            Map<Node, MatchResult> resultNonNull = new HashMap<>();
            matchMap.forEach((k, v) -> { if (v != null) resultNonNull.put(k, v); });
            
            //System.out.println(reaction);
            resultNonNull.forEach((node, v) -> {
              result.match.put(node.getElementId(), v);
              GenericReaction<String> matchedReaction = GenericReactionFactory.build(node);
              result.addReactionElement(node, matchedReaction.getStoichiometry());
              
              //System.out.println(String.format("\t[%d]%s %s", k.getProperty("is_obsolete"), k.getProperty("key"), v));
            });
          }
        }
      }
      
      
      tx.rollback();
      //System.out.println(result.elementInfo);
      return result;
    }
  }
  
  public Neo4jReaction getReaction(String modelId, String reactionId) {
    return null;
  }
  
  public Neo4jReaction getReaction(String eid) {
    try (Transaction tx = this.repository.beginTx()) {
      Node reactionNode = tx.getNodeByElementId(eid);
      Neo4jReaction reaction = Neo4jReaction.build(reactionNode, tx);
      return reaction;
    }
  }
  
  public List<ReactionInferecenResult> modelReactionsInference(String modelId, Set<String> exclude) {
    
    Set<String> eidSet = new HashSet<>();
    try (Transaction tx = this.repository.beginTx()) {
      Neo4jNodeEntity nodeModel = this.repository.getNode(modelId, "SBMLModel", tx);
      List<List<Object>> modelReactions = this.repository.getChilds(nodeModel.getNode(),
          RelationshipType.withName("has_sbml_reaction"), tx);
      

      for (List<Object> edgeAndNode : modelReactions) {
        // @SuppressWarnings("unchecked")
        // Map<String, Object> rel = (Map<String, Object>) edgeAndNode.get(0);
        Neo4jNodeEntity node = (Neo4jNodeEntity) edgeAndNode.get(1);
        Neo4jReaction reaction = Neo4jReaction.build(node.getNode(), modelId, tx);
        //System.out.println(reaction.stoichiometry.s.keySet());
        //skip translocation reactions
        if (!reaction.isTranslocation()) {
          eidSet.add(node.getElementId());
        }
      } 
    }
    
    List<ReactionInferecenResult> result = new ArrayList<>();
    for (String reactionEid: eidSet) {
      result.add(this.reactionInference(reactionEid, exclude));
    }
    return result;
  }
}
