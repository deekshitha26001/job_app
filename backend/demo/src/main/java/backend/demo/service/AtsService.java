package backend.demo.service;

import backend.demo.dto.AtsJobDto;
import backend.demo.entity.AtsProvider;
import backend.demo.entity.Company;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class AtsService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public AtsService(ObjectMapper objectMapper) {
        this.restClient = RestClient.create();
        this.objectMapper = objectMapper;
    }

    public AtsProvider detectProvider(String url) {
        if (url == null) return AtsProvider.UNKNOWN;
        String lower = url.toLowerCase();
        if (lower.contains("greenhouse.io")) return AtsProvider.GREENHOUSE;
        if (lower.contains("lever.co")) return AtsProvider.LEVER;
        if (lower.contains("workday.com") || lower.contains("myworkdayjobs.com")) return AtsProvider.WORKDAY;
        return AtsProvider.UNKNOWN;
    }

    public List<AtsJobDto> fetchJobs(Company company) {
        try {
            return switch (company.getAtsProvider()) {
                case GREENHOUSE -> fetchGreenhouseJobs(company);
                case LEVER -> fetchLeverJobs(company);
                default -> List.of();
            };
        } catch (Exception e) {
            log.warn("Failed to fetch jobs for company '{}': {}", company.getName(), e.getMessage());
            return List.of();
        }
    }

    private List<AtsJobDto> fetchGreenhouseJobs(Company company) throws Exception {
        String url = company.getAtsApiUrl();
        String json = restClient.get()
                .uri(url)
                .retrieve()
                .body(String.class);

        List<AtsJobDto> jobs = new ArrayList<>();
        if (json == null || json.isBlank()) return jobs;

        JsonNode root = objectMapper.readTree(json);
        JsonNode jobsNode = root.path("jobs");

        for (JsonNode job : jobsNode) {
            String location = job.path("location").path("name").asText("");
            String department = "";
            JsonNode depts = job.path("departments");
            if (depts.isArray() && !depts.isEmpty()) {
                department = depts.get(0).path("name").asText("");
            }

            jobs.add(AtsJobDto.builder()
                    .id(job.path("id").asText())
                    .title(job.path("title").asText())
                    .location(location)
                    .department(department)
                    .url(job.path("absolute_url").asText())
                    .companyName(company.getName())
                    .companyLogoUrl(company.getLogoUrl())
                    .atsProvider("GREENHOUSE")
                    .postedAt(job.path("updated_at").asText())
                    .build());
        }
        return jobs;
    }

    private List<AtsJobDto> fetchLeverJobs(Company company) throws Exception {
        String url = company.getAtsApiUrl();
        // Ensure ?mode=json is present
        if (!url.contains("mode=json")) {
            url = url + (url.contains("?") ? "&" : "?") + "mode=json";
        }

        String json = restClient.get()
                .uri(url)
                .retrieve()
                .body(String.class);

        List<AtsJobDto> jobs = new ArrayList<>();
        if (json == null || json.isBlank()) return jobs;

        JsonNode root = objectMapper.readTree(json);
        if (!root.isArray()) return jobs;

        for (JsonNode job : root) {
            String location = job.path("categories").path("location").asText("");
            String department = job.path("categories").path("team").asText("");

            jobs.add(AtsJobDto.builder()
                    .id(job.path("id").asText())
                    .title(job.path("text").asText())
                    .location(location)
                    .department(department)
                    .url(job.path("hostedUrl").asText())
                    .companyName(company.getName())
                    .companyLogoUrl(company.getLogoUrl())
                    .atsProvider("LEVER")
                    .postedAt(String.valueOf(job.path("createdAt").asLong()))
                    .build());
        }
        return jobs;
    }
}
