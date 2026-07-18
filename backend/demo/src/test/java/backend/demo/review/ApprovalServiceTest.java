package backend.demo.review;

import backend.demo.service.CompanyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ApprovalServiceTest {

    private ApprovalService approvalService;
    private ReviewService reviewService;
    private CompanyService companyService;
    private PendingCompanyRepository repository;

    @BeforeEach
    void setUp() {
        repository = mock(PendingCompanyRepository.class);
        companyService = mock(CompanyService.class);
        reviewService = new ReviewService(repository);
        approvalService = new ApprovalService(reviewService, companyService);
    }

    @Test
    void testApprove_callsCompanyServiceAndSetsStatusApproved() {
        UUID id = UUID.randomUUID();
        PendingCompany pending = new PendingCompany();
        pending.setId(id);
        pending.setCompanyName("TestCo");
        pending.setCareerPageUrl("https://boards.greenhouse.io/testco");
        pending.setOfficialWebsite("https://testco.com");
        pending.setStatus(ReviewStatus.PENDING);

        when(repository.findById(id)).thenReturn(Optional.of(pending));
        when(repository.save(any(PendingCompany.class))).thenAnswer(inv -> inv.getArgument(0));

        PendingCompany result = approvalService.approve(id);

        verify(companyService, times(1)).addCompany(
                eq("TestCo"),
                eq("https://boards.greenhouse.io/testco"),
                eq("https://boards.greenhouse.io/testco"),
                isNull()
        );
        assertEquals(ReviewStatus.APPROVED, result.getStatus());
    }

    @Test
    void testReject_setsStatusRejected() {
        UUID id = UUID.randomUUID();
        PendingCompany pending = new PendingCompany();
        pending.setId(id);
        pending.setCompanyName("SpamBlog");
        pending.setStatus(ReviewStatus.PENDING);

        when(repository.findById(id)).thenReturn(Optional.of(pending));
        when(repository.save(any(PendingCompany.class))).thenAnswer(inv -> inv.getArgument(0));

        PendingCompany result = approvalService.reject(id);

        verify(companyService, never()).addCompany(any(), any(), any(), any());
        assertEquals(ReviewStatus.REJECTED, result.getStatus());
    }

    @Test
    void testApprove_alreadyApproved_throwsException() {
        UUID id = UUID.randomUUID();
        PendingCompany pending = new PendingCompany();
        pending.setId(id);
        pending.setStatus(ReviewStatus.APPROVED);

        when(repository.findById(id)).thenReturn(Optional.of(pending));

        assertThrows(IllegalStateException.class, () -> approvalService.approve(id));
    }
}
