package backend.demo.ats.controller;

import backend.demo.ats.model.AtsDetectionResult;
import backend.demo.ats.service.AtsDetectorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ats")
public class AtsDetectionController {

    private final AtsDetectorService atsDetectorService;

    public AtsDetectionController(AtsDetectorService atsDetectorService) {
        this.atsDetectorService = atsDetectorService;
    }

    @GetMapping("/detect")
    public ResponseEntity<AtsDetectionResult> detect(
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String officialWebsite,
            @RequestParam String careerPageUrl) {
        AtsDetectionResult result = atsDetectorService.detect(companyName, officialWebsite, careerPageUrl);
        return ResponseEntity.ok(result);
    }
}
