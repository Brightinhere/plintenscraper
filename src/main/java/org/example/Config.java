package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new IOException("Unable to find config.properties");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Configuration file loading failed", e);
        }
    }

    public static String getUsername() {
        return properties.getProperty("bot.username");
    }

    public static String getPassword() {
        return properties.getProperty("bot.password");
    }
}
