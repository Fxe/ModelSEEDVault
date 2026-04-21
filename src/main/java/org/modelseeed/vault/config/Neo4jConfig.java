package org.modelseeed.vault.config;

import java.nio.file.Paths;
import java.time.Duration;

import javax.annotation.PreDestroy;

import org.modelseeed.vault.biodb.OntologyBiodb;
import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.configuration.connectors.BoltConnector;
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
  
  @Value("${vault.neo4j.tx-log-rotation-size-mb}")
  private long txLogRotationSizeMb;

  @Value("${vault.neo4j.tx-logs-to-keep}")
  private String txLogsToKeep;

  @Value("${vault.neo4j.read-threads}")
  private int readThreads;

  private DatabaseManagementService dbms;

  @Bean
  GraphDatabaseService graphDatabaseService() {
    dbms = new DatabaseManagementServiceBuilder(Paths.get(neo4jPath))
          .setConfig(GraphDatabaseSettings.pagecache_memory, ByteUnit.mebiBytes(pagecacheMemoryMb))
          .setConfig(GraphDatabaseSettings.transaction_timeout, Duration.ofSeconds(transactionTimeoutSeconds))
          .setConfig(GraphDatabaseSettings.preallocate_logical_logs, preallocateLogicalLogs)
          .setConfig(GraphDatabaseSettings.logical_log_rotation_threshold, ByteUnit.mebiBytes(txLogRotationSizeMb))
          .setConfig(GraphDatabaseSettings.keep_logical_logs, String.valueOf(txLogsToKeep))
          .setConfig(BoltConnector.thread_pool_max_size, readThreads)
          .build();
    GraphDatabaseService db = dbms.database(DEFAULT_DATABASE_NAME);

    try (Transaction tx = db.beginTx()) {
        tx.schema()
          .constraintFor(OntologyBiodb.ProteinSequence)
          .assertPropertyIsUnique("key")
          .withName("protein_sha256_unique")
          .create();
        tx.schema()
          .constraintFor(OntologyBiodb.DNASequence)
          .assertPropertyIsUnique("key")
          .withName("dna_sha256_unique")
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
