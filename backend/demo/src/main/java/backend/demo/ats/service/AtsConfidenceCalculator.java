package backend.demo.ats.service;

import backend.demo.ats.model.AtsConfidence;
import org.springframework.stereotype.Service;

@Service
public class AtsConfidenceCalculator {

    public AtsConfidence calculate(String detectionMethod) {
        if (detectionMethod == null) return AtsConfidence.LOW;
        
        switch (detectionMethod) {
            case "Matched Domain":
            case "Matched Redirect URL":
                return AtsConfidence.HIGH;
            case "Found iframe src":
            case "Found External Resource URL":
                return AtsConfidence.MEDIUM;
            case "Found Embedded JavaScript URL":
                return AtsConfidence.LOW;
            default:
                return AtsConfidence.LOW;
        }
    }
}
