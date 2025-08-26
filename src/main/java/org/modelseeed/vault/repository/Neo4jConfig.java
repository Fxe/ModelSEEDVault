package org.modelseeed.vault.repository;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

import javax.annotation.PreDestroy;

import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Transaction;
import org.neo4j.io.ByteUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Neo4jConfig {
  
  private final String DEFAULT_DATABASE_NAME = "neo4j";
  static Path DEFAULT_DATABASE_PATH = Paths.get("graphdb");
  
  private DatabaseManagementService dbms;
  
  @Bean
  GraphDatabaseService graphDatabaseService() {
    dbms = new DatabaseManagementServiceBuilder(DEFAULT_DATABASE_PATH)
          .setConfig(GraphDatabaseSettings.pagecache_memory, ByteUnit.mebiBytes(512))
          .setConfig(GraphDatabaseSettings.transaction_timeout, Duration.ofSeconds(60))
          .setConfig(GraphDatabaseSettings.preallocate_logical_logs, true)
          .build();
      GraphDatabaseService db = dbms.database(DEFAULT_DATABASE_NAME);

      try (Transaction tx = db.beginTx()) {
          tx.schema()
            .constraintFor(Label.label("Protein"))
            .assertPropertyIsUnique("sha256")
            .withName("protein_sha256_unique")
            .create();
          tx.commit();
      } catch (Exception e) {
          System.out.println("Constraint may already exist: " + e.getMessage());
      }

      return db;
  }
  
  @PreDestroy
  public void shutdown() {
    if (dbms != null) {
        dbms.shutdown();
    }
  }
}
