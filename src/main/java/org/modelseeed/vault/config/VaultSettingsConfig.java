package org.modelseeed.vault.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:vault-config.xml", factory = XmlPropertySourceFactory.class)
public class VaultSettingsConfig {
}
