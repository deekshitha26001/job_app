package backend.demo.connector.dto;

import java.time.LocalDateTime;

public class CollectedJob {
    private String jobId;
    private String title;
    private String company;
    private String location;
    private String department;
    private String employmentType;
    private LocalDateTime postedDate;
    private String jobUrl;
    private String description;
    private String sourceAts;
    private String sourceCompanyId;

    public CollectedJob() {
    }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    
    public String getEmploymentType() { return employmentType; }
    public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }
    
    public LocalDateTime getPostedDate() { return postedDate; }
    public void setPostedDate(LocalDateTime postedDate) { this.postedDate = postedDate; }
    
    public String getJobUrl() { return jobUrl; }
    public void setJobUrl(String jobUrl) { this.jobUrl = jobUrl; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getSourceAts() { return sourceAts; }
    public void setSourceAts(String sourceAts) { this.sourceAts = sourceAts; }
    
    public String getSourceCompanyId() { return sourceCompanyId; }
    public void setSourceCompanyId(String sourceCompanyId) { this.sourceCompanyId = sourceCompanyId; }
}
