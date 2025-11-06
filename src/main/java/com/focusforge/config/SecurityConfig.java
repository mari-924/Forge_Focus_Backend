package com.focusforge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private static final String GOOGLE_CLIENT_ID = System.getenv()
            .getOrDefault("GOOGLE_OAUTH_CLIENT_ID", "583541403083-ip677njslglhptvtpq2fshqpv66g3j7q.apps.googleusercontent.com");

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**",
                                "/ping"
                            ).permitAll()
                        .anyRequest().authenticated()
                )
      
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                        )
                );

        return http.build();
    }
    @Bean
    public JwtDecoder jwtDecoder() {
        // ✅ Use Google's OIDC issuer (handles key rotation & validations)
        NimbusJwtDecoder decoder =
                (NimbusJwtDecoder) JwtDecoders.fromOidcIssuerLocation("https://accounts.google.com");

        // ✅ Add an audience validator for your mobile client id
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer("https://accounts.google.com");
        OAuth2TokenValidator<Jwt> audienceValidator = jwt -> {
            String aud = jwt.getClaimAsString("aud");
            if (GOOGLE_CLIENT_ID.equals(aud)) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "Invalid audience", "https://developers.google.com/identity")
            );
        };

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator));
        return decoder;
    }
}
