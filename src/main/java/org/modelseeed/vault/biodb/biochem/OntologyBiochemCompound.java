package org.modelseeed.vault.biodb.biochem;

import org.neo4j.graphdb.Label;

public enum OntologyBiochemCompound implements Label {
  ModelSEEDCompound,
  KEGGCompound,
  KEGGGlycan,
  KEGGDrug,
  
  Disabled,
  //BiocycReaction,
  //RHEAReaction,
  //BiGGReaction,
}
