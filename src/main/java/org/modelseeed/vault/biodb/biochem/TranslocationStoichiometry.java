package org.modelseeed.vault.biodb.biochem;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

public class TranslocationStoichiometry<M, C> extends Stoichiometry<Entry<M, C>> {

  public void addMetabolite(M metabolite, C compartment, double value) {
    this.s.merge(new SimpleEntry<>(metabolite, compartment), value, Double::sum);
  }
  
  public Set<C> getCompartments() {
    //Set<C> result = //new HashSet<>();
//    for ( this.s.keySet())
    return this.s.keySet().stream()
                          .map(Entry::getValue)
                          .distinct()
                          .collect(Collectors.toSet());
  }
  
  public boolean isTranslocation() {
    return this.s.keySet().stream()
        .map(Entry::getValue)
        .distinct()
        .limit(2)
        .count() > 1;
  }
  
  @Override
  public String toString() {
      Map<Entry<M, C>, Double> stoich = this.s;

      // Determine if all compartments are the same
      Set<C> compartments = stoich.keySet().stream()
          .map(Entry::getValue)
          .collect(Collectors.toSet());
      boolean singleCompartment = compartments.size() == 1;

      // Split into reactants (negative) and products (positive)
      List<String> reactants = new ArrayList<>();
      List<String> products  = new ArrayList<>();

      for (Map.Entry<Entry<M, C>, Double> e : stoich.entrySet()) {
          M metabolite  = e.getKey().getKey();
          C compartment = e.getKey().getValue();
          double coeff  = e.getValue();

          String label = singleCompartment
              ? metabolite.toString()
              : metabolite.toString() + "[" + compartment + "]";

          double abs = Math.abs(coeff);
          String term = (abs == 1.0 ? "" : (abs % 1 == 0
              ? String.valueOf((int) abs)
              : String.valueOf(abs)) + " ") + label;

          if (coeff < 0) reactants.add(term);
          else           products.add(term);
      }

      String lhs = String.join(" + ", reactants);
      String rhs = String.join(" + ", products);
      String arrow = lhs + " ==> " + rhs;

      if (singleCompartment) {
          C compartment = compartments.iterator().next();
          return "[" + compartment + "] " + arrow;
      }
      return arrow;
  }
}