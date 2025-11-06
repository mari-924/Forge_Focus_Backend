
package com.focusforge.repositories;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class UserRepositoryTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void contextLoads_repositoryBeanPresent() {
        // Verify the UserRepository present in the application context
        assertNotNull(context.getBean(UserRepository.class), "UserRepository bean should be present in context");
        assertNotNull(userRepository, "Autowired UserRepository should not be null");
    }

    @Test
    public void repository_isJpaRepository() {
        
        assertTrue(JpaRepository.class.isAssignableFrom(userRepository.getClass()) 
                   || implementsJpaRepositoryInterface(userRepository),
                   "UserRepository should implement JpaRepository");
    }

    private boolean implementsJpaRepositoryInterface(Object repo) {
        for (Class<?> iface : repo.getClass().getInterfaces()) {
            if (JpaRepository.class.isAssignableFrom(iface)) {
                return true;
            }
        }
        return false;
    }
}