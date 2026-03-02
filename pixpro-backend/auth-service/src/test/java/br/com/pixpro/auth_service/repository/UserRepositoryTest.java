package br.com.pixpro.auth_service.repository;

import br.com.pixpro.auth_service.model.Role;
import br.com.pixpro.auth_service.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User buildUser(String name, String email) {
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        u.setPassword("secret");
        u.setRole(Role.ROLE_USER);
        return u;
    }

    @Test
    @DisplayName("findByEmail returns user when email exists")
    void findByEmail_WhenExists_ReturnsUser() {
        // arrange
        String email = "alice@example.com";
        userRepository.save(buildUser("Alice", email));

        // act
        Optional<User> found = userRepository.findByEmail(email);

        // assert
        assertTrue(found.isPresent());
        assertEquals(email, found.get().getEmail());
    }

    @Test
    @DisplayName("findByEmail returns empty when email not exists")
    void findByEmail_WhenNotExists_ReturnsEmpty() {
        // act
        Optional<User> found = userRepository.findByEmail("unknown@example.com");

        // assert
        assertTrue(found.isEmpty());
    }
}
