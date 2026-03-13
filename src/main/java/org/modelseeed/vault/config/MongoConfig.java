package org.modelseeed.vault.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

@Configuration
public class MongoConfig {
  
  private static final String MONGO_URI = "mongodb://192.168.1.18:27017";
  private static final String DB_SEQUENCE = "vault_sequence";
  
  @Bean
  MongoClient mongoClient() {
    return MongoClients.create(MONGO_URI);
  }
  
  @Bean
  MongoDatabase sequenceDatabase() {
    return mongoClient().getDatabase(DB_SEQUENCE);
  }

  @Bean
  String proteinCollectionName() {
    return "test_collection";
  }
}
