package org.modelseeed.vault.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/genome")
@CrossOrigin
public class GenomeController {
  
  @PostMapping("/upload/gbff")
  public String upload() {
      return null;
  }
  
  @PostMapping("/contig")
  public String addContig(@RequestParam Map<String, Object> data) {
      return null;
  }
  
  @PostMapping("/cds")
  public String addCodingSequense(@RequestParam Map<String, Object> data) {
      return null;
  }
  
  @PostMapping("/gene")
  public String addGene(@RequestParam Map<String, Object> data) {
      return null;
  }
}
