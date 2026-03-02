package br.com.pixpro.auth_service.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User base;

    @BeforeEach
    void setUp() {
        base = new User();
        base.setId(1L);
        base.setName("Alice");
        base.setEmail("alice@example.com");
        base.setPassword("pw");
        base.setRole(Role.ROLE_USER);
    }

    private User cloneBase() {
        User u = new User();
        u.setId(base.getId());
        u.setName(base.getName());
        u.setEmail(base.getEmail());
        u.setPassword(base.getPassword());
        u.setRole(base.getRole());
        return u;
    }

    @Test
    @DisplayName("equals true for identical values & same instance")
    void equalsTrueForSameValues() {
        assertEquals(base, base); // same instance
        User copy = cloneBase();
        assertEquals(base, copy); // structurally equal
        assertEquals(base.hashCode(), copy.hashCode());
    }

    @Test
    @DisplayName("equals false for null and different type")
    void equalsFalseForNullOrType() {
        assertNotEquals(base, null);
        assertNotEquals(base, "not-a-user");
    }

    @Test
    @DisplayName("equals false when id differs")
    void equalsFalseId() {
        User other = cloneBase();
        other.setId(99L);
        assertNotEquals(base, other);
    }

    @Test
    @DisplayName("equals false when name differs")
    void equalsFalseName() {
        User other = cloneBase();
        other.setName("Bob");
        assertNotEquals(base, other);
    }

    @Test
    @DisplayName("equals false when email differs")
    void equalsFalseEmail() {
        User other = cloneBase();
        other.setEmail("alice+alias@example.com");
        assertNotEquals(base, other);
    }

    @Test
    @DisplayName("equals false when password differs")
    void equalsFalsePassword() {
        User other = cloneBase();
        other.setPassword("new");
        assertNotEquals(base, other);
    }

    @Test
    @DisplayName("equals false when role differs")
    void equalsFalseRole() {
        User other = cloneBase();
        other.setRole(Role.ROLE_ADMIN);
        assertNotEquals(base, other);
    }

    @Test
    @DisplayName("toString contains key fields")
    void toStringContainsFields() {
        String ts = base.toString();
        assertTrue(ts.contains("alice@example.com"));
        assertTrue(ts.contains("Alice"));
    }
}
