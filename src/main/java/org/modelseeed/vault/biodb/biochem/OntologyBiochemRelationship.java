package org.modelseeed.vault.biodb.biochem;

import org.neo4j.graphdb.RelationshipType;

public enum OntologyBiochemRelationship implements RelationshipType {
  has_parent,
  has_child,
}
