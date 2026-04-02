package com.luv2code.ecommerce;

import com.luv2code.ecommerce.entity.User;
import com.luv2code.ecommerce.service.AuthService;
import com.luv2code.ecommerce.dao.UserRepository;
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

    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_FIRST_NAME = "Test";
    private static final String TEST_LAST_NAME = "User";

    private String uniqueUsername() {
        return "testuser_" + System.nanoTime();
    }

    private String uniqueEmail() {
        return "test_" + System.nanoTime() + "@example.com";
    }

    @Test
    public void testUserRegistration() {
        String username = uniqueUsername();
        String email = uniqueEmail();

        User user = authService.registerUser(username, email, TEST_PASSWORD, TEST_FIRST_NAME, TEST_LAST_NAME);

        assertNotNull(user);
        assertEquals(username, user.getUsername());
        assertEquals(email, user.getEmail());
        assertEquals(TEST_FIRST_NAME, user.getFirstName());
        assertEquals(TEST_LAST_NAME, user.getLastName());
        assertTrue(user.isEnabled());
        assertNotNull(user.getRoles());
        assertTrue(user.getRoles().contains("USER"));
    }

    @Test
    public void testUserRegistrationDuplicateUsername() {
        String username = uniqueUsername();
        authService.registerUser(username, uniqueEmail(), TEST_PASSWORD, TEST_FIRST_NAME, TEST_LAST_NAME);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                authService.registerUser(username, uniqueEmail(), TEST_PASSWORD, TEST_FIRST_NAME, TEST_LAST_NAME)
        );

        assertEquals("Username taken", exception.getMessage());
    }

    @Test
    public void testUserRegistrationDuplicateEmail() {
        String email = uniqueEmail();
        authService.registerUser(uniqueUsername(), email, TEST_PASSWORD, TEST_FIRST_NAME, TEST_LAST_NAME);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                authService.registerUser(uniqueUsername(), email, TEST_PASSWORD, TEST_FIRST_NAME, TEST_LAST_NAME)
        );

        assertEquals("Email already in use", exception.getMessage());
    }

    @Test
    public void testPasswordIsEncoded() {
        User user = authService.registerUser(
                uniqueUsername(), uniqueEmail(), TEST_PASSWORD, TEST_FIRST_NAME, TEST_LAST_NAME);

        assertNotNull(user.getPassword());
        assertNotEquals(TEST_PASSWORD, user.getPassword());
        assertTrue(passwordEncoder.matches(TEST_PASSWORD, user.getPassword()));
    }

    @Test
    public void testGetUserById() {
        User registeredUser = authService.registerUser(
                uniqueUsername(), uniqueEmail(), TEST_PASSWORD, TEST_FIRST_NAME, TEST_LAST_NAME);

        User retrievedUser = authService.getUserById(registeredUser.getId());

        assertNotNull(retrievedUser);
        assertEquals(registeredUser.getUsername(), retrievedUser.getUsername());
        assertEquals(registeredUser.getEmail(), retrievedUser.getEmail());
    }

    @Test
    public void testUpdateUser() {
        User registeredUser = authService.registerUser(
                uniqueUsername(), uniqueEmail(), TEST_PASSWORD, TEST_FIRST_NAME, TEST_LAST_NAME);

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
        User user = authService.registerUser(
                uniqueUsername(), uniqueEmail(), TEST_PASSWORD, TEST_FIRST_NAME, TEST_LAST_NAME);

        assertNotNull(user.getCart());
        assertEquals(user.getId(), user.getCart().getUser().getId());
    }
}
