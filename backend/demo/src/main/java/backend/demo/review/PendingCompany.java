package backend.demo.review;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "pending_companies")
public class PendingCompany {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "official_website", length = 1024)
    private String officialWebsite;

    @Column(name = "career_page_url", length = 1024)
    private String careerPageUrl;

    @Column(name = "detected_ats")
    private String detectedAts;

    @Column(name = "ats_confidence")
    private String atsConfidence;

    @Column(name = "matched_pattern")
    private String matchedPattern;

    @Column(name = "detection_method")
    private String detectionMethod;

    @Column(name = "industry")
    private String industry;

    @Column(name = "country")
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus status = ReviewStatus.PENDING;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createdAt;

    public PendingCompany() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getOfficialWebsite() { return officialWebsite; }
    public void setOfficialWebsite(String officialWebsite) { this.officialWebsite = officialWebsite; }
    public String getCareerPageUrl() { return careerPageUrl; }
    public void setCareerPageUrl(String careerPageUrl) { this.careerPageUrl = careerPageUrl; }
    public String getDetectedAts() { return detectedAts; }
    public void setDetectedAts(String detectedAts) { this.detectedAts = detectedAts; }
    public String getAtsConfidence() { return atsConfidence; }
    public void setAtsConfidence(String atsConfidence) { this.atsConfidence = atsConfidence; }
    public String getMatchedPattern() { return matchedPattern; }
    public void setMatchedPattern(String matchedPattern) { this.matchedPattern = matchedPattern; }
    public String getDetectionMethod() { return detectionMethod; }
    public void setDetectionMethod(String detectionMethod) { this.detectionMethod = detectionMethod; }
    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public ReviewStatus getStatus() { return status; }
    public void setStatus(ReviewStatus status) { this.status = status; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
