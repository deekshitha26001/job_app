package backend.demo.discovery.service;

import backend.demo.discovery.model.DiscoveryResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CompanyDiscoveryServiceTest {

    private CompanyDiscoveryService service;
    private SearchProvider mockSearchProvider;
    private WebsiteResolver websiteResolver;
    private CareerPageFinder careerPageFinder;
    private CompanyValidator companyValidator;

    @BeforeEach
    void setUp() {
        mockSearchProvider = query -> {
            if (query.contains("careers")) {
                return Arrays.asList("https://www.phonepe.com/careers");
            }
            return Arrays.asList("https://www.phonepe.com", "https://linkedin.com/company/phonepe");
        };
        
        websiteResolver = new WebsiteResolver();
        careerPageFinder = new CareerPageFinder(mockSearchProvider);
        companyValidator = new CompanyValidator();
        service = new CompanyDiscoveryService(mockSearchProvider, websiteResolver, careerPageFinder, companyValidator);
    }

    @Test
    void testDiscoverCompanies() {
        List<DiscoveryResult> results = service.discoverCompanies("India", "Technology", Arrays.asList("PhonePe"));
        
        assertNotNull(results);
        assertEquals(1, results.size());
        DiscoveryResult result = results.get(0);
        assertEquals("Phonepe", result.getCompanyName());
        assertEquals("https://www.phonepe.com", result.getOfficialWebsite());
        assertEquals("https://www.phonepe.com/careers", result.getCareerPage());
        assertEquals("India", result.getCountry());
        assertEquals("Technology", result.getIndustry());
    }
}
