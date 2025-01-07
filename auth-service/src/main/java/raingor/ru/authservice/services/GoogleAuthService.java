package raingor.ru.authservice.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {
    private final WebClient webClient = WebClient.create();

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    public void createBaseRedirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String state = generateSecureState();

        request.getSession().setAttribute("oauth2State", state);

        String googleAuthUrl = UriComponentsBuilder
                .fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", "openid email profile")
                .queryParam("state", state)
                .queryParam("access_type", "offline")
                .build().toUriString();

        response.sendRedirect(googleAuthUrl);
    }

    public ResponseEntity<?> createCallback(String code, String state, HttpServletRequest request) {
        // csrf
        if (!checkState(state, request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid state");
        }

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("code", code);
        formData.add("state", state);
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("redirect_uri", redirectUri);
        formData.add("grant_type", "authorization_code");

        Map<String, Object> tokenResponse = webClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .block();

        String accessToken = (String) tokenResponse.get("access_token");
        String idToken = (String) tokenResponse.get("id_token");
        String refreshToken = (String) tokenResponse.get("refresh_token");

        System.out.println("accessToken: " + accessToken + ", idToken: " + idToken + ", refreshToken: " + refreshToken);

        Map<String, Object> userInfo = fetchGoogleUserInfo(accessToken);

        return ResponseEntity.ok("Google OAuth2 Success. accessToken=" + accessToken);
    }

    // need add validate
    private Map<String, Object> parseToken(String token) {
        return webClient.get()
                .uri("https://www.googleapis.com/oauth2/v3/userinfo")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .block();
    }

    private Map<String, Object> fetchGoogleUserInfo(String accessToken) {
        return webClient.get()
                .uri("https://www.googleapis.com/oauth2/v3/userinfo")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .block();
    }

    // csrf
    private String generateSecureState() {
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    // csrf
    private boolean checkState(String state, HttpServletRequest request) {
        String storedState = (String) request.getSession().getAttribute("oauth2State");

        // delete "used" state
        request.getSession().removeAttribute("oauth2State");

        return storedState != null && storedState.equals(state);
    }
}
