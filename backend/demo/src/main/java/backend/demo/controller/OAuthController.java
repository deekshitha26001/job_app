package backend.demo.controller;

import backend.demo.dto.AuthResponse;
import backend.demo.dto.OAuthRequest;
import backend.demo.entity.Role;
import backend.demo.entity.User;
import backend.demo.repository.UserRepository;
import backend.demo.security.JwtService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Handles OAuth 2.0 authorization-code exchanges for Google and GitHub.
 *
 * Flow (both providers):
 *   1. Frontend redirects the user to the provider's auth page.
 *   2. Provider redirects back with ?code=xxx to /auth/callback/{provider} on the frontend.
 *   3. Frontend POSTs { code, redirectUri } to this controller.
 *   4. Controller exchanges the code for an access token, fetches the user's profile,
 *      finds or creates a local User, and returns a JWT.
 *
 * Required environment variables (in application.properties):
 *   oauth.google.client-id     — GOOGLE_CLIENT_ID
 *   oauth.google.client-secret — GOOGLE_CLIENT_SECRET
 *   oauth.github.client-id     — GITHUB_CLIENT_ID
 *   oauth.github.client-secret — GITHUB_CLIENT_SECRET
 */
@RestController
@RequestMapping("/api/auth/oauth")
@RequiredArgsConstructor
@Slf4j
public class OAuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    @Value("${oauth.google.client-id:}")
    private String googleClientId;

    @Value("${oauth.google.client-secret:}")
    private String googleClientSecret;

    @Value("${oauth.github.client-id:}")
    private String githubClientId;

    @Value("${oauth.github.client-secret:}")
    private String githubClientSecret;

    // ── Google ─────────────────────────────────────────────────────────────────

    @PostMapping("/google")
    public ResponseEntity<?> googleOAuth(@RequestBody OAuthRequest request) {
        if (googleClientId.isBlank() || googleClientSecret.isBlank()) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                    .body(Map.of("message", "Google OAuth is not configured on this server. "
                            + "Set oauth.google.client-id and oauth.google.client-secret in application.properties."));
        }

        try {
            // 1. Exchange code for access token
            String tokenJson = RestClient.create()
                    .post()
                    .uri("https://oauth2.googleapis.com/token")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .body("code=" + encode(request.getCode())
                            + "&client_id=" + encode(googleClientId)
                            + "&client_secret=" + encode(googleClientSecret)
                            + "&redirect_uri=" + encode(request.getRedirectUri())
                            + "&grant_type=authorization_code")
                    .retrieve()
                    .body(String.class);

            JsonNode tokenNode = objectMapper.readTree(tokenJson);
            String accessToken = tokenNode.path("access_token").asText();

            if (accessToken.isBlank()) {
                log.warn("Google token exchange returned no access_token: {}", tokenJson);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Google sign-in failed: could not obtain access token."));
            }

            // 2. Fetch user profile
            String userInfoJson = RestClient.create()
                    .get()
                    .uri("https://www.googleapis.com/oauth2/v3/userinfo")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(String.class);

            JsonNode profile = objectMapper.readTree(userInfoJson);
            String providerId = profile.path("sub").asText();
            String email = profile.path("email").asText();
            String name = profile.path("name").asText(email.split("@")[0]);

            if (email.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Could not retrieve email from Google account."));
            }

            // 3. Find or create user
            User user = findOrCreateOAuthUser(email, name, "google", providerId);

            // 4. Issue JWT
            String jwt = jwtService.generateToken(user);
            return ResponseEntity.ok(AuthResponse.builder().token(jwt).build());

        } catch (Exception e) {
            log.error("Google OAuth error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Google sign-in failed. Please try again."));
        }
    }

    // ── GitHub ─────────────────────────────────────────────────────────────────

    @PostMapping("/github")
    public ResponseEntity<?> githubOAuth(@RequestBody OAuthRequest request) {
        if (githubClientId.isBlank() || githubClientSecret.isBlank()) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                    .body(Map.of("message", "GitHub OAuth is not configured on this server. "
                            + "Set oauth.github.client-id and oauth.github.client-secret in application.properties."));
        }

        try {
            // 1. Exchange code for access token.
            //
            // NOTE: redirect_uri is intentionally OMITTED from the token exchange.
            // GitHub's docs state that if redirect_uri is included here it must
            // exactly match the one used in the authorization step AND the one
            // registered in the GitHub OAuth App — any mismatch causes
            // "bad_verification_code". Omitting it is safe and recommended when
            // the App has a single callback URL registered.
            String body = "code=" + encode(request.getCode())
                    + "&client_id=" + encode(githubClientId)
                    + "&client_secret=" + encode(githubClientSecret);

            log.debug("GitHub token exchange: client_id={}, code_prefix={}",
                    githubClientId,
                    request.getCode() != null && request.getCode().length() > 6
                            ? request.getCode().substring(0, 6) + "..." : "<empty>");

            String tokenJson = RestClient.create()
                    .post()
                    .uri("https://github.com/login/oauth/access_token")
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .body(body)
                    .retrieve()
                    .body(String.class);

            log.debug("GitHub token response: {}", tokenJson);

            JsonNode tokenNode = objectMapper.readTree(tokenJson);
            String accessToken = tokenNode.path("access_token").asText();

            // GitHub returns error details in the JSON when the exchange fails
            if (accessToken.isBlank()) {
                String ghError = tokenNode.path("error").asText("");
                String ghDesc  = tokenNode.path("error_description").asText("");
                log.warn("GitHub token exchange failed — error='{}' description='{}' raw='{}'",
                        ghError, ghDesc, tokenJson);
                String userMsg = ghError.isBlank()
                        ? "GitHub sign-in failed: could not obtain access token."
                        : "GitHub sign-in failed: " + ghDesc + " (" + ghError + ")";
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", userMsg));
            }

            // 2. Fetch user profile
            String profileJson = RestClient.create()
                    .get()
                    .uri("https://api.github.com/user")
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/vnd.github+json")
                    .retrieve()
                    .body(String.class);

            JsonNode profile = objectMapper.readTree(profileJson);
            String providerId = profile.path("id").asText();
            String name = profile.path("name").asText(profile.path("login").asText("GitHub User"));
            String email = profile.path("email").asText();

            // GitHub may not expose email publicly — fetch from /user/emails
            if (email.isBlank()) {
                email = fetchGithubPrimaryEmail(accessToken);
            }

            if (email.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Could not retrieve a verified email from GitHub. "
                                + "Please make your email public in GitHub settings, or use email/password sign-up."));
            }

            // 3. Find or create user
            User user = findOrCreateOAuthUser(email, name, "github", providerId);

            // 4. Issue JWT
            String jwt = jwtService.generateToken(user);
            return ResponseEntity.ok(AuthResponse.builder().token(jwt).build());

        } catch (Exception e) {
            log.error("GitHub OAuth error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "GitHub sign-in failed. Please try again."));
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    /**
     * Finds an existing user by email (or provider + providerId),
     * or creates a new OAuth user if none exists.
     */
    private User findOrCreateOAuthUser(String email, String name, String provider, String providerId) {
        Optional<User> existing = userRepository.findByEmail(email);

        if (existing.isPresent()) {
            User u = existing.get();
            // Link provider if not already linked
            if (u.getProvider() == null) {
                u.setProvider(provider);
                u.setProviderId(providerId);
                userRepository.save(u);
            }
            return u;
        }

        User newUser = User.builder()
                .name(name)
                .email(email)
                .password(UUID.randomUUID().toString()) // Dummy password to satisfy DB NOT NULL constraint
                .provider(provider)
                .providerId(providerId)
                .role(Role.USER)
                .build();

        return userRepository.save(newUser);
    }

    /** Fetches the primary verified email from GitHub's /user/emails endpoint. */
    private String fetchGithubPrimaryEmail(String accessToken) {
        try {
            String emailsJson = RestClient.create()
                    .get()
                    .uri("https://api.github.com/user/emails")
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/vnd.github+json")
                    .retrieve()
                    .body(String.class);

            JsonNode emails = objectMapper.readTree(emailsJson);
            // Prefer primary + verified
            for (JsonNode node : emails) {
                if (node.path("primary").asBoolean() && node.path("verified").asBoolean()) {
                    return node.path("email").asText("");
                }
            }
            // Fallback: any verified email
            for (JsonNode node : emails) {
                if (node.path("verified").asBoolean()) {
                    return node.path("email").asText("");
                }
            }
        } catch (Exception e) {
            log.warn("Could not fetch GitHub user emails", e);
        }
        return "";
    }

    /** URL-encodes a string value for use in form-encoded bodies. */
    private static String encode(String value) {
        if (value == null) return "";
        try {
            return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value;
        }
    }
}
