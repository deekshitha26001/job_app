package backend.demo.discovery.service;

import backend.demo.discovery.model.DiscoveryResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CompanyDiscoveryServiceTest {

    private CompanyDiscoveryService service;
    private AICompanyExpansionService expansionService;

    @BeforeEach
    void setUp() {
        expansionService = mock(AICompanyExpansionService.class);
        service = new CompanyDiscoveryService(expansionService);
    }

    @Test
    void testDiscoverCompanies() {
        DiscoveryResult expected = new DiscoveryResult();
        expected.setCompanyName("Paytm");
        expected.setOfficialWebsite("https://paytm.com");
        expected.setCareerPage("https://paytm.com/careers");
        expected.setCountry("India");
        expected.setIndustry("Technology");
        when(expansionService.expand("India", "Technology")).thenReturn(List.of(expected));
        List<DiscoveryResult> results = service.discoverCompanies("India", "Technology", List.of());
        
        assertNotNull(results);
        assertEquals(1, results.size());
        DiscoveryResult result = results.get(0);
        assertEquals("Paytm", result.getCompanyName());
        assertEquals("https://paytm.com", result.getOfficialWebsite());
        assertEquals("https://paytm.com/careers", result.getCareerPage());
        assertEquals("India", result.getCountry());
        assertEquals("Technology", result.getIndustry());
    }
}
