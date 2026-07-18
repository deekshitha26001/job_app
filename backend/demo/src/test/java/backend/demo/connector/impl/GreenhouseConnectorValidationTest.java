package backend.demo.connector.impl;

import backend.demo.entity.AtsProvider;
import backend.demo.entity.Company;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GreenhouseConnectorValidationTest {

    @Test
    void testExtractBoardToken() {
        GreenhouseConnector connector = new GreenhouseConnector();
        Company c = new Company();
        c.setName("PhonePe");
        c.setAtsProvider(AtsProvider.GREENHOUSE);
        
        // 1. Standard board URL
        c.setCareerPageUrl("https://boards.greenhouse.io/phonepe");
        assertDoesNotThrow(() -> {
            try {
                // If it fails to extract or connect, it will throw an exception
                connector.fetchJobs(c);
            } catch (Exception e) {
                // We expect a JobCollectionException if the API call fails or board token is invalid
                // but NOT an InvalidBoardTokenException for a valid URL pattern
                assertTrue(e.getMessage().contains("Failed to fetch jobs") || e.getMessage().contains("Connection"), "Expected fetch failure, got: " + e.getMessage());
            }
        });

        // 2. Job boards URL with jobs
        c.setCareerPageUrl("https://job-boards.greenhouse.io/phonepe/jobs/123");
        assertDoesNotThrow(() -> {
            try {
                connector.fetchJobs(c);
            } catch (Exception e) {
                assertTrue(e.getMessage().contains("Failed to fetch jobs") || e.getMessage().contains("Connection"));
            }
        });
        
        // 3. Invalid URL
        c.setCareerPageUrl("https://www.someotherats.com/careers");
        Exception ex = assertThrows(Exception.class, () -> connector.fetchJobs(c));
        assertTrue(ex.getClass().getSimpleName().equals("InvalidBoardTokenException"));
    }
}
