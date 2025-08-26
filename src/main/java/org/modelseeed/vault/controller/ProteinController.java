package org.modelseeed.vault.controller;

import java.io.IOException;
import java.util.Map;

import org.modelseeed.vault.core.Protein;
import org.modelseeed.vault.service.ProteinService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/protein")
public class ProteinController {
  
  private ProteinService proteinService;
  
  public ProteinController(ProteinService proteinService) {
    this.proteinService = proteinService;
  }
  
  @PostMapping("/add")
  public boolean addProtein(@RequestParam String sequence) throws IOException {
    return proteinService.addProtein(sequence);
  }
  
  @GetMapping("/get/sha256/{sha256}")
  public Protein getProteinBySha256(@PathVariable String sha256) {
      return this.proteinService.getProteinBySha256(sha256);
  }

  @GetMapping("/get/sequence/{sequence}")
  public Protein getProteinBySequence(@PathVariable String sequence) {
      return this.proteinService.getProteinBySequence(sequence);
  }
  
  @PostMapping("/annotation/{sha256}/{type}")
  public void addProteinAnnotation(@PathVariable String sha256, 
                                   @PathVariable String type,
                                   @RequestParam Map<String, Object> method) {
    System.out.println(sha256 + " " + type);
    System.out.println(method);
  }

  @GetMapping("/count")
  public long countProteins() {
      return this.proteinService.countProteins();
  }
}
