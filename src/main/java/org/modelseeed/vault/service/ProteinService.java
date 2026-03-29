package org.modelseeed.vault.service;

import java.io.IOException;

import org.modelseeed.vault.core.Protein;
import org.modelseeed.vault.repository.ProteinRepositoryMongo;
import org.modelseeed.vault.repository.ProteinRepositoryNeo4j;
import org.neo4j.graphdb.Transaction;
import org.springframework.stereotype.Service;

@Service
public class ProteinService {
  
  private final ProteinRepositoryNeo4j proteinGraphRepository;
  private final ProteinRepositoryMongo sequenceRepository;
  
  public ProteinService(ProteinRepositoryNeo4j proteinRepository, 
      ProteinRepositoryMongo sequenceRepository) {
    this.proteinGraphRepository = proteinRepository;
    this.sequenceRepository = sequenceRepository;
  }
  
  public boolean addProtein(Protein protein) throws IOException {
    boolean exists = this.proteinGraphRepository.createProteinIfNotExists(protein);
    if (!exists) {
      this.sequenceRepository.storeSequence(protein.getSequence());      
    }
    return exists;
  }
  
  public void addAnnotationToProtein(Protein protein, String annotation) {
    
  }

  public Protein getProteinBySha256(String sha256) throws IOException {
    try (Transaction tx = this.proteinGraphRepository.beginTx()) {
      String sequence = this.sequenceRepository.getSequence(sha256);
      Protein protein = Protein.buildFromSequence(sequence);
      return proteinGraphRepository.getProtein(protein, tx);
    }

  }

  public Protein getProtein(Protein protein) {
    try (Transaction tx = this.proteinGraphRepository.beginTx()) {
      return this.proteinGraphRepository.getProtein(protein, tx);
    }
  }

  public long countProteins() {
      return proteinGraphRepository.countProteins();
  }
}