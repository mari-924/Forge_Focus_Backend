package com.focusforge.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class GithubSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;

    public GithubSuccessHandler(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attrs = oAuth2User.getAttributes();

        String email = (String) attrs.getOrDefault("email", null);
        if (email == null || email.isBlank()) {
            email = (String) attrs.getOrDefault("login", "unknown@users.noreply.github.com");
        }

        String jwt = jwtService.generateToken(email);

        // 🔥 Mobile deep link back into your RN app
        String frontend = "focusforge://auth/callback";
        String location = frontend + "?token=" + URLEncoder.encode(jwt, StandardCharsets.UTF_8);

        response.setStatus(302);
        response.setHeader("Location", location);
    }
}
