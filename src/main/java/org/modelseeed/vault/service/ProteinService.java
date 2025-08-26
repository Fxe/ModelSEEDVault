package org.modelseeed.vault.service;

import java.io.IOException;

import org.modelseeed.vault.core.Protein;
import org.modelseeed.vault.repository.ProteinRepositoryMongo;
import org.modelseeed.vault.repository.ProteinRepositoryNeo4j;
import org.springframework.stereotype.Service;

@Service
public class ProteinService {
  
  private final ProteinRepositoryNeo4j proteinRepository;
  private final ProteinRepositoryMongo sequenceRepository;
  
  public ProteinService(ProteinRepositoryNeo4j proteinRepository, 
      ProteinRepositoryMongo sequenceRepository) {
    this.proteinRepository = proteinRepository;
    this.sequenceRepository = sequenceRepository;
  }
  
  public boolean addProtein(String sequence) throws IOException {
    boolean exists = this.proteinRepository.createProteinIfNotExists(sequence); 
    if (!exists) {
      this.sequenceRepository.storeSequence(sequence);      
    }
    return exists;
  } 

  public Protein getProteinBySha256(String sha256) {
    
    return proteinRepository.getProteinBySha256(sha256);
  }

  public Protein getProteinBySequence(String sequence) {
      return this.proteinRepository.getProteinBySequence(sequence);
  }

  public long countProteins() {
      return proteinRepository.countProteins();
  }
}