package org.modelseeed.vault.core;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import com.google.common.hash.Hashing;

public class Protein extends Neo4jNodeEntity{
  
  // Standard amino acid vocabulary (20 standard amino acids plus common variants)
  private static final Set<Character> DEFAULT_PROTEIN_VOCAB = Set.of(
      'A', 'R', 'N', 'D', 'C', 'Q', 'E', 'G', 'H', 'I', 
      'L', 'K', 'M', 'F', 'P', 'S', 'T', 'W', 'Y', 'V',
      'U', 'O', 'B', 'Z', 'J', 'X', '*', '-'  // Extended amino acids and gap characters
  );
  
  public static final Label LABEL = Label.label("Protein");
  
  private final String sequence;
  //private final String sha256;
  private final String md5;
  
  @SuppressWarnings("deprecation")
  public Protein(String sequence, String sha256) {
    super(sha256, LABEL.name());
    this.sequence = sequence.toUpperCase().trim();
    if (!Protein.validateSequence(sequence)) {
      throw new IllegalArgumentException("bad sequence");
    }
    String sha256FromSeq = Hashing.sha256().hashString(sequence, StandardCharsets.UTF_8).toString();
    if (!sha256FromSeq.equals(sha256)) {
      throw new IllegalArgumentException("sha256 mismatch");
    }
    this.md5 = Hashing.md5().hashString(sequence, StandardCharsets.UTF_8).toString();
  }
  
  @SuppressWarnings("deprecation")
  public Protein(String sequence, Node node) {
    super(node);
    String sha256FromSeq = Hashing.sha256().hashString(sequence, StandardCharsets.UTF_8).toString();
    if (!sha256FromSeq.equals(this.entry)) {
      throw new IllegalArgumentException("sha256 mismatch");
    }
    this.sequence = sequence.toUpperCase().trim();
    this.md5 = Hashing.md5().hashString(sequence, StandardCharsets.UTF_8).toString();
  }
  
  public static Protein buildFromNodeAndSequence(Node node, String sequence) {
    Protein protein = new Protein(sequence, node);
    return protein;
  }
  
  public static Protein buildFromSequence(String sequence) {
    sequence = sequence.toUpperCase().trim();
    if (!Protein.validateSequence(sequence)) {
      throw new IllegalArgumentException("bad sequence");
    }
    String sha256 = Hashing.sha256().hashString(sequence, StandardCharsets.UTF_8).toString();
    
    return new Protein(sequence, sha256);
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
  public static boolean validateSequence(String sequence, Set<Character> vocab) {
      if (sequence == null || sequence.isEmpty()) {
          return false;
      }
      
      // Convert to uppercase for validation
      String upperSequence = sequence.toUpperCase();
      
      for (char c : upperSequence.toCharArray()) {
          if (!vocab.contains(c)) {
              return false;
          }
      }
      return true;
  }
  
  /**
   * Validates a protein sequence against the default amino acid vocabulary
   * @param sequence The protein sequence to validate
   * @return true if sequence is valid, false otherwise
   */
  public static boolean validateSequence(String sequence) {
      return validateSequence(sequence, DEFAULT_PROTEIN_VOCAB);
  }
}
