package org.modelseeed.vault.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.modelseeed.vault.core.DNASequence;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dna")
@CrossOrigin
public class DNASequenceController {
  
  @PostMapping("/bulk")
  public Map<String, String> addProteinBulk(@RequestBody List<String> sequences) throws IOException {
    List<DNASequence> listSequences = sequences.stream()
        .map(seq -> DNASequence.buildFromSequence(seq)).toList();
    return null;
  }
}
