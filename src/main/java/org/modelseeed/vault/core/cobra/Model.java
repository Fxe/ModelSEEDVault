package org.modelseeed.vault.core.cobra;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Root object representing a genome-scale metabolic model (GEM) in JSON format.
 *
 * <p>Corresponds to the top-level structure of {@code iAbaylyiv4_gem.json},
 * which describes the metabolic network of <em>Acinetobacter baylyi</em> ADP1.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * GemModel model = GemModel.fromFile("iAbaylyiv4_gem.json");
 * System.out.println("Model: " + model.getName());
 * System.out.println("Metabolites: " + model.getMetabolites().size());
 * System.out.println("Reactions:   " + model.getReactions().size());
 * System.out.println("Genes:       " + model.getGenes().size());
 * }</pre>
 */
public class Model {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("version")
    private String version;

    @JsonProperty("compartments")
    private Map<String, String> compartments;

    @JsonProperty("metabolites")
    private List<Metabolite> metabolites;

    @JsonProperty("reactions")
    private List<Reaction> reactions;

    @JsonProperty("genes")
    private List<Gene> genes;

    public Model(String id, String name, String version) {
      this.id = id;
      this.name = name;
      this.version = version;
      
      this.compartments = new HashMap<>();
      this.metabolites = new ArrayList<>();
      this.reactions = new ArrayList<>();
      this.genes = new ArrayList<>();
    }

    // ── Factory / deserialization ──────────────────────────────────────────────

    /**
     * Deserializes a GEM JSON file into a {@link Model} instance.
     *
     * @param filePath path to the JSON file
     * @return populated {@link Model}
     * @throws IOException if the file cannot be read or parsed
     */
    public static Model fromFile(String filePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File(filePath), Model.class);
    }

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public Map<String, String> getCompartments() { return compartments; }
    public void setCompartments(Map<String, String> compartments) { this.compartments = compartments; }

    public List<Metabolite> getMetabolites() { return metabolites; }
    public void setMetabolites(List<Metabolite> metabolites) { this.metabolites = metabolites; }

    public List<Reaction> getReactions() { return reactions; }
    public void setReactions(List<Reaction> reactions) { this.reactions = reactions; }

    public List<Gene> getGenes() { return genes; }
    public void setGenes(List<Gene> genes) { this.genes = genes; }

    // ── Convenience helpers ────────────────────────────────────────────────────

    /** Returns the total number of metabolites in the model. */
    public int metaboliteCount() {
        return metabolites == null ? 0 : metabolites.size();
    }

    /** Returns the total number of reactions in the model. */
    public int reactionCount() {
        return reactions == null ? 0 : reactions.size();
    }

    /** Returns the total number of genes in the model. */
    public int geneCount() {
        return genes == null ? 0 : genes.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Model)) return false;
        Model that = (Model) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "GemModel{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", metabolites=" + metaboliteCount() +
                ", reactions=" + reactionCount() +
                ", genes=" + geneCount() +
                '}';
    }
}