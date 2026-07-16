package backend.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompanyResponseDto {
    private UUID id;
    private String name;
    private String atsApiUrl;
    private String careerPageUrl;
    private String logoUrl;
    private String atsProvider;
    private boolean active;
    private Date createdAt;
}
