package org.modelseeed.vault.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

@Configuration
public class MongoConfig {

  @Value("${vault.mongodb.uri}")
  private String mongoUri;

  @Value("${vault.mongodb.database}")
  private String mongoDatabase;

  @Value("${vault.mongodb.protein-collection}")
  private String proteinCollection;
  
  @Value("${vault.mongodb.dna-collection}")
  private String dnaCollection;

  @Bean
  MongoClient mongoClient() {
    return MongoClients.create(mongoUri);
  }

  @Bean
  MongoDatabase sequenceDatabase() {
    return mongoClient().getDatabase(mongoDatabase);
  }

  @Bean
  String proteinCollectionName() {
    return proteinCollection;
  }
  
  @Bean
  String dnaCollectionName() {
    return dnaCollection;
  }
}
