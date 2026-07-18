package backend.demo.review;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PendingCompanyRepository extends JpaRepository<PendingCompany, UUID> {
    List<PendingCompany> findByStatus(ReviewStatus status);
    List<PendingCompany> findAllByOrderByCreatedAtDesc();
}
