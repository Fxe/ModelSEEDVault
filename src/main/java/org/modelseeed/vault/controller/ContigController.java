package org.modelseeed.vault.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/contig")
@CrossOrigin
public class ContigController {
  
  @PostMapping("/add")
  public String addContig(@RequestParam String sequence) {
      return null;
  }
}
