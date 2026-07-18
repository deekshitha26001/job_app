package backend.demo.connector.impl;

import backend.demo.connector.JobConnector;
import backend.demo.connector.dto.CollectedJob;
import backend.demo.connector.exception.InvalidBoardTokenException;
import backend.demo.connector.exception.InvalidCareerPageException;
import backend.demo.connector.exception.JobCollectionException;
import backend.demo.entity.AtsProvider;
import backend.demo.entity.Company;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class GreenhouseConnector implements JobConnector {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public AtsProvider getProvider() {
        return AtsProvider.GREENHOUSE;
    }

    @Override
    public List<CollectedJob> fetchJobs(Company company) {
        // 1 & 2. Validation is handled by Factory and controller
        String careerPage = company.getAtsApiUrl() != null ? company.getAtsApiUrl() : company.getCareerPageUrl();
        if (careerPage == null || careerPage.isBlank()) {
            throw new InvalidCareerPageException("Company " + company.getName() + " has no ATS API or Career Page URL.");
        }

        // 3. Extract Board Token
        String boardToken = extractBoardToken(careerPage);
        if (boardToken == null) {
            throw new InvalidBoardTokenException("Could not extract Greenhouse board token from URL: " + careerPage);
        }

        // 4. Fetch Jobs
        String apiUrl = "https://boards-api.greenhouse.io/v1/boards/" + boardToken + "/jobs";
        Map<String, Object> response;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> resp = restTemplate.getForObject(apiUrl, Map.class);
            response = resp;
        } catch (Exception e) {
            throw new JobCollectionException("Failed to fetch jobs from Greenhouse for board token: " + boardToken, e);
        }

        // 5 & 6 & 7. Transform and Map to DTO
        List<CollectedJob> collectedJobs = new ArrayList<>();
        if (response != null && response.containsKey("jobs")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> jobs = (List<Map<String, Object>>) response.get("jobs");
            for (Map<String, Object> jobMap : jobs) {
                CollectedJob job = new CollectedJob();
                
                Object idObj = jobMap.get("id");
                job.setJobId(idObj != null ? String.valueOf(idObj) : null);
                
                job.setTitle((String) jobMap.get("title"));
                job.setCompany(company.getName());
                
                Object locObj = jobMap.get("location");
                if (locObj instanceof Map) {
                    job.setLocation((String) ((Map<?, ?>) locObj).get("name"));
                }
                
                job.setJobUrl((String) jobMap.get("absolute_url"));
                
                String updatedAt = (String) jobMap.get("updated_at");
                if (updatedAt != null) {
                    try {
                        job.setPostedDate(LocalDateTime.parse(updatedAt, DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                    } catch (Exception e) {
                        // Ignore parse error
                    }
                }
                
                job.setSourceAts(AtsProvider.GREENHOUSE.name());
                job.setSourceCompanyId(String.valueOf(company.getId()));
                
                collectedJobs.add(job);
            }
        }

        // 8. Return List
        return collectedJobs;
    }

    private String extractBoardToken(String url) {
        try {
            // E.g. https://boards.greenhouse.io/phonepe -> phonepe
            // Or https://job-boards.greenhouse.io/phonepe/jobs/123 -> phonepe
            String cleanUrl = url.replaceFirst("^(https?://)?(www\\.)?", "");
            String[] parts = cleanUrl.split("/");
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].contains("greenhouse.io")) {
                    if (i + 1 < parts.length) {
                        String potentialToken = parts[i + 1];
                        if (potentialToken != null && !potentialToken.isBlank() && !potentialToken.contains("?")) {
                            return potentialToken;
                        }
                    }
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}
