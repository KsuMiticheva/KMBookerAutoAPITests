package core.clients;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class APIClient {
    private final String baseUrl;

    public APIClient() {
        this.baseUrl = determineBaseUrl();
    }

    private String determineBaseUrl() {
        String environment = System.getProperty("env", "test");
        String configFileName = "application-" + environment + ".properties";

        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(configFileName)) {
            if (input == null) {
                throw new IllegalStateException("Config file not found: " + configFileName);
            }
            properties.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("Could not load config file: " + configFileName, e);
        }
        return properties.getProperty("baseUrl");
    }
}
