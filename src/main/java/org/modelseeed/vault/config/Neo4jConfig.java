package org.modelseeed.vault.config;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Neo4jConfig {

  private static final String DEFAULT_DATABASE_NAME = "neo4j";

  @Value("${vault.neo4j.path}")
  private String neo4jPath;

  @Value("${vault.neo4j.pagecache-memory-mb}")
  private long pagecacheMemoryMb;

  @Value("${vault.neo4j.transaction-timeout-seconds}")
  private long transactionTimeoutSeconds;

  @Value("${vault.neo4j.preallocate-logical-logs}")
  private boolean preallocateLogicalLogs;

  private DatabaseManagementService dbms;

  @Bean
  GraphDatabaseService graphDatabaseService() {
    dbms = new DatabaseManagementServiceBuilder(Paths.get(neo4jPath))
          .setConfig(GraphDatabaseSettings.pagecache_memory, ByteUnit.mebiBytes(pagecacheMemoryMb))
          .setConfig(GraphDatabaseSettings.transaction_timeout, Duration.ofSeconds(transactionTimeoutSeconds))
          .setConfig(GraphDatabaseSettings.preallocate_logical_logs, preallocateLogicalLogs)
          .build();
    GraphDatabaseService db = dbms.database(DEFAULT_DATABASE_NAME);

    try (Transaction tx = db.beginTx()) {
        tx.schema()
          .constraintFor(Label.label("ProteinSequence"))
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
