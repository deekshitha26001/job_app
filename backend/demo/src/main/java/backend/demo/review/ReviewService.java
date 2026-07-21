package backend.demo.review;

import backend.demo.ats.model.AtsDetectionResult;
import backend.demo.discovery.model.DiscoveryResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ReviewService {

    private final PendingCompanyRepository repository;

    public ReviewService(PendingCompanyRepository repository) {
        this.repository = repository;
    }

    /**
     * Submit a discovered company (with ATS detection result) to the review queue.
     */
    public PendingCompany submitForReview(DiscoveryResult discovery, AtsDetectionResult atsResult) {
        PendingCompany pending = new PendingCompany();
        pending.setCompanyName(discovery.getCompanyName());
        pending.setOfficialWebsite(discovery.getOfficialWebsite());
        pending.setCareerPageUrl(discovery.getCareerPage());
        pending.setIndustry(discovery.getIndustry());
        pending.setCountry(discovery.getCountry());
        pending.setDiscoverySource(discovery.getDiscoverySource());
        pending.setDiscoveryReason(discovery.getDiscoveryReason());

        if (atsResult != null) {
            pending.setDetectedAts(atsResult.getDetectedAts() != null ? atsResult.getDetectedAts().name() : "UNKNOWN");
            pending.setAtsConfidence(atsResult.getConfidence() != null ? atsResult.getConfidence().name() : "LOW");
            pending.setMatchedPattern(atsResult.getMatchedPattern());
            pending.setDetectionMethod(atsResult.getDetectionMethod());
        } else {
            pending.setDetectedAts("UNKNOWN");
            pending.setAtsConfidence("LOW");
        }

        pending.setStatus(ReviewStatus.PENDING);
        return repository.save(pending);
    }

    /** Get all PENDING companies. */
    public List<PendingCompany> getPending() {
        return repository.findByStatus(ReviewStatus.PENDING);
    }

    /** Get all companies regardless of status, newest first. */
    public List<PendingCompany> getAll() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    /** Get by specific status. */
    public List<PendingCompany> getByStatus(ReviewStatus status) {
        return repository.findByStatus(status);
    }

    /** Find a specific pending company by ID. */
    public PendingCompany findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("PendingCompany not found: " + id));
    }

    /** Save a pending company (used internally by ApprovalService). */
    public PendingCompany save(PendingCompany pending) {
        return repository.save(pending);
    }
}
