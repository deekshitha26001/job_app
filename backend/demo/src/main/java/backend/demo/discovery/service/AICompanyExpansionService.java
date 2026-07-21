package backend.demo.discovery.service;

import backend.demo.discovery.config.DiscoveryProperties;
import backend.demo.discovery.model.AiCompanyCandidate;
import backend.demo.discovery.model.DiscoveryResult;
import backend.demo.entity.Company;
import backend.demo.repository.CompanyRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Expands the registered-company ecosystem through Gemini, never generic web queries. */
@Service
public class AICompanyExpansionService {
    private final CompanyRepository companyRepository;
    private final GeminiClient geminiClient;
    private final CompanyDuplicateDetector duplicateDetector;
    private final WebsiteResolver websiteResolver;
    private final CareerPageFinder careerPageFinder;
    private final CompanyValidator companyValidator;
    private final DiscoveryProperties properties;

    public AICompanyExpansionService(CompanyRepository companyRepository, GeminiClient geminiClient,
                                     CompanyDuplicateDetector duplicateDetector, WebsiteResolver websiteResolver,
                                     CareerPageFinder careerPageFinder, CompanyValidator companyValidator,
                                     DiscoveryProperties properties) {
        this.companyRepository = companyRepository;
        this.geminiClient = geminiClient;
        this.duplicateDetector = duplicateDetector;
        this.websiteResolver = websiteResolver;
        this.careerPageFinder = careerPageFinder;
        this.companyValidator = companyValidator;
        this.properties = properties;
    }

    public List<DiscoveryResult> expand(String country, String industry) {
        List<DiscoveryResult> results = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (Company seed : companyRepository.findByActiveTrue().stream().limit(properties.getMaxSeedCompanies()).toList()) {
            if (results.size() >= properties.getMaxResults()) break;
            for (AiCompanyCandidate candidate : geminiClient.expandCompanies(prompt(seed, industry, country))) {
                if (results.size() >= properties.getMaxResults()) break;
                String normalized = CompanyDuplicateDetector.normalize(candidate.name());
                if (!seen.add(normalized) || duplicateDetector.alreadyKnown(candidate.name())) continue;
                String officialWebsite = websiteResolver.validateOfficialWebsite(candidate.name(), candidate.officialWebsite());
                if (officialWebsite == null) continue;
                DiscoveryResult result = new DiscoveryResult();
                result.setCompanyName(candidate.name());
                result.setIndustry(industry);
                result.setCountry(country);
                result.setOfficialWebsite(officialWebsite);
                result.setCareerPage(careerPageFinder.findCareerPage(candidate.name(), officialWebsite));
                result.setDiscoverySource("AI Expansion");
                result.setDiscoveryReason(candidate.reason());
                if (companyValidator.isValid(result)) results.add(result);
            }
        }
        return results;
    }

    private String prompt(Company seed, String industry, String country) {
        return """
                You expand a company registry. Return ONLY valid JSON, no markdown or prose.
                Find up to 10 real companies that compete with, are similar to, or solve similar problems as the seed.
                Exclude the seed itself. Do not include directories, publishers, job boards, articles, or social profiles.
                Every officialWebsite must be the company's canonical HTTPS domain.
                JSON schema: {"companies":[{"name":"string","reason":"string","officialWebsite":"https://example.com"}]}
                Seed company: %s
                Industry: %s
                Country/market: %s
                """.formatted(seed.getName(), industry, country);
    }
}
