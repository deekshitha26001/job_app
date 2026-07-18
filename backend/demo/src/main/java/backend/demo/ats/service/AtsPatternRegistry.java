package backend.demo.ats.service;

import backend.demo.ats.model.AtsProvider;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AtsPatternRegistry {
    
    private final Map<String, AtsProvider> domainPatterns = new HashMap<>();
    
    public AtsPatternRegistry() {
        domainPatterns.put("boards.greenhouse.io", AtsProvider.GREENHOUSE);
        domainPatterns.put("jobs.lever.co", AtsProvider.LEVER);
        domainPatterns.put("myworkdayjobs.com", AtsProvider.WORKDAY);
        domainPatterns.put("jobs.ashbyhq.com", AtsProvider.ASHBY);
        domainPatterns.put("careers.smartrecruiters.com", AtsProvider.SMARTRECRUITERS);
    }
    
    public Map<String, AtsProvider> getDomainPatterns() {
        return domainPatterns;
    }
}
