package backend.demo.service;

import backend.demo.dto.CompanyResponseDto;
import backend.demo.entity.AtsProvider;
import backend.demo.entity.Company;
import backend.demo.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final AtsService atsService;

    public CompanyResponseDto addCompany(String name, String atsApiUrl, String careerPageUrl, String logoUrl) {
        AtsProvider provider = atsService.detectProvider(atsApiUrl);
        Company company = Company.builder()
                .name(name)
                .atsApiUrl(atsApiUrl)
                .careerPageUrl(careerPageUrl)
                .logoUrl(logoUrl)
                .atsProvider(provider)
                .build();
        Company saved = companyRepository.save(company);
        return toDto(saved);
    }

    public List<CompanyResponseDto> getAllCompanies() {
        return companyRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public void deleteCompany(UUID id) {
        companyRepository.deleteById(id);
    }

    private CompanyResponseDto toDto(Company company) {
        return CompanyResponseDto.builder()
                .id(company.getId())
                .name(company.getName())
                .atsApiUrl(company.getAtsApiUrl())
                .careerPageUrl(company.getCareerPageUrl())
                .logoUrl(company.getLogoUrl())
                .atsProvider(company.getAtsProvider() != null ? company.getAtsProvider().name() : "UNKNOWN")
                .active(company.isActive())
                .createdAt(company.getCreatedAt())
                .build();
    }
}
