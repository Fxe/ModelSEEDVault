package org.modelseeed.vault.repository;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.modelseeed.vault.biodb.OntologyBiodb;
import org.modelseeed.vault.core.FunctionalAnnotation;
import org.modelseeed.vault.core.Neo4jNodeEntity;
import org.modelseeed.vault.core.ProteinSequence;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.springframework.stereotype.Repository;

@Repository
public class ProteinRepositoryNeo4j extends GraphRepository{
  
  public ProteinRepositoryNeo4j(GraphDatabaseService db) {
    super(db);
  }
  
  public long countProteins(Transaction tx) {
        return tx.findNodes(ProteinSequence.LABEL).stream().count();
  }
  
  public void addAnnotationToProtein(ProteinSequence protein, FunctionalAnnotation annotation) {
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
  
  public Node getProteinNode(ProteinSequence protein) {
    try (Transaction tx = db.beginTx()) {
      if (protein.getHash() != null) {
        Node node = tx.findNode(ProteinSequence.LABEL, "sha256", protein.getHash());
        if (node != null) {
          return node;
        }
      }
    }
    return null;
  }
  
  /**
  public Protein getProteinBySha256(String sha256) {
    try (Transaction tx = db.beginTx()) {
      Node node = tx.findNode(Protein.LABEL, "sha256", sha256);
      if (node != null) {
        Protein protein = Protein.buildFromNodeAndSequence(node, sha256);
        return protein;
      }
      tx.commit();
    }
    return null;
  }
  **/
  
  public ProteinSequence getProtein(ProteinSequence protein, Transaction tx) {
    Neo4jNodeEntity node = this.getNode(protein.getHash(), OntologyBiodb.ProteinSequence.name(), tx);
    //Neo4jNodeEntity node = this.getNode(protein.getHash(), tx);
    ProteinSequence res = new ProteinSequence(protein.getSequence(), (String) node.getProperties().get("key"));
    return res;
  }
  
  public static record CreateIfNotExistsResult(String elmenetId, boolean created) {}
  
  public CreateIfNotExistsResult createProteinIfNotExists(ProteinSequence protein, Transaction tx) {
      Node nodeFound = tx.findNode(OntologyBiodb.ProteinSequence, "key", protein.getHash());
      /**
        boolean exists = tx.findNodes(Protein.LABEL)
                           .stream()
                           .anyMatch(node -> protein.getHash().equals(node.getProperty("key", null)));
                           **/
      boolean exists = nodeFound != null;

        if (!exists) {
          Map<String, Object> properties = new HashMap<>();
          Set<String> labels = new HashSet<>();
          Neo4jNodeEntity node = new Neo4jNodeEntity(protein.getHash(), 
              OntologyBiodb.ProteinSequence, labels, properties);
          Neo4jNodeEntity res = this.addNode(node, tx);
          
          //this.addNode(, "Protein", properties, tx);
            //var node = tx.createNode(Protein.LABEL);
            //node.setProperty("key", protein.getHash());
            //node.setProperty("type", "Protein");
          return new CreateIfNotExistsResult(res.getElementId(), true);
        } else {
          
          return new CreateIfNotExistsResult(nodeFound.getElementId(), false);          
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
