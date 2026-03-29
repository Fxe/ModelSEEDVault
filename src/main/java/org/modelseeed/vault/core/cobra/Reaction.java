package org.modelseeed.vault.core.cobra;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a biochemical reaction in the genome-scale metabolic model (GEM).
 *
 * <p>The {@code metabolites} map holds stoichiometric coefficients keyed by
 * metabolite ID. Negative values indicate consumed reactants; positive values
 * indicate produced products.</p>
 *
 * <p>{@code lowerBound} and {@code upperBound} define the flux boundaries
 * (typically -1000 / +1000 for reversible, 0 / +1000 for irreversible).</p>
 */
public class Reaction {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    /**
     * Stoichiometric coefficients: metaboliteId → coefficient.
     * Negative = reactant (consumed), Positive = product (produced).
     */
    @JsonProperty("metabolites")
    private Map<String, Double> metabolites;

    @JsonProperty("lower_bound")
    private double lowerBound;

    @JsonProperty("upper_bound")
    private double upperBound;

    /** Boolean gene-reaction rule, e.g. "ACIAD1134 or ACIAD3648". */
    @JsonProperty("gene_reaction_rule")
    private String geneReactionRule;

    @JsonProperty("notes")
    private Map<String, String> notes;

    public Reaction() {}

    public Reaction(String id, String name, Map<String, Double> metabolites,
                    double lowerBound, double upperBound,
                    String geneReactionRule, Map<String, String> notes) {
        this.id = id;
        this.name = name;
        this.metabolites = metabolites;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.geneReactionRule = geneReactionRule;
        this.notes = notes;
    }

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Map<String, Double> getMetabolites() { return metabolites; }
    public void setMetabolites(Map<String, Double> metabolites) { this.metabolites = metabolites; }

    public double getLowerBound() { return lowerBound; }
    public void setLowerBound(double lowerBound) { this.lowerBound = lowerBound; }

    public double getUpperBound() { return upperBound; }
    public void setUpperBound(double upperBound) { this.upperBound = upperBound; }

    public String getGeneReactionRule() { return geneReactionRule; }
    public void setGeneReactionRule(String geneReactionRule) { this.geneReactionRule = geneReactionRule; }

    public Map<String, String> getNotes() { return notes; }
    public void setNotes(Map<String, String> notes) { this.notes = notes; }

    /** Convenience: true when lowerBound < 0, meaning the reaction can run in reverse. */
    public boolean isReversible() {
        return lowerBound < 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reaction)) return false;
        Reaction that = (Reaction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Reaction{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", lowerBound=" + lowerBound +
                ", upperBound=" + upperBound +
                ", geneReactionRule='" + geneReactionRule + '\'' +
                '}';
    }
}