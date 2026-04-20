package org.modelseeed.vault.core;

import java.io.IOException;

import org.neo4j.graphdb.Label;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class Neo4jLabelSerializer extends JsonSerializer<Label> {

  @Override
  public void serialize(Label value, JsonGenerator gen, 
      SerializerProvider serializers) throws IOException {
    gen.writeString(value.name());
  }
}
