package org.modelseeed.vault.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Standalone config loader for use outside the Spring context (App.java CLI).
 * Reads the same vault-config.xml format as the Spring PropertySource.
 */
public class VaultSettings {

    private final Properties props;

    private VaultSettings(Properties props) {
        this.props = props;
    }

    public static VaultSettings load() throws Exception {
        Properties props = new Properties();
        File external = new File("vault-config.xml");
        System.out.println("vault-config.xml (exists): " + external.exists());
        System.out.println("vault-config.xml (isFile): " + external.isFile());
        System.out.println(external.getAbsolutePath());
        if (external.isFile()) {
            System.out.println("Loading config from: " + external.getAbsolutePath());
            try (FileInputStream in = new FileInputStream(external)) {
                props.loadFromXML(in);
            }
        } else {
            try (InputStream in = VaultSettings.class.getClassLoader().getResourceAsStream("vault-config.xml")) {
                if (in == null) throw new IllegalStateException("vault-config.xml not found in working directory or classpath");
                System.out.println("Loading config from classpath: vault-config.xml");
                props.loadFromXML(in);
            }
        }
        return new VaultSettings(props);
    }

    public String  getNeo4jPath()                  { return props.getProperty("vault.neo4j.path"); }
    public long    getNeo4jPagecacheMemoryMb()      { return Long.parseLong(props.getProperty("vault.neo4j.pagecache-memory-mb", "512")); }
    public long    getNeo4jTransactionTimeoutSecs() { return Long.parseLong(props.getProperty("vault.neo4j.transaction-timeout-seconds", "60")); }
    public boolean getNeo4jPreallocateLogicalLogs() { return Boolean.parseBoolean(props.getProperty("vault.neo4j.preallocate-logical-logs", "true")); }
    public String getMongoUri()         { return props.getProperty("vault.mongodb.uri"); }
    public String getMongoDatabase()    { return props.getProperty("vault.mongodb.database"); }
    public String getProteinCollection(){ return props.getProperty("vault.mongodb.protein-collection"); }
    public String getRabbitHost()       { return props.getProperty("vault.rabbitmq.host"); }
    public int    getRabbitPort()       { return Integer.parseInt(props.getProperty("vault.rabbitmq.port", "5672")); }
}
