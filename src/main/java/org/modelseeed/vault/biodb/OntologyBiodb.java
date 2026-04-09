package org.modelseeed.vault.biodb;

import org.neo4j.graphdb.Label;

public enum OntologyBiodb implements Label {
  UniProKBSwissProt,
  UniProKBTrEMBL,
  UniProKBAccession,
  UniProKBSubcellularLocation,
  Name,
  ProteinSequence,
  DNASequence,
  OntologyECO,
}
