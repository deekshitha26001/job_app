package backend.demo.discovery.service;

import backend.demo.discovery.config.GeminiProperties;
import backend.demo.discovery.model.AiCompanyCandidate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Gemini transport and strict response parser; no controller contains AI logic. */
@Service
public class GeminiApiClient implements GeminiClient {
    private final GeminiProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    public GeminiApiClient(GeminiProperties properties) { this.properties = properties; }

    @Override
    public List<AiCompanyCandidate> expandCompanies(String prompt) {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new IllegalStateException("Gemini is not configured: gemini.api-key is missing.");
        }
        System.out.println("[GeminiExpansion] Prompt: " + prompt);
        Exception lastFailure = null;
        for (int attempt = 1; attempt <= properties.getMaxRetries(); attempt++) {
            try {
                String url = "https://integrate.api.nvidia.com/v1/chat/completions";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(properties.getApiKey());
                Map<String, Object> body = Map.of(
                        "model", properties.getModel(),
                        "messages", List.of(Map.of("role", "user", "content", prompt)),
                        "temperature", 1,
                        "top_p", 1,
                        "max_tokens", 16384,
                        "seed", 42
                );
                String response = restTemplate.postForObject(url, new HttpEntity<>(body, headers), String.class);
                System.out.println("[AIExpansion] Response: " + response);
                return parseCandidates(response);
            } catch (Exception e) {
                lastFailure = e;
                System.err.println("[AIExpansion] Attempt " + attempt + " failed: " + e.getMessage());
            }
        }
        throw new IllegalStateException("AI expansion failed after " + properties.getMaxRetries() + " attempts.", lastFailure);
    }

    private List<AiCompanyCandidate> parseCandidates(String response) throws Exception {
        JsonNode root = objectMapper.readTree(response);
        JsonNode choices = root.path("choices");
        if (!choices.isArray() || choices.isEmpty()) throw new IllegalArgumentException("AI returned no content.");
        String text = choices.get(0).path("message").path("content").asText();
        JsonNode companies = objectMapper.readTree(stripCodeFence(text)).path("companies");
        if (!companies.isArray()) throw new IllegalArgumentException("AI response does not contain a companies array.");
        List<AiCompanyCandidate> candidates = new ArrayList<>();
        for (JsonNode company : companies) {
            String name = company.path("name").asText().trim();
            String reason = company.path("reason").asText().trim();
            String website = company.path("officialWebsite").asText().trim();
            if (!name.isBlank() && !reason.isBlank() && !website.isBlank()) candidates.add(new AiCompanyCandidate(name, reason, website));
        }
        return candidates;
    }

    private String stripCodeFence(String text) {
        return text.replaceFirst("^```(?:json)?\\s*", "").replaceFirst("\\s*```$", "").trim();
    }
}
