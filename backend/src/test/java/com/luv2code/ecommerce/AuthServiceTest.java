package com.luv2code.ecommerce;

import com.luv2code.ecommerce.entity.User;
import com.luv2code.ecommerce.service.AuthService;
import com.luv2code.ecommerce.dao.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_FIRST_NAME = "Test";
    private static final String TEST_LAST_NAME = "User";

    @BeforeEach
    public void setUp() {
        // Clean up test data
        userRepository.deleteAll();
    }

    @Test
    public void testUserRegistration() {
        User user = authService.registerUser(TEST_USERNAME, TEST_EMAIL, TEST_PASSWORD, TEST_FIRST_NAME, TEST_LAST_NAME);

        assertNotNull(user);
        assertEquals(TEST_USERNAME, user.getUsername());
        assertEquals(TEST_EMAIL, user.getEmail());
        assertEquals(TEST_FIRST_NAME, user.getFirstName());
        assertEquals(TEST_LAST_NAME, user.getLastName());
        assertTrue(user.isEnabled());
        assertNotNull(user.getRoles());
        assertTrue(user.getRoles().contains("USER"));
    }

    @Test
    public void testUserRegistrationDuplicateUsername() {
        authService.registerUser(TEST_USERNAME, TEST_EMAIL, TEST_PASSWORD, TEST_FIRST_NAME, TEST_LAST_NAME);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.registerUser(TEST_USERNAME, "another@example.com", TEST_PASSWORD, TEST_FIRST_NAME, TEST_LAST_NAME);
        });

        assertEquals("Username taken", exception.getMessage());
    }

    @Test
    public void testUserRegistrationDuplicateEmail() {
        authService.registerUser(TEST_USERNAME, TEST_EMAIL, TEST_PASSWORD, TEST_FIRST_NAME, TEST_LAST_NAME);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.registerUser("anotheruser", TEST_EMAIL, TEST_PASSWORD, TEST_FIRST_NAME, TEST_LAST_NAME);
        });

        assertEquals("Email already in use", exception.getMessage());
    }

    @Test
    public void testPasswordIsEncoded() {
        User user = authService.registerUser(TEST_USERNAME, TEST_EMAIL, TEST_PASSWORD, TEST_FIRST_NAME, TEST_LAST_NAME);

        assertNotNull(user.getPassword());
        assertNotEquals(TEST_PASSWORD, user.getPassword());
        assertTrue(passwordEncoder.matches(TEST_PASSWORD, user.getPassword()));
    }

    @Test
    public void testGetUserById() {
        User registeredUser = authService.registerUser(TEST_USERNAME, TEST_EMAIL, TEST_PASSWORD, TEST_FIRST_NAME, TEST_LAST_NAME);

        User retrievedUser = authService.getUserById(registeredUser.getId());

        assertNotNull(retrievedUser);
        assertEquals(TEST_USERNAME, retrievedUser.getUsername());
        assertEquals(TEST_EMAIL, retrievedUser.getEmail());
    }

    @Test
    public void testUpdateUser() {
        User registeredUser = authService.registerUser(TEST_USERNAME, TEST_EMAIL, TEST_PASSWORD, TEST_FIRST_NAME, TEST_LAST_NAME);

        User updatedUser = authService.updateUser(
                registeredUser.getId(),
                "UpdatedFirst",
                "UpdatedLast",
                "1234567890",
                "123 Main St",
                "New York",
                "NY",
                "10001",
                "USA"
        );

        assertEquals("UpdatedFirst", updatedUser.getFirstName());
        assertEquals("UpdatedLast", updatedUser.getLastName());
        assertEquals("1234567890", updatedUser.getPhoneNumber());
        assertEquals("123 Main St", updatedUser.getAddress());
        assertEquals("New York", updatedUser.getCity());
        assertEquals("NY", updatedUser.getState());
        assertEquals("10001", updatedUser.getZipCode());
        assertEquals("USA", updatedUser.getCountry());
    }

    @Test
    public void testCartCreatedOnUserRegistration() {
        User user = authService.registerUser(TEST_USERNAME, TEST_EMAIL, TEST_PASSWORD, TEST_FIRST_NAME, TEST_LAST_NAME);

        assertNotNull(user.getCart());
        assertEquals(user.getId(), user.getCart().getUser().getId());
    }
}
