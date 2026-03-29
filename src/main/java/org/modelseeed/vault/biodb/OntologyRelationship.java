package org.modelseeed.vault.biodb;

import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

public enum OntologyRelationship implements RelationshipType {
  has_accession,
  has_protein_sequence,
}
