package backend.demo.discovery.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Typed configuration for the Company Discovery module.
 * Values are loaded from application.properties under the "company.discovery" prefix.
 *
 * To override, set any of these in application.properties:
 *   company.discovery.country=India
 *   company.discovery.industry=Technology
 *   company.discovery.max-results=15
 *   company.discovery.max-candidate-resolutions=20
 *   company.discovery.max-seed-companies=5
 */
@Component
@ConfigurationProperties(prefix = "company.discovery")
public class DiscoveryProperties {

    /** Legacy request input; discovery is now seeded from the Company Registry. */
    private List<String> keywords = List.of();
    private String country = "India";
    private String industry = "Technology";
    /** Maximum new companies emitted by one discovery run. */
    private int maxResults = 15;
    /** Hard cap on company-name-to-official-site SERP lookups in one run. */
    private int maxCandidateResolutions = 20;
    /** Maximum active registry companies used as similarity/competitor seeds per run. */
    private int maxSeedCompanies = 5;

    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }

    public int getMaxResults() { return maxResults; }
    public void setMaxResults(int maxResults) { this.maxResults = maxResults; }
    public int getMaxCandidateResolutions() { return maxCandidateResolutions; }
    public void setMaxCandidateResolutions(int maxCandidateResolutions) { this.maxCandidateResolutions = maxCandidateResolutions; }
    public int getMaxSeedCompanies() { return maxSeedCompanies; }
    public void setMaxSeedCompanies(int maxSeedCompanies) { this.maxSeedCompanies = maxSeedCompanies; }
}
