package backend.demo.discovery.model;

public class DiscoverySummary {
    private int companiesFound;
    private int submitted;
    private int skipped;
    private int failed;

    public DiscoverySummary() {
    }

    public DiscoverySummary(int companiesFound, int submitted, int skipped, int failed) {
        this.companiesFound = companiesFound;
        this.submitted = submitted;
        this.skipped = skipped;
        this.failed = failed;
    }

    public int getCompaniesFound() { return companiesFound; }
    public void setCompaniesFound(int companiesFound) { this.companiesFound = companiesFound; }
    public int getSubmitted() { return submitted; }
    public void setSubmitted(int submitted) { this.submitted = submitted; }
    public int getSkipped() { return skipped; }
    public void setSkipped(int skipped) { this.skipped = skipped; }
    public int getFailed() { return failed; }
    public void setFailed(int failed) { this.failed = failed; }

    @Override
    public String toString() {
        return "DiscoverySummary{found=" + companiesFound +
               ", submitted=" + submitted +
               ", skipped=" + skipped +
               ", failed=" + failed + "}";
    }
}
