package org.modelseeed.vault.controller;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.modelseeed.vault.core.cobra.Model;
import org.modelseeed.vault.service.CobraModelService;
import org.modelseeed.vault.service.CobraModelService.ReactionInferecenResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/cobra")
@CrossOrigin
public class CobraController {
  
  private CobraModelService cobraModelService;
  
  public CobraController(CobraModelService cobraModelService) {
    this.cobraModelService = cobraModelService;
}
  
  
  @GetMapping("/model/{id}")
  public Model getModel(
      @Parameter(
          description = "Model ID",
          example = "iAbaylyiv4"
      )
      @PathVariable String id, 
      @Parameter(
          description = "Translate Model",
          example = "ModelSEED"
      )
      @RequestParam(required = false) String translate, 
      @Parameter(
          description = "Override compartment symbol",
          example = "Cytosol:c;Extraorganism:e"
      )
      @RequestParam(required = false) String translationCompartmentParam) {
    
    //implement
    Map<String, String> translationCompartment = new HashMap<>();

    if (translationCompartmentParam != null && !translationCompartmentParam.isBlank()) {
      String[] pairs = translationCompartmentParam.split(";");

      for (String pair : pairs) {
        String[] parts = pair.split(":");
        if (parts.length == 2) {
          String key = parts[0].trim();
          String value = parts[1].trim();
          translationCompartment.put(key, value);
        }
      }
    }

    Model model = this.cobraModelService.getModel(id, translate, translationCompartment);
    /**
    Neo4jNodeEntity nodeModel = graphService.getNode(id, "SBMLModel");
    System.out.println("translate:" + translate);
    System.out.println(nodeModel.getEntry());
    System.out.println(nodeModel.getLabel());
    System.out.println(nodeModel.getLabels());
    List<List<Object>> childs = graphService.getChilds(nodeModel.getNode(), null);
    System.out.println(childs.size());
    //graphService.getChilds(id, id, id)
    Object o_name = nodeModel.getProperties().getOrDefault("name", null);
    String modelName = null;
    if (o_name != null) {
      modelName = o_name.toString();
    }
    Model model = new Model(nodeModel.getEntry(), modelName, null);
    
    int prefix_strip = id.length() + 1;
    
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
        
        //System.out.println(node.getProperties());
        String mappedEntry = null;
        if (translate != null) {
          long tLast = 0;
          List<List<Object>> events = graphService.getChilds(node.getNode(), "has_annotation_event");
          for (List<Object> event: events) {
            @SuppressWarnings("unchecked")
            Map<String, Object> eRel = (Map<String, Object>) event.get(0);
            @SuppressWarnings("unchecked")
            Map<String, Object> eRelProperties = (Map<String, Object>) eRel.get("properties");
            long createdAt = (long) eRelProperties.get("_created_at");
            if (createdAt > tLast) {
              tLast = createdAt;
              Neo4jNodeEntity eNode = (Neo4jNodeEntity) event.get(1);
              mappedEntry = String.format("%s_%s", eNode.getEntry(), speciesCompartment);
              //System.out.println(e + " " + mappedEntry);
            }

          }
        }
        
        if (mappedEntry != null) {
          translationSpecies.put(e, mappedEntry);
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
        String e = node.getEntry().substring(id.length());
        String name = node.getProperties().getOrDefault("name", e).toString();
        
        Map<String, Double> metabolites = new HashMap<>();
        
        List<List<Object>> react = graphService.getChilds(node.getNode(), "has_reactant");
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
        List<List<Object>> products = graphService.getChilds(node.getNode(), "has_product");
        
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
        
        
        Reaction reaction = new Reaction(e, name, metabolites, 0, 1000, "", null);
        model.getReactions().add(reaction);
      }
    }
    **/
    
    return model;
  }
  
  @PostMapping("/model/{id}/reaction/all/inference")
  public List<ReactionInferecenResult> reactionAnnotationInference(
      @Parameter(
          description = "Model ID",
          example = "iAbaylyiv4"
      )
      @PathVariable String id) {
    Set<String> exclude = new HashSet<>();
    exclude.add("cpd00067");
    return this.cobraModelService.modelReactionsInference(id, exclude);
  }
  
  
  @PostMapping("/model/{id}/reaction/eid/{eid}/inference")
  public ReactionInferecenResult reactionAnnotationInference(
      @Parameter(
          description = "Model ID",
          example = "iAbaylyiv4"
      )
      @PathVariable String id,
      @Parameter(
          description = "Neo4j Element ID",
          example = "4:3779b88f-90cb-49d4-a69d-a7360263bf82:2364"
      )
      @PathVariable String eid) {
    Set<String> exclude = new HashSet<>();
    exclude.add("cpd00067");
    return this.cobraModelService.reactionInference(eid, exclude);
  }
}
