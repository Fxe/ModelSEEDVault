package org.modelseeed.vault.core;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.modelseeed.vault.biodb.OntologyBiodb;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import com.google.common.hash.Hashing;

public class ProteinSequence extends AbstractSequence {
  
  // Standard amino acid vocabulary (20 standard amino acids plus common variants)
  public static final Set<Character> DEFAULT_PROTEIN_VOCAB = Set.of(
      'A', 'R', 'N', 'D', 'C', 'Q', 'E', 'G', 'H', 'I', 
      'L', 'K', 'M', 'F', 'P', 'S', 'T', 'W', 'Y', 'V',
      'U', 'O', 'B', 'Z', 'J', 'X', '*', '-'  // Extended amino acids and gap characters
  );
  
  public static final Label LABEL = OntologyBiodb.ProteinSequence;
  
  public ProteinSequence(String sequence, String sha256) {
    super(sequence, sha256, LABEL.name());
  }

  public ProteinSequence(String sequence, Node node) {
    super(sequence, node);
  }
  
  public static ProteinSequence buildFromNodeAndSequence(Node node, String sequence) {
    ProteinSequence protein = new ProteinSequence(sequence, node);
    return protein;
  }
  
  public static ProteinSequence buildFromSequence(String sequence) {
    sequence = sequence.strip().replace("\r", "").replace("\n", "").replace("\"", "");
    sequence = sequence.toUpperCase();
    if (!ProteinSequence.validateSequence(sequence, ProteinSequence.DEFAULT_PROTEIN_VOCAB)) {
      throw new IllegalArgumentException("bad sequence: " + sequence);
    }
    String sha256 = Hashing.sha256().hashString(sequence, StandardCharsets.UTF_8).toString();
    
    return new ProteinSequence(sequence, sha256);
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

  @Override
  public Set<Character> getVocab() {
    return DEFAULT_PROTEIN_VOCAB;
  }
}
