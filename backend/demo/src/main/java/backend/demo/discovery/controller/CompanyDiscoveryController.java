package backend.demo.discovery.controller;

import backend.demo.discovery.model.DiscoveryResult;
import backend.demo.discovery.service.CompanyDiscoveryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/discovery")
public class CompanyDiscoveryController {

    private final CompanyDiscoveryService discoveryService;

    public CompanyDiscoveryController(CompanyDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    @GetMapping("/companies")
    public ResponseEntity<List<DiscoveryResult>> discover(
            @RequestParam String country,
            @RequestParam String industry,
            @RequestParam List<String> keywords) {
        
        List<DiscoveryResult> results = discoveryService.discoverCompanies(country, industry, keywords);
        return ResponseEntity.ok(results);
    }
}
