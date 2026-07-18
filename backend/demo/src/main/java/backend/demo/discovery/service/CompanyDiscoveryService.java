package backend.demo.discovery.service;

import backend.demo.discovery.model.DiscoveryResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CompanyDiscoveryService {

    private final SearchProvider searchProvider;
    private final WebsiteResolver websiteResolver;
    private final CareerPageFinder careerPageFinder;
    private final CompanyValidator companyValidator;

    public CompanyDiscoveryService(SearchProvider searchProvider,
                                   WebsiteResolver websiteResolver,
                                   CareerPageFinder careerPageFinder,
                                   CompanyValidator companyValidator) {
        this.searchProvider = searchProvider;
        this.websiteResolver = websiteResolver;
        this.careerPageFinder = careerPageFinder;
        this.companyValidator = companyValidator;
    }

    public List<DiscoveryResult> discoverCompanies(String country, String industry, List<String> keywords) {
        List<DiscoveryResult> discovered = new ArrayList<>();
        Set<String> seenDomains = new HashSet<>();
        
        for (String keyword : keywords) {
            String query = keyword + " companies in " + country + " " + industry;
            List<String> rawSearchResults = searchProvider.search(query);
            
            for (String link : rawSearchResults) {
                // Wrap in a list since WebsiteResolver expects a list of search results
                List<String> singleResultList = List.of(link);
                String officialWebsite = websiteResolver.resolveOfficialWebsite(singleResultList);
                
                if (officialWebsite != null) {
                    String domain = extractDomain(officialWebsite);
                    if (seenDomains.contains(domain)) {
                        continue;
                    }
                    seenDomains.add(domain);

                    String companyName = extractCompanyNameFromUrl(officialWebsite);
                    String careerPage = careerPageFinder.findCareerPage(companyName, officialWebsite);
                    
                    DiscoveryResult result = new DiscoveryResult();
                    result.setCompanyName(companyName);
                    result.setOfficialWebsite(officialWebsite);
                    result.setCareerPage(careerPage);
                    result.setCountry(country);
                    result.setIndustry(industry);
                    
                    if (companyValidator.isValid(result)) {
                        discovered.add(result);
                    }
                }
            }
        }
        
        return discovered;
    }

    private String extractDomain(String url) {
        try {
            return url.replaceFirst("^(https?://)?(www\\.)?", "").split("/")[0];
        } catch (Exception e) {
            return url;
        }
    }

    private String extractCompanyNameFromUrl(String url) {
        try {
            String domain = extractDomain(url);
            String[] parts = domain.split("\\.");
            if (parts.length > 0) {
                String name = parts[0];
                return name.substring(0, 1).toUpperCase() + name.substring(1);
            }
        } catch (Exception e) {
            // ignore
        }
        return "Unknown Company";
    }
}
