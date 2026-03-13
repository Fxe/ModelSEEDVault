package org.modelseeed.rast;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AnnotationRASTResult {

  protected List<Object> contigs;
  protected List<Object> features;
  protected List<AnnotationRASTResultEvent> analysis_events;

  public List<Object> getContigs() {
    return contigs;
  }

  public void setContigs(List<Object> contigs) {
    this.contigs = contigs;
  }

  public List<Object> getFeatures() {
    return features;
  }

  public void setFeatures(List<Object> features) {
    this.features = features;
  }

  public List<AnnotationRASTResultEvent> getAnalysis_events() {
    return analysis_events;
  }

  public void setAnalysis_events(List<AnnotationRASTResultEvent> analysis_events) {
    this.analysis_events = analysis_events;
  }

  public static AnnotationRASTResult fromJson(String json, ObjectMapper objectMapper) throws JsonMappingException, JsonProcessingException {
    List<AnnotationRASTResult> results =
        objectMapper.readValue(json, new TypeReference<List<AnnotationRASTResult>>() {});

    if (results == null || results.isEmpty()) {
      throw new IllegalArgumentException("Expected singleton array with one AnnotationRASTResult");
    }

    return results.get(0);
  }
}
