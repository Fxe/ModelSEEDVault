package org.modelseeed.vault.repository;

import org.springframework.stereotype.Repository;
import org.modelseeed.vault.core.FunctionalAnnotation;
import org.modelseeed.vault.core.Protein;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

@Repository
public class ProteinRepositoryNeo4j {
  
  private final GraphDatabaseService db;
  
  public ProteinRepositoryNeo4j(GraphDatabaseService db) {
    this.db = db;
  }
  
  public long countProteins() {
    try (Transaction tx = db.beginTx()) {
        return tx.findNodes(Protein.LABEL).stream().count();
    }
  }
  
  public void addAnnotationToProtein(Protein protein, FunctionalAnnotation annotation) {
    Node proteinNode = this.getProteinNode(protein);
    if (proteinNode == null) {
      return;
    }
    Node annotationNode = this.getAnnotationNode(annotation);
  }
  
  public Node getAnnotationNode(FunctionalAnnotation annotation) {
    try (Transaction tx = db.beginTx()) {
      Node node = tx.findNode(FunctionalAnnotation.LABEL, "annotation", annotation.getAnnotation());
      if (node != null) {
        return node;
      }
    }
    return null;
  }
  
  public Node getProteinNode(Protein protein) {
    try (Transaction tx = db.beginTx()) {
      if (protein.getHash() != null) {
        Node node = tx.findNode(Protein.LABEL, "sha256", protein.getHash());
        if (node != null) {
          return node;
        }
      }
    }
    return null;
  }
  
  public Protein getProteinBySha256(String sha256) {
    try (Transaction tx = db.beginTx()) {
      Node node = tx.findNode(Protein.LABEL, "sha256", sha256);
      if (node != null) {
        Protein protein = new Protein("XXX");
        return protein;
      }
      tx.commit();
    }
    return null;
  }
  
  public Protein getProteinBySequence(String sequence) {
    return null;
  }
  
  public boolean createProteinIfNotExists(String sequence) {
    Protein protein = new Protein(sequence);
    try (Transaction tx = db.beginTx()) {
        boolean exists = tx.findNodes(Protein.LABEL)
                           .stream()
                           .anyMatch(node -> protein.getHash().equals(node.getProperty("sha256", null)));

        if (!exists) {
            var node = tx.createNode(Protein.LABEL);
            node.setProperty("sha256", protein.getHash());
        }
        tx.commit();
        return exists;
    }
  }
  
  public boolean createAnnotationIfNotExists(FunctionalAnnotation annotation) {
    try (Transaction tx = db.beginTx()) {
      Node node = this.getAnnotationNode(annotation);
        boolean exists = node != null;

        if (!exists) {
          node = tx.createNode(FunctionalAnnotation.LABEL);
          node.addLabel(annotation.getAnnotationType());
          node.setProperty("_created_at", 0);
          node.setProperty("_updated_at", 0);
          node.setProperty("annotation", annotation.getAnnotation());
        }
        tx.commit();
        return exists;
    }
  }
}
