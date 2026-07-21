package backend.demo.discovery.controller;

import backend.demo.discovery.model.DiscoveryResult;
import backend.demo.discovery.model.DiscoverySummary;
import backend.demo.discovery.service.CompanyDiscoveryService;
import backend.demo.discovery.service.DiscoveryOrchestrator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/discovery")
public class CompanyDiscoveryController {

    private final CompanyDiscoveryService discoveryService;
    private final DiscoveryOrchestrator orchestrator;

    public CompanyDiscoveryController(CompanyDiscoveryService discoveryService,
                                      DiscoveryOrchestrator orchestrator) {
        this.discoveryService = discoveryService;
        this.orchestrator = orchestrator;
    }

    /** Raw AI-assisted registry expansion without review-queue submission. */
    @GetMapping("/companies")
    public ResponseEntity<List<DiscoveryResult>> discover(
            @RequestParam String country,
            @RequestParam String industry,
            @RequestParam List<String> keywords) {
        List<DiscoveryResult> results = discoveryService.discoverCompanies(country, industry, keywords);
        return ResponseEntity.ok(results);
    }

    /**
     * Sprint 2.6 – Run the full discovery pipeline.
     * Discovers companies → detects ATS → submits to Review Queue.
     * Returns a summary of what happened.
     *
     * When called without params, uses values from application.properties:
     *   company.discovery.country, company.discovery.industry, company.discovery.keywords
     *
     * Params can be overridden from the UI for custom runs.
     */
    @PostMapping("/run")
    public ResponseEntity<DiscoverySummary> run(
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) List<String> keywords) {

        DiscoverySummary summary;
        if (country == null && industry == null && keywords == null) {
            // Use values from application.properties
            summary = orchestrator.runWithDefaults();
        } else {
            summary = orchestrator.run(
                country != null ? country : "India",
                industry != null ? industry : "Technology",
                keywords != null ? keywords : List.of("Software", "IT", "SaaS", "AI", "Cloud", "FinTech")
            );
        }
        return ResponseEntity.ok(summary);
    }
}
