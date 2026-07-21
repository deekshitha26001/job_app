package backend.demo.discovery.model;

public class DiscoveryResult {
    private String companyName;
    private String officialWebsite;
    private String careerPage;
    private String logoUrl;
    private String industry;
    private String country;
    private String discoverySource;
    private String discoveryReason;

    public DiscoveryResult() {
    }

    public DiscoveryResult(String companyName, String officialWebsite, String careerPage, String logoUrl, String industry, String country) {
        this.companyName = companyName;
        this.officialWebsite = officialWebsite;
        this.careerPage = careerPage;
        this.logoUrl = logoUrl;
        this.industry = industry;
        this.country = country;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getOfficialWebsite() {
        return officialWebsite;
    }

    public void setOfficialWebsite(String officialWebsite) {
        this.officialWebsite = officialWebsite;
    }

    public String getCareerPage() {
        return careerPage;
    }

    public void setCareerPage(String careerPage) {
        this.careerPage = careerPage;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDiscoverySource() { return discoverySource; }
    public void setDiscoverySource(String discoverySource) { this.discoverySource = discoverySource; }
    public String getDiscoveryReason() { return discoveryReason; }
    public void setDiscoveryReason(String discoveryReason) { this.discoveryReason = discoveryReason; }

    @Override
    public String toString() {
        return "DiscoveryResult{" +
                "companyName='" + companyName + '\'' +
                ", officialWebsite='" + officialWebsite + '\'' +
                ", careerPage='" + careerPage + '\'' +
                ", logoUrl='" + logoUrl + '\'' +
                ", industry='" + industry + '\'' +
                ", country='" + country + '\'' +
                '}';
    }
}
