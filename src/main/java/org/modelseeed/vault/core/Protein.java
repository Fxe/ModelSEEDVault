package org.modelseeed.vault.core;

import java.nio.charset.StandardCharsets;
import org.neo4j.graphdb.Label;
import com.google.common.hash.Hashing;

public class Protein {
  
  public static final Label LABEL = Label.label("Protein");
  
  private final String sequence;
  private final String sha256;
  private final String md5;
  
  @SuppressWarnings("deprecation")
  public Protein(String sequence) {
    this.sequence = sequence;
    this.sha256 = Hashing.sha256().hashString(sequence, StandardCharsets.UTF_8).toString();
    this.md5 = Hashing.md5().hashString(sequence, StandardCharsets.UTF_8).toString();
    // TODO Auto-generated constructor stub
  }

  public String getSequence() {
    return sequence;
  }
  
  public String getHash() {
    return sha256;
  }
  
  public String getMd5() {
    return md5;
  }
  
  @Override
  public String toString() {
    return sha256;
  }
}
