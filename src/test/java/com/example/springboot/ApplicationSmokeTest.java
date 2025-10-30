package com.example.springboot;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ApplicationSmokeTest {

    @Autowired
    private HelloController helloController;

    @Test
    void contextLoads() {
        // Ensures the Spring application context loads successfully
    }

    @Test
    void helloControllerBeanExists() {
        assertThat(helloController).isNotNull();
    }
}
