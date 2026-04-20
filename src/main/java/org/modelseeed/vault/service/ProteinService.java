package org.modelseeed.vault.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
  
  public Map<String, String> addProteins(List<ProteinSequence> proteins) throws IOException {
    Map<String, String> result = new HashMap<>();
    try (Transaction tx = this.proteinGraphRepository.beginTx()) {
      for (ProteinSequence protein: proteins) {
        CreateIfNotExistsResult res = this.proteinGraphRepository.createProteinIfNotExists(protein, tx);
        if (res.created()) {
          this.sequenceRepository.storeSequence(protein);      
        }
        result.put(protein.getHash(), res.elmenetId());
      }
      
      tx.commit();

      return result;
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