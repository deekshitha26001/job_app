package backend.demo.discovery.service;

import backend.demo.discovery.model.DiscoveryResult;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyDiscoveryService {

    private final AICompanyExpansionService expansionService;

    public CompanyDiscoveryService(AICompanyExpansionService expansionService) {
        this.expansionService = expansionService;
    }

    public List<DiscoveryResult> discoverCompanies(String country, String industry, List<String> keywords) {
        return expansionService.expand(country, industry);
    }
}
