package backend.demo.connector;

import backend.demo.connector.dto.CollectedJob;
import backend.demo.entity.AtsProvider;
import backend.demo.entity.Company;

import java.util.List;

public interface JobConnector {
    /**
     * Identifies which ATS provider this connector handles.
     */
    AtsProvider getProvider();

    /**
     * Connects to the ATS and fetches open jobs for the given company.
     * Follows the standard connector pipeline:
     * 1. Validate Company
     * 2. Validate ATS
     * 3. Prepare Request (e.g. Extract Board Token)
     * 4. Fetch Jobs
     * 5. Transform Raw Response
     * 6. Map to CollectedJob DTO
     * 7. Return List<CollectedJob>
     */
    List<CollectedJob> fetchJobs(Company company);
}
