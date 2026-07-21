package backend.demo.discovery.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.PropertySource;

@Component
@PropertySource(value = "classpath:application-secrets.properties", ignoreResourceNotFound = true)
@ConfigurationProperties(prefix = "gemini")
public class GeminiProperties {
    private String apiKey;
    private String model = "gemini-2.5-flash";
    private int maxRetries = 3;

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
}
