package raingor.ru.authservice.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import raingor.ru.authservice.services.GoogleAuthService;

import java.io.IOException;

@RestController
@RequestMapping("/auth/google")
@RequiredArgsConstructor
public class GoogleAuthController {
    private final GoogleAuthService googleAuthService;

    @GetMapping("/redirect")
    public void redirect(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        googleAuthService.createBaseRedirect(request, response);
    }

    @GetMapping("/callback")
    public ResponseEntity<?> callback(@RequestParam("code") String code,
                                      @RequestParam("state") String state,
                                      HttpServletRequest request) {
        return googleAuthService.createCallback(code, state, request);
    }

}
