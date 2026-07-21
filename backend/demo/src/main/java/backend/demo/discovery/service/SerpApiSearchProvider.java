package backend.demo.discovery.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SerpApiSearchProvider implements SearchProvider {

    /**
     * SERP API key — must be set in application-secrets.properties as:
     *   serpapi.key=YOUR_KEY
     *
     * The file is excluded from Git. Key is NEVER logged.
     */
    @Value("${serpapi.key:}")
    private String apiKey;

    @Value("${company.discovery.results-per-query:5}")
    private int resultsPerQuery;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public List<String> search(String query) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                "Company Discovery is misconfigured: 'serpapi.key' is missing. " +
                "Add it to application-secrets.properties (excluded from Git)."
            );
        }

        String url = UriComponentsBuilder.fromUriString("https://serpapi.com/search.json")
                .queryParam("q", query)
                .queryParam("api_key", apiKey)
                .queryParam("engine", "google")
                .queryParam("num", resultsPerQuery)
                .toUriString();

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            List<String> results = new ArrayList<>();

            if (response != null && response.containsKey("organic_results")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> organicResults =
                        (List<Map<String, Object>>) response.get("organic_results");
                for (Map<String, Object> result : organicResults) {
                    if (result.containsKey("link")) {
                        results.add((String) result.get("link"));
                    }
                }
            }
            return results;
        } catch (IllegalStateException e) {
            // Re-throw config errors without swallowing them
            throw e;
        } catch (Exception e) {
            // Network / parsing errors — log the message only, never the key
            System.err.println("[SerpApiSearchProvider] Search failed for query '" + query + "': " + e.getMessage());
            throw new IllegalStateException("SERP API search failed. Check the server log and SERP API configuration.", e);
        }
    }
}
