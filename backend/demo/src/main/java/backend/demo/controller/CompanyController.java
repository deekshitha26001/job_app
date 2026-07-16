package backend.demo.controller;

import backend.demo.dto.CompanyRequestDto;
import backend.demo.dto.CompanyResponseDto;
import backend.demo.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    public ResponseEntity<List<CompanyResponseDto>> getAllCompanies() {
        return ResponseEntity.ok(companyService.getAllCompanies());
    }

    @PostMapping
    public ResponseEntity<?> addCompany(@RequestBody CompanyRequestDto dto) {
        try {
            CompanyResponseDto company = companyService.addCompany(
                    dto.getName(), dto.getAtsApiUrl(), dto.getCareerPageUrl(), dto.getLogoUrl()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(company);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to add company: " + e.getMessage()));
        }
    }

    @PostMapping("/bulk")
    public ResponseEntity<?> addCompaniesBulk(@RequestBody List<CompanyRequestDto> dtos) {
        try {
            List<CompanyResponseDto> saved = dtos.stream()
                    .map(dto -> companyService.addCompany(
                            dto.getName(), dto.getAtsApiUrl(), dto.getCareerPageUrl(), dto.getLogoUrl()
                    ))
                    .toList();
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Imported " + saved.size() + " companies",
                    "companies", saved
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Bulk import failed: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCompany(@PathVariable UUID id) {
        try {
            companyService.deleteCompany(id);
            return ResponseEntity.ok(Map.of("message", "Company deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to delete company"));
        }
    }
}
