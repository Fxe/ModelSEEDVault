package org.modelseeed.vault.neo4j.cobra;

import org.neo4j.graphdb.RelationshipType;

public enum RelationshipCOBRA implements RelationshipType {
  has_compartment,
  has_species,
  has_reaction,
  has_reactant,
  has_product,
  has_annotation_event,
  has_gene_complex,
  has_gene,
}