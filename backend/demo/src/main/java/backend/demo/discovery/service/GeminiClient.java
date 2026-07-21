package backend.demo.discovery.service;

import backend.demo.discovery.model.AiCompanyCandidate;

import java.util.List;

public interface GeminiClient {
    List<AiCompanyCandidate> expandCompanies(String prompt);
}
