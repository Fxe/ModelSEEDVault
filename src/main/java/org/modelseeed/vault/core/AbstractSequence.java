package org.modelseeed.vault.core;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.neo4j.graphdb.Node;

import com.google.common.hash.Hashing;

public abstract class AbstractSequence extends Neo4jNodeEntity {
  
  private final String sequence;
  //private final String sha256;
  private final String md5;
  
  @SuppressWarnings("deprecation")
  public AbstractSequence(String sequence, String sha256, String sequenceType) {
    super(sha256, sequenceType);
    this.sequence = sequence.toUpperCase().trim();
    if (!this.validateSequence(sequence)) {
      throw new IllegalArgumentException("bad sequence");
    }
    String sha256FromSeq = Hashing.sha256().hashString(sequence, StandardCharsets.UTF_8).toString();
    if (!sha256FromSeq.equals(sha256)) {
      throw new IllegalArgumentException("sha256 mismatch");
    }
    this.md5 = Hashing.md5().hashString(sequence, StandardCharsets.UTF_8).toString();
  }
  
  @SuppressWarnings("deprecation")
  public AbstractSequence(String sequence, Node node) {
    super(node);
    String sha256FromSeq = Hashing.sha256().hashString(sequence, StandardCharsets.UTF_8).toString();
    if (!sha256FromSeq.equals(this.entry)) {
      throw new IllegalArgumentException("sha256 mismatch");
    }
    this.sequence = sequence.toUpperCase().trim();
    this.md5 = Hashing.md5().hashString(sequence, StandardCharsets.UTF_8).toString();
  }

  public String getSequence() {
    return sequence;
  }
  
  public String getHash() {
    return this.entry;
  }
  
  public String getMd5() {
    return md5;
  }
  
  @Override
  public String toString() {
    return this.entry;
  }
  
  /**
   * Validates a protein sequence against a vocabulary of allowed characters
   * @param sequence The protein sequence to validate
   * @param vocab Set of allowed characters (amino acids)
   * @return true if sequence is valid, false otherwise
   */
  public boolean validateSequence(String sequence) {
    if (sequence == null || sequence.isEmpty()) {
      return false;
    }
    
    // Convert to uppercase for validation
    String upperSequence = sequence.toUpperCase();
    
    for (char c : upperSequence.toCharArray()) {
        if (!this.getVocab().contains(c)) {
            return false;
        }
    }
    return true;
  }
  
  public abstract Set<Character> getVocab();
}
