package org.modelseeed.vault.core;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

public record Neo4jNodeReference(
    String key, 
    @JsonDeserialize(using = Neo4jLabelDeserializer.class)
    @JsonSerialize(using = Neo4jLabelSerializer.class)
    Label type, 
    String elementId
    ) {

  public static Neo4jNodeReference fromNode(Node node) {

    return new Neo4jNodeReference(node.getProperty("key").toString(), 
                                  Label.label(node.getProperty("_primary_label", "").toString()),
                                  node.getElementId());
  }
}
