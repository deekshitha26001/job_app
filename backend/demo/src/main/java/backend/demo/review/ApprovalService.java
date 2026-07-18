package backend.demo.review;

import backend.demo.service.CompanyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ApprovalService {

    private final ReviewService reviewService;
    private final CompanyService companyService;

    public ApprovalService(ReviewService reviewService, CompanyService companyService) {
        this.reviewService = reviewService;
        this.companyService = companyService;
    }

    /**
     * Approve a pending company: registers it in the Company Registry and marks as APPROVED.
     */
    @Transactional
    public PendingCompany approve(UUID id) {
        PendingCompany pending = reviewService.findById(id);
        if (pending.getStatus() != ReviewStatus.PENDING) {
            throw new IllegalStateException("Company is not in PENDING state: " + id);
        }

        // Use the career page URL as the ATS API URL for the registered company.
        String atsApiUrl = pending.getCareerPageUrl() != null ? pending.getCareerPageUrl() : pending.getOfficialWebsite();

        companyService.addCompany(
                pending.getCompanyName(),
                atsApiUrl,
                pending.getCareerPageUrl(),
                null // logoUrl — not available from discovery
        );

        pending.setStatus(ReviewStatus.APPROVED);
        return reviewService.save(pending);
    }

    /**
     * Reject a pending company.
     */
    @Transactional
    public PendingCompany reject(UUID id) {
        PendingCompany pending = reviewService.findById(id);
        if (pending.getStatus() != ReviewStatus.PENDING) {
            throw new IllegalStateException("Company is not in PENDING state: " + id);
        }
        pending.setStatus(ReviewStatus.REJECTED);
        return reviewService.save(pending);
    }

    /**
     * Bulk approve a list of pending companies.
     */
    @Transactional
    public List<UUID> bulkApprove(List<UUID> ids) {
        return ids.stream()
                .map(id -> {
                    try {
                        approve(id);
                        return id;
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(id -> id != null)
                .toList();
    }

    /**
     * Bulk reject a list of pending companies.
     */
    @Transactional
    public List<UUID> bulkReject(List<UUID> ids) {
        return ids.stream()
                .map(id -> {
                    try {
                        reject(id);
                        return id;
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(id -> id != null)
                .toList();
    }
}
