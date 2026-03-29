package org.modelseeed.vault.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelseeed.vault.biodb.biochem.OntologyBiochemReaction;
import org.modelseeed.vault.core.Neo4jNodeEntity;
import org.modelseeed.vault.core.cobra.Metabolite;
import org.modelseeed.vault.core.cobra.Model;
import org.modelseeed.vault.core.cobra.Reaction;
import org.modelseeed.vault.neo4j.cobra.Neo4jReaction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.springframework.stereotype.Repository;

@Repository
public class CobraModelRepository extends GraphRepository {

    //private final CorsConfig corsConfig;

  public CobraModelRepository(GraphDatabaseService db) {
    super(db);
    //this.corsConfig = corsConfig;
  }
  
  public Map<String, Double> getReactionMetabolites(Node reactionNode, int prefix_strip, Map<String, String> translationSpecies, Transaction tx) {
    Map<String, Double> metabolites = new HashMap<>();
    
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
    
    return metabolites;
  }
  
  public Neo4jNodeEntity getLastAnnotation(Node node, Transaction tx) {
    long tLast = 0;
    Neo4jNodeEntity last = null;
    List<List<Object>> events = this.getChilds(node, RelationshipType.withName("has_annotation_event"), tx);
    for (List<Object> event: events) {
      @SuppressWarnings("unchecked")
      Map<String, Object> eRel = (Map<String, Object>) event.get(0);
      @SuppressWarnings("unchecked")
      Map<String, Object> eRelProperties = (Map<String, Object>) eRel.get("properties");
      long createdAt = (long) eRelProperties.get("_created_at");
      if (createdAt > tLast) {
        tLast = createdAt;
        Neo4jNodeEntity eNode = (Neo4jNodeEntity) event.get(1);
        last = eNode;
        //mappedEntry = String.format("%s_%s", eNode.getEntry(), speciesCompartment);
        //System.out.println(e + " " + mappedEntry);
      }
    }
    
    return last;
  }
  
  public Model getCobraModelByModelId(String modelId, String translate, 
      Map<String, String> translationCompartment, Integer compartmentIndex, Transaction tx) {
    Neo4jNodeEntity nodeModel = this.getNode(modelId, "SBMLModel", tx);
    return this.getCobraModel(nodeModel.getNode(), translate, translationCompartment, 
        compartmentIndex, tx);
  }
  
  public Model getCobraModel(Node modelNode, String translate, 
      Map<String, String> translationCompartment, Integer compartmentIndex, Transaction tx) {
    System.out.println("translate:" + translate);
    //System.out.println(nodeModel.getEntry());
    //System.out.println(nodeModel.getLabel());
    ///System.out.println(nodeModel.getLabels());
    //
    String modelEntry = (String) modelNode.getProperty("key");
    int prefix_strip = modelEntry.length() + 1;
    String modelName = null;
    if (modelNode.hasProperty("name")) {
      modelName = (String) modelNode.getProperty("name");      
    }
    
    
    List<List<Object>> childs = this.getChilds(modelNode, null, tx);
    //System.out.println(childs.size());
    //graphService.getChilds(id, id, id)
    
    Model model = new Model(modelEntry, modelName, null);
    
    Map<String, String> translationSpecies = new HashMap<>(); 
    
    for (List<Object> t: childs) {
      @SuppressWarnings("unchecked")
      Map<String, Object> rel = (Map<String, Object>) t.get(0);
      Neo4jNodeEntity node = (Neo4jNodeEntity) t.get(1);
      String relType = rel.get("t").toString();
      if (relType.equals("has_sbml_compartment")) {
        String e = node.getEntry().substring(prefix_strip);
        String name = node.getProperties().getOrDefault("name", e).toString();
        e = translationCompartment.getOrDefault(e, e);
        
        model.getCompartments().put(e, name);
      } else if (relType.equals("has_sbml_species")) {
        String e = node.getEntry().substring(prefix_strip);
        String name = node.getProperties().getOrDefault("name", e).toString();
        String speciesCompartment = node.getProperties().getOrDefault("compartment", "").toString();
        speciesCompartment = translationCompartment.getOrDefault(speciesCompartment, speciesCompartment);
        if (compartmentIndex != null) {
          speciesCompartment += "" + compartmentIndex;
        }
        
        //System.out.println(node.getProperties());s
        if (translate != null) {
          Neo4jNodeEntity annotationNode = this.getLastAnnotation(node.getNode(), tx);
          if (annotationNode != null) {
            String mappedEntry = String.format("%s_%s", annotationNode.getEntry(), speciesCompartment);         
            translationSpecies.put(e, mappedEntry);
          }
        }

        Metabolite metabolite = new Metabolite(translationSpecies.getOrDefault(e, e), name, "");
        model.getMetabolites().add(metabolite);
      }
    }
    
    for (List<Object> t: childs) {
      @SuppressWarnings("unchecked")
      Map<String, Object> rel = (Map<String, Object>) t.get(0);
      Neo4jNodeEntity node = (Neo4jNodeEntity) t.get(1);
      String relType = rel.get("t").toString();
      if (relType.equals("has_sbml_reaction")) {
        Neo4jReaction reactionNode = Neo4jReaction.build(node.getNode(), modelEntry, tx);
        String e = node.getEntry().substring(prefix_strip);
        String name = node.getProperties().getOrDefault("name", e).toString();
        String mappedEntry = e;

        Map<String, Double> metabolites = this.getReactionMetabolites(node.getNode(), prefix_strip, translationSpecies, tx);
        Set<String> compartments = reactionNode.stoichiometry.getCompartments().stream()
            .map(x -> translationCompartment.getOrDefault(x, x))
            .collect(Collectors.toSet());
        String compartment = List.copyOf(compartments).get(0);
        if (compartments.size() > 1) {
          if (compartments.contains("p")) {
            compartment = "p";
          }
        }
        
        if (translate != null) {
          Node annotation = reactionNode.annotation(OntologyBiochemReaction.ModelSEEDReaction);
          if (annotation != null) {
            mappedEntry = String.format("%s_%s", annotation.getProperty("key"), compartment);         
          }
        }
        
        //System.out.println(compartments);
        Reaction reaction = new Reaction(mappedEntry, name, metabolites, 0, 1000, "", null);
        model.getReactions().add(reaction);
      }
    }
    return model;
  }

  
}
