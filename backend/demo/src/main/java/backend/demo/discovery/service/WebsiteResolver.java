package backend.demo.discovery.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.net.URI;

@Service
public class WebsiteResolver {

    private final CompanyQualityFilter qualityFilter;

    public WebsiteResolver(CompanyQualityFilter qualityFilter) {
        this.qualityFilter = qualityFilter;
    }

    /**
     * From a list of raw search result URLs, returns the first one that
     * passes the CompanyQualityFilter (i.e. is an actual company website).
     * Returns null if none qualify.
     */
    public String resolveOfficialWebsite(List<String> searchResults) {
        return searchResults.stream()
                .filter(qualityFilter::isCompanyWebsite)
                .findFirst()
                .orElse(null);
    }

    /** Resolves a named company through SERP results, never accepting blocked publishers or profiles. */
    public String resolveOfficialWebsite(String companyName, List<String> searchResults) {
        String normalizedName = CompanyDuplicateDetector.normalize(companyName);
        return searchResults.stream()
                .filter(qualityFilter::isCompanyWebsite)
                .filter(url -> CompanyDuplicateDetector.normalize(domainLabel(url)).contains(normalizedName)
                        || normalizedName.contains(CompanyDuplicateDetector.normalize(domainLabel(url))))
                .findFirst().orElse(null);
    }

    private String domainLabel(String url) {
        String domain = url.replaceFirst("^(https?://)?(www\\.)?", "").split("/")[0];
        return domain.split("\\.")[0];
    }

    /** Validates a Gemini-supplied official domain without falling back to SERP. */
    public String validateOfficialWebsite(String companyName, String website) {
        try {
            URI uri = URI.create(website);
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null || !qualityFilter.isCompanyWebsite(website)) return null;
            String company = CompanyDuplicateDetector.normalize(companyName);
            String domain = CompanyDuplicateDetector.normalize(domainLabel(website));
            return domain.contains(company) || company.contains(domain) ? "https://" + uri.getHost() : null;
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
