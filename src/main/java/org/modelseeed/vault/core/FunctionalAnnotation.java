package org.modelseeed.vault.core;

import org.neo4j.graphdb.Label;

public class FunctionalAnnotation {
  
  public static final Label LABEL = Label.label("FunctionalAnnotation");
  
  private final Label annotationType;
  private final String annotation;
  
  public FunctionalAnnotation(String annotationType, String annotation) {
    this.annotationType = Label.label(annotationType);
    this.annotation = annotation;
  }

  public String getAnnotation() {
    return annotation;
  }

  public Label getAnnotationType() {
    return annotationType;
  }
}
