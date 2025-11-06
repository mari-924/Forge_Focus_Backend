// src/main/java/com/example/springboot/config/GoogleVerifierConfig.java
package com.focusforge.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class GoogleVerifierConfig {

    @Bean
    public GoogleIdTokenVerifier googleIdTokenVerifier() {
        return new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(
                        "583541403083-ip677njslglhptvtpq2fshqpv66g3j7q.apps.googleusercontent.com"))
                .build();
    }
}
