package backend.demo.discovery.model;

/** Validated, structured output from Gemini. */
public record AiCompanyCandidate(String name, String reason, String officialWebsite) { }
