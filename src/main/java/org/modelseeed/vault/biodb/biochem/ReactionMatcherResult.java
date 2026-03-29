package org.modelseeed.vault.biodb.biochem;

import java.util.Map;
import java.util.Set;

import org.modelseeed.vault.biodb.biochem.ReactionMapper.MatchResult;
import org.modelseeed.vault.neo4j.cobra.Neo4jMetabolite;
import org.neo4j.graphdb.Node;

public class ReactionMatcherResult {
  
  private final Map<Node, MatchResult> match;
  private final Map<Neo4jMetabolite, Node> translation;
  private final Set<String> exclude;
  
  public ReactionMatcherResult(Map<Neo4jMetabolite, Node> translation, 
      Map<Node, MatchResult> match, Set<String> exclude) {
    this.translation = translation;
    this.match = match;
    this.exclude = exclude;
  }

  public Map<Node, MatchResult> getMatch() {
    return match;
  }

  public Map<Neo4jMetabolite, Node> getTranslation() {
    return translation;
  }

  public Set<String> getExclude() {
    return exclude;
  }
  
  
}
