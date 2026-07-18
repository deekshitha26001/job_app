package backend.demo.discovery.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Startup validator for the Company Discovery module.
 *
 * Runs after the application context is fully loaded. If serpapi.key is
 * missing or blank, it logs a clear, actionable error message so the
 * operator knows exactly what to fix — without exposing the key value itself.
 */
@Component
public class DiscoveryStartupValidator {

    @Value("${serpapi.key:}")
    private String serpApiKey;

    @EventListener(ApplicationReadyEvent.class)
    public void validateConfiguration() {
        if (serpApiKey == null || serpApiKey.isBlank()) {
            System.err.println("""
                    ╔══════════════════════════════════════════════════════════════╗
                    ║  [Company Discovery] CONFIGURATION ERROR                     ║
                    ║                                                              ║
                    ║  serpapi.key is missing or empty.                            ║
                    ║                                                              ║
                    ║  The Company Discovery module requires a valid SERP API key  ║
                    ║  to search for companies. Without it, discovery will return  ║
                    ║  zero results.                                               ║
                    ║                                                              ║
                    ║  Fix: Add the following line to application-secrets.properties ║
                    ║       (this file is excluded from Git):                      ║
                    ║                                                              ║
                    ║       serpapi.key=YOUR_SERP_API_KEY                          ║
                    ║                                                              ║
                    ║  Get your key at: https://serpapi.com/manage-api-key         ║
                    ╚══════════════════════════════════════════════════════════════╝
                    """);
        } else {
            System.out.println("[Company Discovery] SERP API key loaded successfully. Discovery module is ready.");
        }
    }
}
