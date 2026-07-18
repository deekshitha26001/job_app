package backend.demo.discovery.service;

import org.springframework.stereotype.Service;

import java.util.List;

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
}
