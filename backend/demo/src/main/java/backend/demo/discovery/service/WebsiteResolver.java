package backend.demo.discovery.service;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class WebsiteResolver {

    public String resolveOfficialWebsite(List<String> searchResults) {
        for (String link : searchResults) {
            String lowerLink = link.toLowerCase();
            // Filter out common job boards, directories, and social media
            if (!lowerLink.contains("linkedin.com") && 
                !lowerLink.contains("glassdoor.com") && 
                !lowerLink.contains("crunchbase.com") &&
                !lowerLink.contains("bloomberg.com") &&
                !lowerLink.contains("wikipedia.org") &&
                !lowerLink.contains("indeed.com") &&
                !lowerLink.contains("naukri.com")) {
                return link;
            }
        }
        return null;
    }
}
