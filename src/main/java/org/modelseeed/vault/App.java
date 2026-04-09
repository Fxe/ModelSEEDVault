package org.modelseeed.vault;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.types.Binary;
import org.modelseeed.rast.RASTClient;
import org.modelseeed.rast.RPCClient;
import org.modelseeed.vault.biodb.OntologyRelationship;
import org.modelseeed.vault.biodb.biochem.GenericReaction;
import org.modelseeed.vault.biodb.biochem.GenericReactionFactory;
import org.modelseeed.vault.biodb.biochem.OntologyBiochemCompound;
import org.modelseeed.vault.biodb.biochem.OntologyBiochemReaction;
import org.modelseeed.vault.biodb.biochem.ReactionMapper;
import org.modelseeed.vault.biodb.biochem.ReactionMatcher;
import org.modelseeed.vault.biodb.biochem.ReactionMatcherResult;
import org.modelseeed.vault.biodb.biochem.ReactionMapper.MatchResult;
import org.modelseeed.vault.config.VaultSettings;
import org.modelseeed.vault.core.Compress;
import org.modelseeed.vault.core.Neo4jNodeEntity;
import org.modelseeed.vault.core.ProteinSequence;
import org.modelseeed.vault.core.cobra.Metabolite;
import org.modelseeed.vault.core.cobra.Model;
import org.modelseeed.vault.core.cobra.Reaction;
import org.modelseeed.vault.neo4j.Export;
import org.modelseeed.vault.neo4j.Export.ExportGraph;
import org.modelseeed.vault.neo4j.cobra.LabelCOBRA;
import org.modelseeed.vault.neo4j.cobra.Neo4jMetabolite;
import org.modelseeed.vault.neo4j.cobra.Neo4jReaction;
import org.modelseeed.vault.neo4j.cobra.RelationshipCOBRA;
import org.modelseeed.vault.repository.CobraModelRepository;
import org.modelseeed.vault.repository.GraphRepository;
import org.modelseeed.vault.repository.ProteinRepositoryNeo4j;
import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.io.ByteUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.Hashing;
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

  private static final String DEFAULT_DATABASE_NAME = "neo4j";

  private static VaultSettings cachedSettings;

  private static VaultSettings settings() {
    if (cachedSettings == null) {
      try {
        cachedSettings = VaultSettings.load();
      } catch (Exception e) {
        throw new RuntimeException("Failed to load vault-config.xml", e);
      }
    }
    return cachedSettings;
  }

  public static DatabaseManagementService getDatabaseManagementService() {
    System.out.println("vault test!");
    System.out.println("Loading database...");
    DatabaseManagementService managementService = new DatabaseManagementServiceBuilder(Paths.get(settings().getNeo4jPath()))
        .setConfig(GraphDatabaseSettings.pagecache_memory, ByteUnit.mebiBytes(settings().getNeo4jPagecacheMemoryMb()))
        .setConfig(GraphDatabaseSettings.transaction_timeout, Duration.ofSeconds(settings().getNeo4jTransactionTimeoutSecs()))
        .setConfig(GraphDatabaseSettings.preallocate_logical_logs, settings().getNeo4jPreallocateLogicalLogs()).build();
    System.out.println("Database loaded!");
    return managementService;
  }
  
  public static void exportRelationships() {
    System.out.println("Loading database...");
    DatabaseManagementService managementService = getDatabaseManagementService();
    GraphDatabaseService graphDb = managementService.database(DEFAULT_DATABASE_NAME);
    
    try (Transaction tx = graphDb.beginTx()) {
      ExportGraph g = Export.exportRelationships(tx, Set.of(
          OntologyRelationship.has_annotation_event));
      Export.writeToFile(g, new File("M:/vault/annotation_v1.json"));
    }
    
    managementService.shutdown();
  }

  

  public static void neo4jTest() {
    System.out.println("Loading database...");
    DatabaseManagementService managementService = getDatabaseManagementService();
    System.out.println("Database loaded!");
    GraphDatabaseService graphDb = managementService.database(DEFAULT_DATABASE_NAME);

    Transaction tx = graphDb.beginTx();
    Node node = tx.findNode(LabelCOBRA.SBMLSpecies, "key", "iAbaylyiv4:M_ANTHRANILATE_Cytosol");
    //Node node = tx.getNodeByElementId("4:565e9de0-64a0-4532-a9a2-ec5de2c8db36:1375");
    List<String> l = new ArrayList<>();
    l.add("a");
    l.add("b");
    node.setProperty("list", l);
    System.out.println(node.getAllProperties());

    // tx.createNode(null)
    tx.rollback();

    System.out.println("Shutdown database...");
    managementService.shutdown();
    System.out.println("Done!");
  }

  public static void mongoTest() {
    MongoClient client = MongoClients.create(settings().getMongoUri());
    MongoDatabase sequenceDatabase = client.getDatabase(settings().getMongoDatabase());
    MongoCollection<Document> seqProteins = sequenceDatabase.getCollection("seq_protein");
    Document docProtein = seqProteins
        .find(new Document("_id", "146f0fe779bddd1b11b83af6aa8db7c3b082f10c335a1e0cda0e151ede72b499")).first();

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
    DatabaseManagementService managementService = getDatabaseManagementService();
    System.out.println("Database loaded!");
    GraphDatabaseService graphDb = managementService.database(DEFAULT_DATABASE_NAME);

    GraphRepository repo = new GraphRepository(graphDb);
    Transaction tx = graphDb.beginTx();

    try {
      repo.registerEntity("InChI", tx);
    } catch (Exception e) {
      System.out.println("exists!");
    }
    try {
      repo.registerEntity("SMILES", tx);
    } catch (Exception e) {
      System.out.println("exists!");
    }
    // 4:a4d4dc85-b194-4676-99ad-043b41065382:7
    // 4:a4d4dc85-b194-4676-99ad-043b41065382:8
    Neo4jNodeEntity nodeToAdd = new Neo4jNodeEntity("C".repeat(4000) + "O", "SMILES");
    Neo4jNodeEntity addedNode = repo.addNode(nodeToAdd, tx);
    System.out.println(addedNode.getElementId());
    System.out.println(addedNode.getEntry());
    System.out.println(addedNode.getType());
    // repo.addNode("omg1", "InChI", null);
    // repo.addNode("O", "SMILES", null);
    // repo.addNode("O", "SMILES", null);

    Object res = repo.getUniqueConstraint(tx);
    System.out.println(res);

    managementService.shutdown();
  }

  public static void vaultTestProtein() {
    System.out.println("test protein");
    System.out.println("Loading database...");
    DatabaseManagementService managementService = getDatabaseManagementService();
    System.out.println("Database loaded!");
    GraphDatabaseService graphDb = managementService.database(DEFAULT_DATABASE_NAME);

    ProteinRepositoryNeo4j repo = new ProteinRepositoryNeo4j(graphDb);

    String proteinSequence = "MQWQTKLPLIAILRGITPDEALAHVGAVIDAGFDAVEIPLNSPQWEQSIPAIVDAYGDKALIGAGTVLKPEQVDALARMGCQLIVTPNIHSEVIRRAVGYGMTVCPGCATATEAFTALEAGAQALKIFPSSAFGPQYIKALKAVLPSDIAVFAVGGVTPENLAQWIDAGCAGAGLGSDLYRAGQSVERTAQQAAAFVKAYREAVQ";
    String sha256 = Hashing.sha256().hashString(proteinSequence, StandardCharsets.UTF_8).toString();

    try (Transaction tx = graphDb.beginTx()) {
      tx.findNodes(ProteinSequence.LABEL).forEachRemaining(e -> {
        System.out.println(e.getAllProperties());
      });
      tx.commit();
    } finally {

    }

    try (Transaction tx = graphDb.beginTx()) {
      Node node = tx.findNode(ProteinSequence.LABEL, "key", sha256);
      ProteinSequence protein = new ProteinSequence(proteinSequence, node);
      System.out.println(protein.getProperties());
      tx.commit();
    } finally {

    }

    managementService.shutdown();
  }

  public static void testRast() {
    RASTClient client = new RASTClient();
    RPCClient rpc = new RPCClient("https://tutorial.theseed.org/services/genome_annotation");

    List<Object> params = new ArrayList<>();
    List<Map<String, Object>> features = new ArrayList<>();
    String sequence = "MKILINKSELNKILKKMNNVIISNNKIKPHHSYFLIEAKEKEINFYANNEYFSVKCNLNKNIDILEQGSLIVKGKIFNDL"
        + "INGIKEEIITIQEKDQTLLVKTKKTSINLNTINVNEFPRIRFNEKNDLSEFNQFKINYSLLVKGIKKIFHSVSNNREISS"
        + "KFNGVNFNGSNGKEIFLEASDTYKLSVFEIKQETEPFDFILESNLLSFINSFNPEEDKSIVFYYRKDNKDSFSTEMLISM"
        + "DNFMISYTSVNEKFPEVNYFFEFEPETKIVVQKNELKDALQRIQTLAQNERTFLCDMQINSSELKIRAIVNNIGNSLEEI"
        + "SCLKFEGYKLNISFNPSSLLDHIESFESNEINFDFQGNSKYFLITSKSEPELKQILVPSR";
    Map<String, Object> feature = Map.of("id", "example", "protein_translation", sequence);
    params.add(Map.of("features", features));
    params.add(Map.of("stages", client.getStages()));

    try {
      Object res = rpc.call("GenomeAnnotation.run_pipeline", params);
      System.out.println(res);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public static void vaultTestReadModel() {
    System.out.println("vault test!");
    DatabaseManagementService managementService = getDatabaseManagementService();
    System.out.println("Database loaded!");
    GraphDatabaseService graphDb = managementService.database(DEFAULT_DATABASE_NAME);

    // GraphRepository rep = new GraphRepository(graphDb);
    // GraphService graphService = new GraphService(rep);
    CobraModelRepository rep = new CobraModelRepository(graphDb);

    Transaction tx = graphDb.beginTx();

    String id = "iAbaylyiv4";
    String translate = "ModelSEED";
    Map<String, String> cmpMapping = new HashMap<>();
    cmpMapping.put("Cytosol", "c");
    cmpMapping.put("Extraorganism", "e");
    Neo4jNodeEntity nodeModel = rep.getNode(id, "SBMLModel", tx);
    Integer compartmentIndex = 0;

    System.out.println(nodeModel);

    Model m = rep.getCobraModel(nodeModel.getNode(), translate, cmpMapping, compartmentIndex, tx);

    Set<String> uspecies = new HashSet<>();
    Set<String> ureactions = new HashSet<>();
    for (Metabolite metabolite: m.getMetabolites()) {
      uspecies.add(metabolite.getId());
      //System.out.println(metabolite.getId());
    }
    for (Reaction reaction: m.getReactions()) {
      ureactions.add(reaction.getId());
      System.out.println(reaction.getId());
      //System.out.println(reaction.getMetabolites());
    }
    
    System.out.println(m.getMetabolites().size() + " " + uspecies.size());
    System.out.println(m.getReactions().size() + " " + ureactions.size());
    if (uspecies.size() == m.getMetabolites().size()) {
      System.out.println("all species unique");
    }
    if (ureactions.size() == m.getReactions().size()) {
      System.out.println("all reactions unique");
    }
    tx.rollback();

    System.out.println("Shutdown database...");
    managementService.shutdown();
    System.out.println("Done!");
  }


  public static Set<String> match(Neo4jReaction reaction) {
    /***
     * for each stoichiometry nodes in reaction fetchs the matching annotation |->
     * reaction_1 |-> metabolite_A -> metabolite_1 -|-> reaction_2 reaction_A -| |->
     * metabolite_B -> metabolite_2 -|-> reaction_1 |-> reaction_4 |-> reaction_3
     * 
     * reaction_1 intersects all metabolite_1 and metabolite_2 thus reaction_A =
     * reaction_1
     * 
     * to avoid fetching big lists of reactions for example metabolite_1 may have
     * 200000 reactions we do intersection of the lowest to highest degree if a
     * intersection fail return null
     */
    Map<String, Double> reactionMetabolites = reaction.getMetabolites();
    int reactionSize = reactionMetabolites.size();

    Map<String, Integer> degreeMap = new HashMap<>();
    Map<String, Node> nodeMap = new HashMap<>();
    Map<Integer, List<Node>> degreeToCompound = new HashMap<>();
    for (Entry<String, Neo4jMetabolite> e : reaction.metaboliteNodes.entrySet()) {
      Node annotatedCompound = e.getValue().annotation(Label.label("ModelSEEDCompound"));
      if (annotatedCompound != null) {
        int degree = annotatedCompound.getDegree(RelationshipType.withName("has_stoichiometry_coefficient"));
        degreeMap.put(annotatedCompound.getElementId(), degree);
        nodeMap.put(annotatedCompound.getElementId(), annotatedCompound);
        degreeToCompound.putIfAbsent(degree, new ArrayList<>());
        degreeToCompound.get(degree).add(annotatedCompound);
        // System.out.println(annotatedCompound.getAllProperties() + " " +
        // annotatedCompound.getLabels());
      } else {
        return null;
      }
    }

    if (degreeMap.isEmpty()) {
      return null;
    }

    // Sort degrees ascending so we intersect the smallest neighbor sets first
    List<Integer> sortedDegreeList = degreeMap.values().stream().sorted().distinct().collect(Collectors.toList());

    Set<String> intersection = null;

    for (int degree : sortedDegreeList) {
      for (Node annotatedCompound : degreeToCompound.get(degree)) {

        // Collect all generic reaction elementIds connected to this compound
        Set<String> reactionIds = new HashSet<>();
        for (Relationship relStoich : annotatedCompound
            .getRelationships(RelationshipType.withName("has_stoichiometry_coefficient"))) {
          Node genericReaction = relStoich.getOtherNode(annotatedCompound);
          int stoichDegree = genericReaction.getDegree(RelationshipType.withName("has_stoichiometry_coefficient"));
          if (stoichDegree == reactionSize) {
            reactionIds.add(genericReaction.getElementId());
          }
        }

        if (intersection == null) {
          // Seed with the first (smallest-degree) compound's reaction set
          intersection = reactionIds;
          // System.out.println(degree + " " + annotatedCompound.getProperty("key") + " i
          // " + intersection.size());
        } else {
          // Intersect incrementally — fail fast if already empty
          intersection.retainAll(reactionIds);
          // System.out.println(degree + " " + annotatedCompound.getProperty("key") + " i
          // " + intersection.size());
          if (intersection.isEmpty()) {
            return null;
          }
        }
      }
    }

    // System.out.println("intersection: " + intersection);
    return intersection;
    /**
     * 
     * if (intersection == null || intersection.size() != 1) { // No unique match
     * found return null; }
     * 
     * //Resolve the single matching elementId back to a Node String matchedId =
     * intersection.iterator().next(); System.out.println(matchedId);
     * 
     * /** for (Relationship relStoich : other.getRelationships(
     * RelationshipType.withName("has_stoichiometry_coefficient"))) { Node
     * genericReaction = relStoich.getOtherNode(other); r1.put((String)
     * genericReaction.getProperty("key"), genericReaction); }
     * 
     * degreeMap.entrySet() .stream() .sorted(Map.Entry.comparingByValue())
     * .forEach(e -> System.out.println(e.getValue() + " " + e.getKey()));
     * 
     * return degreeMap;
     **/
  }
  
  public static void vaultTestSBMLRectionInference() {
    DatabaseManagementService databaseManagementService = getDatabaseManagementService();
    GraphDatabaseService graphDb = databaseManagementService.database(DEFAULT_DATABASE_NAME);
    CobraModelRepository rep = new CobraModelRepository(graphDb);
    //iAbaylyiv4:R_ISOCITDEH_DASH_RXN 4:3779b88f-90cb-49d4-a69d-a7360263bf82:2364
    
    Set<String> exclude = new HashSet<>();
    exclude.add("cpd00067");
    
    try (Transaction tx = rep.beginTx()) {
      
      Node reactionNode = tx.getNodeByElementId("4:3779b88f-90cb-49d4-a69d-a7360263bf82:2364");
      String prefix = "iAbaylyiv4";
      Neo4jReaction reaction = Neo4jReaction.build(reactionNode, prefix, tx);
      ReactionMatcher matcher = new ReactionMatcher(exclude);
      ReactionMatcherResult matcherResult = matcher.match(reaction, tx);
      Map<Node, MatchResult> result = matcherResult.getMatch();
      System.out.println(result);

      
      tx.rollback();
    }
  }

  public static void vaultTestModelReactionInference() {
    DatabaseManagementService databaseManagementService = getDatabaseManagementService();
    GraphDatabaseService graphDb = databaseManagementService.database(DEFAULT_DATABASE_NAME);
    // GraphRepository rep = new GraphRepository(graphDb);
    // GraphService graphService = new GraphService(rep);
    CobraModelRepository rep = new CobraModelRepository(graphDb);

    Transaction tx = graphDb.beginTx();
    String modelId = "iAbaylyiv4";

    Neo4jNodeEntity nodeModel = rep.getNode(modelId, "SBMLModel", tx);
    List<List<Object>> modelReactions = rep.getChilds(nodeModel.getNode(),
        RelationshipType.withName("has_sbml_reaction"), tx);
    
    Set<String> exclude = new HashSet<>();
    exclude.add("cpd00067");
    ReactionMatcher matcher = new ReactionMatcher(exclude);

    for (List<Object> edgeAndNode : modelReactions) {
      // @SuppressWarnings("unchecked")
      // Map<String, Object> rel = (Map<String, Object>) edgeAndNode.get(0);
      Neo4jNodeEntity node = (Neo4jNodeEntity) edgeAndNode.get(1);
      Neo4jReaction reaction = Neo4jReaction.build(node.getNode(), modelId, tx);
      //System.out.println(reaction.stoichiometry.s.keySet());
      //skip translocation reactions
      if (!reaction.isTranslocation()) {
        ReactionMatcherResult matcherResult = matcher.match(reaction, tx);
        if (matcherResult != null) {
          Map<Node, MatchResult> result = matcherResult.getMatch();
          Map<Node, MatchResult> resultNonNull = new HashMap<>();
          result.forEach((k, v) -> { if (v != null) resultNonNull.put(k, v); });
          
          System.out.println(reaction);
          resultNonNull.forEach((k, v) -> {
            System.out.println(String.format("\t[%d]%s %s", 
                k.getProperty("is_obsolete"), k.getProperty("key"), v));
          });
          System.out.println();
        }
      }
    }

    tx.rollback();

    System.out.println("Shutdown database...");
    databaseManagementService.shutdown();
    System.out.println("Done!");
  }
  
  public static void modelseed() {
    DatabaseManagementService databaseManagementService = getDatabaseManagementService();
    GraphDatabaseService graphDb = databaseManagementService.database(DEFAULT_DATABASE_NAME);
    
    try (Transaction tx = graphDb.beginTx()) {
      ResourceIterator<Node> cursor = tx.findNodes(OntologyBiochemReaction.ModelSEEDReaction);
      while (cursor.hasNext()) {
        Node node = cursor.next();
        boolean isObsolete = Integer.valueOf(node.getProperty("is_obsolete").toString()) == 1;
        if (isObsolete) {
          if (!node.hasLabel(OntologyBiochemCompound.Disabled)) {
            node.addLabel(OntologyBiochemCompound.Disabled);
          }
        }
      }
      tx.commit();
    }
    
    System.out.println("Shutdown database...");
    databaseManagementService.shutdown();
    System.out.println("Done!");
  }

  public static void main(String[] args) {
    //neo4jTest();
    exportRelationships();
    // vaultTest();
    // vaultTestProtein();
    // testRast();
    // vaultTestReadModel();
    //vaultTestModelReactionInference();
    //vaultTestSBMLRectionInference();
    //modelseed();
    System.exit(0);
    String sequence = "MSEFPTTARVVIIGGGAVGASCLYHLAKMGWSDCVLLEKNELTAGSTWHAAGNVPTFSTSWSIMNMQRYSTELYRGLGEAVDYPMNYHV"
        + "TGSIRLAHSKERMQEFERAAGMGRYQGMPIEILNPTETQERYPFLETHDLAGSLYDPHDGDIDPAQLTQ";

    ProteinSequence protein1 = ProteinSequence.buildFromSequence(sequence + "*");
    ProteinSequence protein2 = ProteinSequence.buildFromSequence(sequence);
    System.out.println(protein1);
    System.out.println(protein2);

    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(settings().getRabbitHost());
    factory.setPort(settings().getRabbitPort());
    factory.setUsername("admin");
    factory.setPassword("123m56");
    try (Connection con = factory.newConnection(); Channel channel = con.createChannel()) {
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
