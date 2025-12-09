package com.focusforge.ping;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Lightweight periodic ping to keep Heroku dynos warm. Disabled unless ping.enabled=true.
 */
@Component
@Slf4j
public class HerokuPinger {

    private final boolean enabled;
    private final List<String> targets;
    private final WebClient client;

    public HerokuPinger(
            @Value("${ping.enabled:false}") boolean enabled,
            @Value("${ping.urls:}") String urls,
            WebClient.Builder builder
    ) {
        this.enabled = enabled;
        this.targets = Arrays.stream(urls.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        this.client = builder.build();
    }

    @Scheduled(initialDelayString = "${ping.initial-delay-ms:120000}", fixedDelayString = "${ping.delay-ms:300000}")
    public void ping() {
        if (!enabled || targets.isEmpty()) {
            return; // no-op when disabled or not configured
        }

        for (String url : targets) {
            client.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .onErrorResume(ex -> {
                        log.warn("Ping to {} failed: {}", url, ex.getMessage());
                        return Mono.empty();
                    })
                    .subscribe(body -> log.debug("Ping to {} ok ({} chars)", url, body.length()));
        }
    }
}
