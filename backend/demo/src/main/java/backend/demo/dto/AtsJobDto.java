package backend.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AtsJobDto {
    private String id;
    private String title;
    private String location;
    private String department;
    private String url;
    private String companyName;
    private String companyLogoUrl;
    private String atsProvider;
    private String postedAt;
}
