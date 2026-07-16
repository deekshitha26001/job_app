package backend.demo.dto;

import lombok.Data;

@Data
public class CompanyRequestDto {
    private String name;
    private String atsApiUrl;
    private String careerPageUrl;
    private String logoUrl;
}
