package raingor.ru.authservice.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClient;
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
import raingor.ru.authservice.jwt.JwtUtil;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {
    private final WebClient webClient = WebClient.create();
    private final EurekaDiscoveryClient discoveryClient;
    private final JwtUtil jwtUtil;

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

        //get tokens
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
        String email = (String) userInfo.get("email");
        Map<String, Object> userResponse = checkUserExists(email);

        //check in user-service
        if (userResponse != null && userResponse.containsKey("user")) {
            Map<String, Object> user = (Map<String, Object>) userResponse.get("user");
            String userToken = jwtUtil.generateUserToken(user);
            return ResponseEntity.ok("Login successful. Token: " + userToken);
        } else {
            Map<String, Object> newUser = createUserInUserService(userInfo);
            String userToken = jwtUtil.generateUserToken(newUser);
            return ResponseEntity.ok("Registration successful. Token: " + userToken);
        }
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

    public Map<String, Object> fetchGoogleUserInfo(HttpServletRequest request) {
        String accessToken = (String) request.getSession().getAttribute("oauth2State");

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

    // get url user service from Eureka
    private String getUserServiceUrl() {
        List<ServiceInstance> instances = discoveryClient.getInstances("user-service");
        if (instances.isEmpty()) {
            throw new RuntimeException("User service not found");
        }
        return instances.get(0).getUri().toString();
    }

    // check exist user in db
    public Map<String, Object> checkUserExists(String email) {
        String userServiceUrl = getUserServiceUrl();
        return webClient.get()
                .uri(userServiceUrl + "/api/users/check?email=" + email)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .block();
    }

    // create new user
    public Map<String, Object> createUserInUserService(Map<String, Object> userInfo) {
        String userServiceUrl = getUserServiceUrl();
        return webClient.post()
                .uri(userServiceUrl + "/api/users/create")
                .bodyValue(userInfo)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .block();
    }

    // need add validate
    @Deprecated
    private Map<String, Object> parseToken(String token) {
        return webClient.get()
                .uri("https://www.googleapis.com/oauth2/v3/userinfo")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .block();
    }
}
