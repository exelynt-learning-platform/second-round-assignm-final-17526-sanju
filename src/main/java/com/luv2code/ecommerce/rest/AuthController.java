package com.luv2code.ecommerce.rest;

import com.luv2code.ecommerce.dto.LoginRequest;
import com.luv2code.ecommerce.dto.RegisterRequest;
import com.luv2code.ecommerce.entity.User;
import com.luv2code.ecommerce.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    private Map<String, Object> errorBody(String error, String code) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", error);
        body.put("code", code);
        return body;
    }

    /**
     * Register a new user
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest req,
            BindingResult binding) {

        if (binding.hasErrors()) {
            Map<String, String> errs = new HashMap<>();
            binding.getFieldErrors().forEach(e ->
                    errs.put(e.getField(), e.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errs);
        }

        try {
            User user = authService.registerUser(
                    req.getUsername(),
                    req.getEmail(),
                    req.getPassword(),
                    req.getFirstName(),
                    req.getLastName()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(user);

        } catch (RuntimeException e) {
            logger.warn("Registration failed: {}", e.getMessage());
            String msg = e.getMessage();
            if (msg != null && msg.contains("Username taken")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(errorBody("Username already taken", "USERNAME_TAKEN"));
            } else if (msg != null && msg.contains("Email already in use")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(errorBody("Email already registered", "EMAIL_TAKEN"));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(errorBody("Registration failed: " + msg, "REGISTRATION_ERROR"));
        }
    }

    /**
     * Login
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        try {
            String token = authService.loginUser(req.getUsername(), req.getPassword());
            Map<String, String> res = new HashMap<>();
            res.put("token", token);
            res.put("tokenType", "Bearer");
            return ResponseEntity.ok(res);

        } catch (RuntimeException e) {
            logger.warn("Login failed for user {}: {}", req.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(errorBody("Invalid username or password", "INVALID_CREDENTIALS"));
        }
    }

    /**
     * Get current authenticated user profile
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<?> me() {
        try {
            User user = authService.getCurrentUser();
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(errorBody("Not authenticated", "NOT_AUTHENTICATED"));
            }
            return ResponseEntity.ok(user);

        } catch (RuntimeException e) {
            logger.error("Error retrieving user profile: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorBody("Failed to retrieve user profile", "PROFILE_ERROR"));
        }
    }

    /**
     * Update user profile
     * PUT /api/auth/update
     */
    @PutMapping("/update")
    public ResponseEntity<?> update(@RequestBody Map<String, String> data) {
        try {
            User current = authService.getCurrentUser();
            if (current == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(errorBody("Not authenticated", "NOT_AUTHENTICATED"));
            }

            User updated = authService.updateUser(
                    current.getId(),
                    data.get("firstName"),
                    data.get("lastName"),
                    data.get("phoneNumber"),
                    data.get("address"),
                    data.get("city"),
                    data.get("state"),
                    data.get("zipCode"),
                    data.get("country")
            );
            return ResponseEntity.ok(updated);

        } catch (RuntimeException e) {
            logger.error("Profile update error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(errorBody("Failed to update profile: " + e.getMessage(), "UPDATE_ERROR"));
        }
    }
}
