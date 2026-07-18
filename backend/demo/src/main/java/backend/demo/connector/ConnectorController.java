package backend.demo.connector;

import backend.demo.connector.dto.CollectedJob;
import backend.demo.connector.exception.ConnectorException;
import backend.demo.entity.Company;
import backend.demo.repository.CompanyRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/connectors")
public class ConnectorController {

    private final JobConnectorFactory connectorFactory;
    private final CompanyRepository companyRepository;

    public ConnectorController(JobConnectorFactory connectorFactory, CompanyRepository companyRepository) {
        this.connectorFactory = connectorFactory;
        this.companyRepository = companyRepository;
    }

    /**
     * Manually triggers a job collection for an approved company.
     * Returns the collected jobs in memory without storing them.
     */
    @PostMapping("/collect/{companyId}")
    public ResponseEntity<?> collectJobs(@PathVariable UUID companyId) {
        try {
            Company company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new IllegalArgumentException("Company not found"));
            
            if (company.getAtsProvider() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Company has no ATS provider assigned."));
            }

            JobConnector connector = connectorFactory.getConnector(company.getAtsProvider());
            List<CollectedJob> jobs = connector.fetchJobs(company);
            
            return ResponseEntity.ok(jobs);
            
        } catch (ConnectorException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage(), "type", e.getClass().getSimpleName()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "An unexpected error occurred: " + e.getMessage()));
        }
    }
}
