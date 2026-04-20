package org.modelseeed.vault.core;

import java.io.IOException;

import org.neo4j.graphdb.Label;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class Neo4jLabelDeserializer extends JsonDeserializer<Label> {

  @Override
  public Label deserialize(JsonParser p, DeserializationContext ctxt) 
      throws IOException, JacksonException {
    
    String value = p.getValueAsString();
    return Label.label(value);
  }

}
