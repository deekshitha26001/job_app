package backend.demo.discovery.service;

import backend.demo.entity.Company;
import backend.demo.repository.CompanyRepository;
import backend.demo.review.PendingCompany;
import backend.demo.review.PendingCompanyRepository;
import org.springframework.stereotype.Service;

@Service
public class CompanyDuplicateDetector {
    private final CompanyRepository companies;
    private final PendingCompanyRepository pendingCompanies;

    public CompanyDuplicateDetector(CompanyRepository companies, PendingCompanyRepository pendingCompanies) {
        this.companies = companies;
        this.pendingCompanies = pendingCompanies;
    }

    public boolean alreadyKnown(String companyName) {
        String normalized = normalize(companyName);
        return companies.findAll().stream().map(Company::getName).anyMatch(n -> normalize(n).equals(normalized))
                || pendingCompanies.findAll().stream().map(PendingCompany::getCompanyName)
                .anyMatch(n -> normalize(n).equals(normalized));
    }

    public static String normalize(String name) {
        return name == null ? "" : name.toLowerCase().replaceAll("[^a-z0-9]", "");
    }
}
