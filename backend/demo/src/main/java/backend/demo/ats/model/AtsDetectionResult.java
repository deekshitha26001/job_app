package backend.demo.ats.model;

public class AtsDetectionResult {
    private AtsProvider detectedAts;
    private String detectionMethod;
    private String matchedPattern;
    private AtsConfidence confidence;

    public AtsDetectionResult() {
    }

    public AtsDetectionResult(AtsProvider detectedAts, String detectionMethod, String matchedPattern, AtsConfidence confidence) {
        this.detectedAts = detectedAts;
        this.detectionMethod = detectionMethod;
        this.matchedPattern = matchedPattern;
        this.confidence = confidence;
    }

    public AtsProvider getDetectedAts() {
        return detectedAts;
    }

    public void setDetectedAts(AtsProvider detectedAts) {
        this.detectedAts = detectedAts;
    }

    public String getDetectionMethod() {
        return detectionMethod;
    }

    public void setDetectionMethod(String detectionMethod) {
        this.detectionMethod = detectionMethod;
    }

    public String getMatchedPattern() {
        return matchedPattern;
    }

    public void setMatchedPattern(String matchedPattern) {
        this.matchedPattern = matchedPattern;
    }

    public AtsConfidence getConfidence() {
        return confidence;
    }

    public void setConfidence(AtsConfidence confidence) {
        this.confidence = confidence;
    }

    @Override
    public String toString() {
        return "AtsDetectionResult{" +
                "detectedAts=" + detectedAts +
                ", detectionMethod='" + detectionMethod + '\'' +
                ", matchedPattern='" + matchedPattern + '\'' +
                ", confidence=" + confidence +
                '}';
    }
}
