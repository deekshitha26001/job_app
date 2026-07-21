package backend.demo.discovery.service;

import org.springframework.stereotype.Service;
import java.util.Arrays;

@Service
public class CareerPageFinder {

    private final PageContentFetcher pageContentFetcher;

    public CareerPageFinder(PageContentFetcher pageContentFetcher) {
        this.pageContentFetcher = pageContentFetcher;
    }

    public String findCareerPage(String companyName, String officialWebsite) {
        if (officialWebsite == null) return null;
        String base = officialWebsite.endsWith("/") ? officialWebsite.substring(0, officialWebsite.length() - 1) : officialWebsite;
        for (String path : Arrays.asList("/careers", "/jobs", "/work-with-us", "/join-us", "/company/careers")) {
            String candidate = base + path;
            String page = pageContentFetcher.fetch(candidate);
            if (!page.isBlank() && (page.toLowerCase().contains("career") || page.toLowerCase().contains("job"))) return candidate;
        }

        // Do not issue an additional SERP request for every candidate. The path probe above
        // covers the standard locations and keeps a discovery run inside its search budget.
        return null;
    }
}
