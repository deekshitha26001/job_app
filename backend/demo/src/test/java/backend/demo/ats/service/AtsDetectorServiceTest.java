package backend.demo.ats.service;

import backend.demo.ats.model.AtsConfidence;
import backend.demo.ats.model.AtsDetectionResult;
import backend.demo.ats.model.AtsProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AtsDetectorServiceTest {

    private AtsDetectorService atsDetectorService;

    @BeforeEach
    void setUp() {
        AtsPatternRegistry registry = new AtsPatternRegistry();
        AtsConfidenceCalculator calculator = new AtsConfidenceCalculator();
        atsDetectorService = new AtsDetectorService(registry, calculator);
    }

    @Test
    void testDetectByDomainMatch() {
        AtsDetectionResult result = atsDetectorService.detect("TestCompany", "https://test.com", "https://boards.greenhouse.io/testcompany");
        
        assertNotNull(result);
        assertEquals(AtsProvider.GREENHOUSE, result.getDetectedAts());
        assertEquals("Matched Domain", result.getDetectionMethod());
        assertEquals(AtsConfidence.HIGH, result.getConfidence());
    }

    @Test
    void testDetectByDomainMatchLever() {
        AtsDetectionResult result = atsDetectorService.detect("AnotherCompany", "https://another.com", "https://jobs.lever.co/another");
        
        assertNotNull(result);
        assertEquals(AtsProvider.LEVER, result.getDetectedAts());
        assertEquals("Matched Domain", result.getDetectionMethod());
        assertEquals(AtsConfidence.HIGH, result.getConfidence());
    }

    @Test
    void testDetectUnknown() {
        // Mock a URL that isn't a known ATS domain and let it fail to connect (returns UNKNOWN gracefully)
        AtsDetectionResult result = atsDetectorService.detect("UnknownCompany", "https://unknown.com", "https://unknown.com/careers");
        
        assertNotNull(result);
        assertEquals(AtsProvider.UNKNOWN, result.getDetectedAts());
        assertEquals(AtsConfidence.LOW, result.getConfidence());
    }
}
