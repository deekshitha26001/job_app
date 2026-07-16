package backend.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "companies")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "ats_api_url", nullable = false, length = 1024)
    private String atsApiUrl;

    @Column(name = "career_page_url", length = 1024)
    private String careerPageUrl;

    @Column(name = "logo_url", length = 1024)
    private String logoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "ats_provider")
    private AtsProvider atsProvider;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createdAt;
}
