package backend.demo.discovery.service;

import backend.demo.ats.model.AtsDetectionResult;
import backend.demo.ats.service.AtsDetectorService;
import backend.demo.discovery.config.DiscoveryProperties;
import backend.demo.discovery.model.DiscoveryResult;
import backend.demo.discovery.model.DiscoverySummary;
import backend.demo.review.ReviewService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Sprint 2.6 – Discovery Orchestrator.
 *
 * Single responsibility: run the full discovery pipeline end-to-end
 * and submit results to the Review Queue.
 *
 * Flow:
 *   CompanyDiscoveryService
 *       → AtsDetectorService
 *           → ReviewService.submitForReview()
 *               → pending_companies table
 */
@Service
public class DiscoveryOrchestrator {

    private final CompanyDiscoveryService discoveryService;
    private final AtsDetectorService atsDetectorService;
    private final ReviewService reviewService;
    private final DiscoveryProperties discoveryProperties;

    public DiscoveryOrchestrator(CompanyDiscoveryService discoveryService,
                                 AtsDetectorService atsDetectorService,
                                 ReviewService reviewService,
                                 DiscoveryProperties discoveryProperties) {
        this.discoveryService = discoveryService;
        this.atsDetectorService = atsDetectorService;
        this.reviewService = reviewService;
        this.discoveryProperties = discoveryProperties;
    }

    /**
     * Run with defaults from application.properties (company.discovery.*).
     * Called by POST /api/discovery/run when no params are supplied.
     */
    public DiscoverySummary runWithDefaults() {
        return run(
            discoveryProperties.getCountry(),
            discoveryProperties.getIndustry(),
            discoveryProperties.getKeywords()
        );
    }

    /**
     * Run the full pipeline for the given country, industry, and keywords.
     * Returns a summary of what happened.
     */
    public DiscoverySummary run(String country, String industry, List<String> keywords) {
        int submitted = 0;
        int skipped = 0;
        int failed = 0;

        // Step 1: Discover companies via SERP API
        List<DiscoveryResult> discovered = discoveryService.discoverCompanies(country, industry, keywords);
        int companiesFound = discovered.size();

        // Step 2: For each discovered company, detect ATS and submit to review queue
        for (DiscoveryResult result : discovered) {
            try {
                // Step 3: ATS Detection
                AtsDetectionResult atsResult = atsDetectorService.detect(
                        result.getCompanyName(),
                        result.getOfficialWebsite(),
                        result.getCareerPage()
                );

                // Step 4: Submit to Review Queue
                reviewService.submitForReview(result, atsResult);
                submitted++;

            } catch (Exception e) {
                System.err.println("[DiscoveryOrchestrator] Failed to process company: "
                        + result.getCompanyName() + " — " + e.getMessage());
                failed++;
            }
        }

        // Skipped = companies that discovery returned but were invalid (filtered out upstream)
        // We report the gap if any; for now it's the difference between raw results and valid ones.
        skipped = Math.max(0, companiesFound - submitted - failed);

        return new DiscoverySummary(companiesFound, submitted, skipped, failed);
    }
}
