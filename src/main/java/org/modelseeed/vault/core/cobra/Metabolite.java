package org.modelseeed.vault.core.cobra;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * Represents a metabolite in the genome-scale metabolic model (GEM).
 * Each metabolite has a unique identifier, a human-readable name, and
 * the cellular compartment it belongs to.
 */
public class Metabolite {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("compartment")
    private String compartment;

    public Metabolite() {}

    public Metabolite(String id, String name, String compartment) {
        this.id = id;
        this.name = name;
        this.compartment = compartment;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompartment() {
        return this.compartment;
    }

    public void setCompartment(String compartment) {
        this.compartment = compartment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Metabolite)) return false;
        Metabolite that = (Metabolite) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Metabolite{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", compartment='" + compartment + '\'' +
                '}';
    }
}