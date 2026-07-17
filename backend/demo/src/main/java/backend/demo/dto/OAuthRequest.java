package backend.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OAuthRequest {
    /** The authorization code received from the OAuth provider's redirect. */
    private String code;

    /** The redirect URI used when initiating the OAuth flow (must match exactly). */
    private String redirectUri;
}
