package org.modelseeed.vault.core.cobra;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * Represents a gene in the genome-scale metabolic model (GEM).
 *
 * <p>Gene IDs follow the ACIAD locus-tag format (e.g., {@code ACIAD1134})
 * used in <em>Acinetobacter baylyi</em> ADP1. The {@code name} field mirrors
 * the locus tag when no common gene name is available.</p>
 */
public class Gene {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    public Gene() {}

    public Gene(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Gene)) return false;
        Gene that = (Gene) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Gene{id='" + id + "', name='" + name + "'}";
    }
}