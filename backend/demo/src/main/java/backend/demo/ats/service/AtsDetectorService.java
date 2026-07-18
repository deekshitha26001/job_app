package backend.demo.ats.service;

import backend.demo.ats.model.AtsConfidence;
import backend.demo.ats.model.AtsDetectionResult;
import backend.demo.ats.model.AtsProvider;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

@Service
public class AtsDetectorService {

    private final AtsPatternRegistry patternRegistry;
    private final AtsConfidenceCalculator confidenceCalculator;

    public AtsDetectorService(AtsPatternRegistry patternRegistry, AtsConfidenceCalculator confidenceCalculator) {
        this.patternRegistry = patternRegistry;
        this.confidenceCalculator = confidenceCalculator;
    }

    public AtsDetectionResult detect(String companyName, String officialWebsite, String careerPageUrl) {
        if (careerPageUrl == null || careerPageUrl.isEmpty()) {
            return buildResult(AtsProvider.UNKNOWN, "No Career Page", null, AtsConfidence.LOW);
        }

        // 1. Inspect Career Page URL
        AtsDetectionResult urlMatch = checkUrlPatterns(careerPageUrl, "Matched Domain");
        if (urlMatch != null) return urlMatch;

        // 2. HTTP Redirects & HTML Source
        try {
            return inspectPage(careerPageUrl);
        } catch (Exception e) {
            return buildResult(AtsProvider.UNKNOWN, "Error / Timeout", null, AtsConfidence.LOW);
        }
    }

    private AtsDetectionResult inspectPage(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setInstanceFollowRedirects(false); // Check redirect header manually
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
        
        int status = conn.getResponseCode();
        
        // Handle Redirects
        if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_SEE_OTHER) {
            String redirectUrl = conn.getHeaderField("Location");
            if (redirectUrl != null) {
                AtsDetectionResult redirectMatch = checkUrlPatterns(redirectUrl, "Matched Redirect URL");
                if (redirectMatch != null) return redirectMatch;
            }
        }
        
        // Fetch HTML
        if (status == HttpURLConnection.HTTP_OK) {
            String html = new String(conn.getInputStream().readAllBytes());
            
            // 3. iframe src values
            // 4. Embedded JavaScript URLs
            // 5. External resource URLs
            for (Map.Entry<String, AtsProvider> entry : patternRegistry.getDomainPatterns().entrySet()) {
                String pattern = entry.getKey();
                
                if (html.contains("iframe") && html.contains("src=") && html.contains(pattern)) {
                    return buildResult(entry.getValue(), "Found iframe src", pattern, confidenceCalculator.calculate("Found iframe src"));
                }
                
                if (html.contains("<script") && html.contains("src=") && html.contains(pattern)) {
                    return buildResult(entry.getValue(), "Found External Resource URL", pattern, confidenceCalculator.calculate("Found External Resource URL"));
                }
                
                if (html.contains(pattern)) {
                    return buildResult(entry.getValue(), "Found Embedded JavaScript URL", pattern, confidenceCalculator.calculate("Found Embedded JavaScript URL"));
                }
            }
        }
        
        return buildResult(AtsProvider.UNKNOWN, "No Pattern Found", null, AtsConfidence.LOW);
    }

    private AtsDetectionResult checkUrlPatterns(String url, String method) {
        for (Map.Entry<String, AtsProvider> entry : patternRegistry.getDomainPatterns().entrySet()) {
            if (url.contains(entry.getKey())) {
                return buildResult(entry.getValue(), method, entry.getKey(), confidenceCalculator.calculate(method));
            }
        }
        return null;
    }

    private AtsDetectionResult buildResult(AtsProvider provider, String method, String pattern, AtsConfidence confidence) {
        return new AtsDetectionResult(provider, method, pattern, confidence);
    }
}
