package backend.demo.controller;

import backend.demo.dto.AtsJobDto;
import backend.demo.entity.Company;
import backend.demo.repository.CompanyRepository;
import backend.demo.service.AtsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final CompanyRepository companyRepository;
    private final AtsService atsService;

    @GetMapping
    public ResponseEntity<List<AtsJobDto>> getJobs() {
        List<Company> companies = companyRepository.findByActiveTrue();

        List<CompletableFuture<List<AtsJobDto>>> futures = companies.stream()
                .map(company -> CompletableFuture.supplyAsync(() -> atsService.fetchJobs(company)))
                .collect(Collectors.toList());

        List<AtsJobDto> allJobs = futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        return ResponseEntity.ok(allJobs);
    }
}
