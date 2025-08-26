package org.modelseeed.vault;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.bson.Document;
import org.bson.types.Binary;
import org.modelseeed.vault.core.Compress;
import org.modelseeed.vault.core.Neo4jNodeEntity;
import org.modelseeed.vault.core.Protein;
import org.modelseeed.vault.repository.GraphRepository;
import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.io.ByteUnit;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * Hello world!
 */
public class App {
  
  static String DEFAULT_DATABASE_NAME = "neo4j";
  static Path DEFAULT_DATABASE_PATH = Paths.get("/graphdb");
  
  private static final String MONGO_URI = "mongodb://127.0.0.1:27017";
  private static final String DB_SEQUENCE = "vault_sequence";
  
  public static void neo4jTest() {
    System.out.println("Loading database...");
    DatabaseManagementService managementService = new DatabaseManagementServiceBuilder(DEFAULT_DATABASE_PATH)
        .setConfig(GraphDatabaseSettings.pagecache_memory, ByteUnit.mebiBytes(512))
        .setConfig(GraphDatabaseSettings.transaction_timeout, Duration.ofSeconds(60))
        .setConfig(GraphDatabaseSettings.preallocate_logical_logs, true).build();
    System.out.println("Database loaded!");
    GraphDatabaseService graphDb = managementService.database( DEFAULT_DATABASE_NAME );
    
    Transaction tx = graphDb.beginTx();

    //tx.createNode(null)
    tx.rollback();
    
    System.out.println("Shutdown database...");
    managementService.shutdown();
    System.out.println("Done!");
  }
  
  public static void mongoTest() {
    MongoClient client = MongoClients.create(MONGO_URI);
    MongoDatabase sequenceDatabase = client.getDatabase(DB_SEQUENCE);
    MongoCollection<Document> seqProteins = sequenceDatabase.getCollection("seq_protein");
    Document docProtein = seqProteins.find(new Document("_id", "146f0fe779bddd1b11b83af6aa8db7c3b082f10c335a1e0cda0e151ede72b499")).first();
    
    Binary zData = docProtein.get("z_seq", Binary.class);
    try {
      String seq = Compress.decompress(zData);
      System.out.println(seq);
      byte[] bin1 = zData.getData();
      byte[] bin2 = Compress.compress(seq);
      System.out.println(bin1.length);
      System.out.println(bin2.length);
      for (int i = 0; i < bin1.length; i++) {
        if (bin1[i] != bin2[i]) {
          System.out.println(i + " " + bin1[i] + " " + bin2[i]);
          byte b = bin1[i];
          for (int j = 7; j >= 0; j--) {
            int bit = (b >> j) & 1;
            System.out.print(bit);
          }
          System.out.println();
          b = bin2[i];
          for (int j = 7; j >= 0; j--) {
            int bit = (b >> j) & 1;
            System.out.print(bit);
          }
          System.out.println();
        }
      }
      System.out.println(Arrays.equals(bin1, bin2));
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  public static void vaultTest() {
    System.out.println("vault test!");
    System.out.println("Loading database...");
    DatabaseManagementService managementService = new DatabaseManagementServiceBuilder(DEFAULT_DATABASE_PATH)
        .setConfig(GraphDatabaseSettings.pagecache_memory, ByteUnit.mebiBytes(512))
        .setConfig(GraphDatabaseSettings.transaction_timeout, Duration.ofSeconds(60))
        .setConfig(GraphDatabaseSettings.preallocate_logical_logs, true).build();
    System.out.println("Database loaded!");
    GraphDatabaseService graphDb = managementService.database( DEFAULT_DATABASE_NAME );
    
    GraphRepository repo = new GraphRepository(graphDb);
    
    try {
      repo.registerEntity("InChI"); 
    } catch (Exception e) {
      System.out.println("exists!");
    }
    try {
      repo.registerEntity("SMILES"); 
    } catch (Exception e) {
      System.out.println("exists!");
    }
    //4:a4d4dc85-b194-4676-99ad-043b41065382:7
    //4:a4d4dc85-b194-4676-99ad-043b41065382:8
    Neo4jNodeEntity nodeToAdd = new Neo4jNodeEntity("C".repeat(4000) + "O", "SMILES");
    Neo4jNodeEntity addedNode = repo.addNode(nodeToAdd);
    System.out.println(addedNode.getElementId());
    System.out.println(addedNode.getEntry());
    System.out.println(addedNode.getType());
    //repo.addNode("omg1", "InChI", null);
    //repo.addNode("O", "SMILES", null);
    //repo.addNode("O", "SMILES", null);
    
    Object res = repo.getUniqueConstraint();
    System.out.println(res);
    
    managementService.shutdown();
  }
  
  public static void main(String[] args) {
    vaultTest();
    System.exit(0);
    String sequence = "MSEFPTTARVVIIGGGAVGASCLYHLAKMGWSDCVLLEKNELTAGSTWHAAGNVPTFSTSWSIMNMQRYSTELYRGLGEAVDYPMNYHV"
                    + "TGSIRLAHSKERMQEFERAAGMGRYQGMPIEILNPTETQERYPFLETHDLAGSLYDPHDGDIDPAQLTQ";
    
    Protein protein1 = new Protein(sequence + "*");
    Protein protein2 = new Protein(sequence);
    System.out.println(protein1);
    System.out.println(protein2);
    
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("192.168.1.22");
    factory.setPort(5672);
    factory.setUsername("admin");
    factory.setPassword("123456");
    try (Connection con = factory.newConnection(); 
         Channel channel = con.createChannel()) {
      channel.queueDeclare("rast", true, false, false, null);
      {
        String proteinHash = "1xx";
        channel.basicPublish("", "rast", null, proteinHash.getBytes());
      }
      {
        String proteinHash = "2tx";
        channel.basicPublish("", "rast", null, proteinHash.getBytes());
      }
      {
        String proteinHash = "3hx";
        channel.basicPublish("", "rast", null, proteinHash.getBytes());
      }
    } catch (TimeoutException timeoutException) {
      timeoutException.printStackTrace();
    } catch (IOException ioException) {
      ioException.printStackTrace();
    }
  }
}
