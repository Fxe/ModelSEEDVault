package org.modelseeed.vault.core;

import java.util.Set;

import org.modelseeed.vault.biodb.OntologyBiodb;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

public class DNASequence extends AbstractSequence {
  
  public static final Set<Character> DEFAULT_VOCAB = Set.of(
      'A', 'T', 'G', 'C'
  );
  
  public static final Label LABEL = OntologyBiodb.DNASequence;
  
  public DNASequence(String sequence, String sha256) {
    super(sequence, sha256, LABEL.name());
  }
  
  public DNASequence(String sequence, Node node) {
    super(sequence, node);
  }
  
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
    return DEFAULT_VOCAB;
  }
}
