package backend.demo.discovery.service;

import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Sprint 2.7 – Company Quality Filter.
 *
 * Single responsibility: decide whether a URL points to an actual company
 * website or to an article / directory / aggregator that should be rejected.
 *
 * To add a new blocked domain, add its root domain to BLOCKED_DOMAINS below.
 * No other code needs to change.
 */
@Component
public class CompanyQualityFilter {

    /**
     * Root domains that are NOT company websites.
     * Checked as a substring of the lower-cased URL, so
     * "forbes.com" will block https://www.forbes.com/lists/... etc.
     */
    private static final Set<String> BLOCKED_DOMAINS = Set.of(
            // News & media
            "forbes.com",
            "businessinsider.com",
            "techcrunch.com",
            "wired.com",
            "theguardian.com",
            "bbc.com",
            "bloomberg.com",
            "reuters.com",
            "ft.com",
            "economictimes.indiatimes.com",
            "livemint.com",
            "yourstory.com",
            "inc42.com",

            // Encyclopedias / wikis
            "wikipedia.org",
            "en.m.wikipedia.org",

            // Professional networks & job boards
            "linkedin.com",
            "glassdoor.com",
            "indeed.com",
            "naukri.com",
            "internshala.com",
            "shine.com",
            "monster.com",
            "simplyhired.com",
            "ziprecruiter.com",
            "wellfound.com",     // formerly AngelList jobs
            "instahyre.com",

            // Aggregators / directories / funding info
            "crunchbase.com",
            "tracxn.com",
            "zaubacorp.com",
            "startupindia.gov.in",
            "ambitionbox.com",
            "comparably.com",
            "owler.com",
            "screener.in",
            "justdial.com",
            "clutch.co",
            "goodfirms.co",
            "ycombinator.com",

            // Blog / publishing platforms
            "medium.com",
            "substack.com",
            "hashnode.dev",
            "dev.to",
            "wordpress.com",
            "blogger.com",

            // List / ranking articles
            "builtin.com",
            "g2.com",
            "capterra.com",
            "softwareadvice.com",
            "gartner.com",

            // Social media
            "twitter.com",
            "x.com",
            "facebook.com",
            "instagram.com",
            "youtube.com",

            // Review / Q&A
            "quora.com",
            "reddit.com",
            "stackoverflow.com",
            "google.com",
            "bing.com",
            "duckduckgo.com"
    );

    /**
     * Returns true if the URL appears to be an actual company website
     * (i.e. it is NOT in the blocked-domain list).
     */
    public boolean isCompanyWebsite(String url) {
        if (url == null || url.isBlank()) return false;
        String lower = url.toLowerCase();
        return BLOCKED_DOMAINS.stream().noneMatch(lower::contains);
    }

    /**
     * Convenience inverse — returns true when the URL should be rejected.
     */
    public boolean isNonCompanyUrl(String url) {
        return !isCompanyWebsite(url);
    }
}
