package org.modelseeed.vault.biodb.biochem;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class GenericReaction<T> {
  
  protected String id;
  protected Map<T, Double> lhs;
  protected Map<T, Double> rhs;
  
  public GenericReaction(String id, Map<T, Double> lhs, Map<T, Double> rhs) {
    this.id = id;
    this.lhs = lhs;
    this.rhs = rhs;
  }
  
  public String getId() {
    return id;
  }



  public void setId(String id) {
    this.id = id;
  }



  public void mult(double v) {
    
  }
  
  public void reverse() {
    
  }
  
  public Map<T, Double> getLhs() {
    return lhs;
  }



  public void setLhs(Map<T, Double> lhs) {
    this.lhs = lhs;
  }



  public Map<T, Double> getRhs() {
    return rhs;
  }



  public void setRhs(Map<T, Double> rhs) {
    this.rhs = rhs;
  }

  public Map<T, Double> getStoichiometry() {
    Map<T, Double> stoich = new HashMap<>();
    for (Entry<T, Double> e: lhs.entrySet()) {
      stoich.put(e.getKey(), -1 * e.getValue());
    }
    for (Entry<T, Double> e: rhs.entrySet()) {
      stoich.put(e.getKey(), e.getValue());
    }
    return stoich;
  }
  
  public String toString() {
    return String.format("[%s]:", this.id);
  }
}
