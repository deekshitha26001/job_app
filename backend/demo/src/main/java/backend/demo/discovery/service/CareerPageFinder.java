package backend.demo.discovery.service;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CareerPageFinder {

    private final SearchProvider searchProvider;

    public CareerPageFinder(SearchProvider searchProvider) {
        this.searchProvider = searchProvider;
    }

    public String findCareerPage(String companyName, String officialWebsite) {
        List<String> results = searchProvider.search(companyName + " careers");
        
        for (String link : results) {
            String lowerLink = link.toLowerCase();
            if (lowerLink.contains("careers") || 
                lowerLink.contains("jobs") || 
                (officialWebsite != null && link.startsWith(officialWebsite))) {
                return link;
            }
        }
        
        if (officialWebsite != null) {
            String base = officialWebsite.endsWith("/") ? officialWebsite : officialWebsite + "/";
            return base + "careers";
        }
        
        return null;
    }
}
