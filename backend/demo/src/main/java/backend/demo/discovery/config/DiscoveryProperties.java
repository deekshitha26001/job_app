package backend.demo.discovery.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Typed configuration for the Company Discovery module.
 * Values are loaded from application.properties under the "company.discovery" prefix.
 *
 * To override, set any of these in application.properties:
 *   company.discovery.keywords=Software,IT,SaaS,AI,Cloud
 *   company.discovery.country=India
 *   company.discovery.industry=Technology
 *   company.discovery.max-results=100
 */
@Component
@ConfigurationProperties(prefix = "company.discovery")
public class DiscoveryProperties {

    private List<String> keywords = List.of("Software", "IT", "SaaS", "AI", "Cloud", "FinTech");
    private String country = "India";
    private String industry = "Technology";
    private int maxResults = 100;

    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }

    public int getMaxResults() { return maxResults; }
    public void setMaxResults(int maxResults) { this.maxResults = maxResults; }
}
