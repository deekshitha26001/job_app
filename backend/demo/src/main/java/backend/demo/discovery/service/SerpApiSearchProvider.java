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

    @Value("${serpapi.key:}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public List<String> search(String query) {
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("SERP API Key is missing! Please configure serpapi.key in application properties.");
            return new ArrayList<>();
        }
        
        String url = UriComponentsBuilder.fromUriString("https://serpapi.com/search.json")
                .queryParam("q", query)
                .queryParam("api_key", apiKey)
                .queryParam("engine", "google")
                .toUriString();

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            List<String> results = new ArrayList<>();
            
            if (response != null && response.containsKey("organic_results")) {
                List<Map<String, Object>> organicResults = (List<Map<String, Object>>) response.get("organic_results");
                for (Map<String, Object> result : organicResults) {
                    if (result.containsKey("link")) {
                        results.add((String) result.get("link"));
                    }
                }
            }
            return results;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
