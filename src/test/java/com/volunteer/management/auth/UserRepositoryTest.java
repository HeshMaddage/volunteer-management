package com.volunteer.management.auth;

import com.volunteer.management.common.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class UserRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void savesAndFindsUserByEmail() {
        User user = User.builder()
                .email("volunteer@example.com")
                .passwordHash("hashed-value")
                .role(Role.VOLUNTEER)
                .build();

        userRepository.save(user);

        assertThat(userRepository.findByEmail("volunteer@example.com"))
                .isPresent()
                .get()
                .satisfies(found -> {
                    assertThat(found.getRole()).isEqualTo(Role.VOLUNTEER);
                    assertThat(found.getCreatedAt()).isNotNull();
                });
    }

    @Test
    void emailUniquenessIsEnforcedAtDbLevel() {
        userRepository.saveAndFlush(User.builder()
                .email("dup@example.com")
                .passwordHash("hash1")
                .role(Role.ADMIN)
                .build());

        User duplicate = User.builder()
                .email("dup@example.com")
                .passwordHash("hash2")
                .role(Role.VOLUNTEER)
                .build();

        assertThrows(DataIntegrityViolationException.class,
                () -> userRepository.saveAndFlush(duplicate));
    }
}
