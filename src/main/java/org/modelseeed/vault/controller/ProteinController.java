package org.modelseeed.vault.controller;

import java.io.IOException;
import java.util.Map;

import org.modelseeed.vault.core.Protein;
import org.modelseeed.vault.service.ProteinService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/protein")
@CrossOrigin
public class ProteinController {
  
  private ProteinService proteinService;
  
  public ProteinController(ProteinService proteinService) {
    this.proteinService = proteinService;
  }
  
  @PostMapping("/")
  public boolean addProtein(@RequestBody String sequence) throws IOException {
    Protein protein = Protein.buildFromSequence(sequence);
    return proteinService.addProtein(protein);
  }
  
  @GetMapping("/sha256/{sha256}")
  public Protein getProteinBySha256(@PathVariable String sha256) throws IOException {
      return this.proteinService.getProteinBySha256(sha256);
  }

  @GetMapping("/sequence/{sequence}")
  public Protein getProteinBySequence(@PathVariable String sequence) {
    Protein protein = Protein.buildFromSequence(sequence);
      return this.proteinService.getProtein(protein);
  }
  
  @PostMapping("/annotation/{sha256}/{type}")
  public void addProteinAnnotation(@PathVariable String sha256, 
                                   @PathVariable String type,
                                   @RequestBody Map<String, Object> method) throws IOException {
    Protein protein = this.getProteinBySha256(sha256);
    if (protein == null) {
      throw new IllegalArgumentException("Protein not found: " + sha256);
    }
    System.out.println(sha256 + " " + type);
    System.out.println(method);
  }

  @GetMapping("/count")
  public long countProteins() {
      return this.proteinService.countProteins();
  }
}
