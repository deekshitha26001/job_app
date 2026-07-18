package backend.demo.discovery.service;

import backend.demo.discovery.model.DiscoveryResult;
import org.springframework.stereotype.Component;

@Component
public class CompanyValidator {

    public boolean isValid(DiscoveryResult result) {
        if (result == null) return false;
        if (result.getCompanyName() == null || result.getCompanyName().trim().isEmpty()) return false;
        if (result.getOfficialWebsite() == null || result.getOfficialWebsite().trim().isEmpty()) return false;
        if (result.getCountry() == null || result.getCountry().trim().isEmpty()) return false;
        if (result.getIndustry() == null || result.getIndustry().trim().isEmpty()) return false;
        
        if (!result.getOfficialWebsite().startsWith("http")) return false;
        
        return true;
    }
}
