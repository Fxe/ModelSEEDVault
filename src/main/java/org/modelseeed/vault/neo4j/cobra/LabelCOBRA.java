package org.modelseeed.vault.neo4j.cobra;

import org.neo4j.graphdb.Label;

public enum LabelCOBRA implements Label {
  SBMLModel, 
  SBMLSpecies, 
  SBMLReaction, 
  SBMLGene,
  SBMLComplex,
  SBMLCompartment,
  
  COBRAModel,
  COBRAMetabolite,
  COBRAReaction,
  COBRAComplex,
  COBRAGene,
  COBRACompartment,
}
