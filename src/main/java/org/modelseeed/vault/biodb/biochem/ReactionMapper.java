package org.modelseeed.vault.biodb.biochem;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ReactionMapper {

  protected Map<String, Double> r1;
  protected Map<String, Double> r2;
  protected Map<String, String> t;
  protected Set<String> exclusion;

  public enum MatchResult {
    IDENTITY, REVERSE, IDENTITY_EXCLUSION, REVERSE_EXCLUSION, MAPPED
  }

  public ReactionMapper(Map<String, Double> r1, Map<String, Double> r2, Map<String, String> t, Set<String> exclusion) {
    this.r1 = r1;
    this.r2 = r2;
    this.t = t;
    this.exclusion = exclusion;
  }

  /**
   * Translate r1 keys through the translation map t. Keys absent from t are kept
   * as-is. If two keys collide after translation, their coefficients are summed.
   */
  public static Map<String, Double> translate(Map<String, Double> m, Map<String, String> t) {
    Map<String, Double> translated = new HashMap<>();
    for (Map.Entry<String, Double> e : m.entrySet()) {
      String mappedKey = t.getOrDefault(e.getKey(), e.getKey());
      translated.merge(mappedKey, e.getValue(), Double::sum);
    }
    return translated;
  }

  /**
   * Strip exclusion-set keys from a stoichiometry map.
   */
  private Map<String, Double> stripExclusion(Map<String, Double> map) {
    Map<String, Double> result = new HashMap<>(map);
    exclusion.forEach(result::remove);
    return result;
  }

  /**
   * True when a and b have exactly the same keys and, for every key, the
   * coefficients are equal within a small floating-point tolerance.
   */
  private static boolean mapsEqual(Map<String, Double> a, Map<String, Double> b) {
    
    //System.out.println("Equals");
    //System.out.println(a);
    //System.out.println(b);
    
    if (!a.keySet().equals(b.keySet())) {
      return false;
    }
    for (Map.Entry<String, Double> e : a.entrySet()) {
      if (Math.abs(e.getValue() - b.get(e.getKey())) > 1e-9) {
        return false;
      }
    }
    return true;
  }

  /**
   * Return a new map with every coefficient negated (i.e. the reversed reaction).
   */
  private static Map<String, Double> negate(Map<String, Double> map) {
    Map<String, Double> negated = new HashMap<>(map.size());
    map.forEach((k, v) -> negated.put(k, -v));
    return negated;
  }

  public MatchResult match() {
    Map<String, Double> r1t = ReactionMapper.translate(this.r1, this.t);
    
    //System.out.println(r1t);

    // --- strict comparisons ---
    if (mapsEqual(r1t, this.r2)) {
      return MatchResult.IDENTITY;
    }
    if (mapsEqual(r1t, negate(this.r2))) {
      return MatchResult.REVERSE;
    }

    // --- exclusion-relaxed comparisons ---
    Map<String, Double> r1tStripped = stripExclusion(r1t);
    Map<String, Double> r2Stripped = stripExclusion(r2);

    if (mapsEqual(r1tStripped, r2Stripped)) {
      return MatchResult.IDENTITY_EXCLUSION;
    }
    if (mapsEqual(r1tStripped, negate(r2Stripped))) {
      return MatchResult.REVERSE_EXCLUSION;
    }

    return null;
  }
}
