package org.modelseeed.vault.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

/**
 * Loads a Java XML Properties file (Properties.loadFromXML format).
 * Checks the working directory for vault-config.xml first so a file
 * placed next to the JAR overrides the classpath default.
 */
public class XmlPropertySourceFactory implements PropertySourceFactory {

    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        Properties props = new Properties();

        File external = new File("vault-config.xml");
        if (external.isFile()) {
            try (FileInputStream in = new FileInputStream(external)) {
                props.loadFromXML(in);
            }
            return new PropertiesPropertySource("vault-config[file]", props);
        }

        try (var in = resource.getInputStream()) {
            props.loadFromXML(in);
        }
        return new PropertiesPropertySource("vault-config[classpath]", props);
    }
}
