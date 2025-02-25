package raingor.ru.authservice.controllers;

import com.nimbusds.jose.jwk.OctetSequenceKey;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import raingor.ru.authservice.jwt.JwtUtil;

import java.util.Collections;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class JwksController {
    private final JwtUtil jwtUtil;
    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> jwks() {
        OctetSequenceKey jwk = new OctetSequenceKey.Builder(jwtUtil.getSecretKey())
                .keyID("auth-service-key")
                .build();
        return Collections.singletonMap("keys", Collections.singletonList(jwk.toJSONObject()));
    }
}
