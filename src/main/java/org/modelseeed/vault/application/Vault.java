package org.modelseeed.vault.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

//@SpringBootApplication(exclude = {MongoAutoConfiguration.class})
@SpringBootApplication
@ComponentScan("org.modelseeed")
public class Vault {
  public static void main(String[] args) {
    SpringApplication.run(Vault.class, args);
  }
}
