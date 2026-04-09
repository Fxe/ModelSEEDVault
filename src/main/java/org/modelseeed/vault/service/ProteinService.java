package org.modelseeed.vault.service;

import java.io.IOException;

import org.modelseeed.vault.core.ProteinSequence;
import org.modelseeed.vault.repository.ProteinRepositoryMongo;
import org.modelseeed.vault.repository.ProteinRepositoryNeo4j;
import org.modelseeed.vault.repository.ProteinRepositoryNeo4j.CreateIfNotExistsResult;
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
  
  public String addProtein(ProteinSequence protein) throws IOException {
    try (Transaction tx = this.proteinGraphRepository.beginTx()) {
      CreateIfNotExistsResult res = this.proteinGraphRepository.createProteinIfNotExists(protein, tx);
      tx.commit();
      System.out.println(res.elmenetId() + " " + res.created());
      if (res.created()) {
        this.sequenceRepository.storeSequence(protein);      
      }
      return res.elmenetId();
    }

  }
  
  public void addAnnotationToProtein(ProteinSequence protein, String annotation) {
    
  }

  public ProteinSequence getProteinBySha256(String sha256) throws IOException {
    try (Transaction tx = this.proteinGraphRepository.beginTx()) {
      String sequence = this.sequenceRepository.getSequence(sha256);
      ProteinSequence protein = ProteinSequence.buildFromSequence(sequence);
      return proteinGraphRepository.getProtein(protein, tx);
    }
  }

  public ProteinSequence getProtein(ProteinSequence protein) {
    try (Transaction tx = this.proteinGraphRepository.beginTx()) {
      return this.proteinGraphRepository.getProtein(protein, tx);
    }
  }

  public long countProteins() {
    try (Transaction tx = this.proteinGraphRepository.beginTx()) {
      return proteinGraphRepository.countProteins(tx);
    }
  }
}