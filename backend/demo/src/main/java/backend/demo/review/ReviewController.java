package backend.demo.review;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    private final ReviewService reviewService;
    private final ApprovalService approvalService;

    public ReviewController(ReviewService reviewService, ApprovalService approvalService) {
        this.reviewService = reviewService;
        this.approvalService = approvalService;
    }

    /** Get all pending companies. */
    @GetMapping("/pending")
    public ResponseEntity<List<PendingCompany>> getPending() {
        return ResponseEntity.ok(reviewService.getPending());
    }

    /** Get all companies (any status). */
    @GetMapping
    public ResponseEntity<List<PendingCompany>> getAll() {
        return ResponseEntity.ok(reviewService.getAll());
    }

    /** Get by status: PENDING, APPROVED, REJECTED */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PendingCompany>> getByStatus(@PathVariable ReviewStatus status) {
        return ResponseEntity.ok(reviewService.getByStatus(status));
    }

    /** Approve a single pending company. */
    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable UUID id) {
        try {
            PendingCompany result = approvalService.approve(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** Reject a single pending company. */
    @PostMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable UUID id) {
        try {
            PendingCompany result = approvalService.reject(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** Bulk approve. Body: list of UUIDs. */
    @PostMapping("/bulk-approve")
    public ResponseEntity<?> bulkApprove(@RequestBody List<UUID> ids) {
        List<UUID> approved = approvalService.bulkApprove(ids);
        return ResponseEntity.ok(Map.of("approved", approved.size(), "ids", approved));
    }

    /** Bulk reject. Body: list of UUIDs. */
    @PostMapping("/bulk-reject")
    public ResponseEntity<?> bulkReject(@RequestBody List<UUID> ids) {
        List<UUID> rejected = approvalService.bulkReject(ids);
        return ResponseEntity.ok(Map.of("rejected", rejected.size(), "ids", rejected));
    }
}
