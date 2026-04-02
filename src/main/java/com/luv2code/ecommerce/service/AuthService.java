package com.luv2code.ecommerce.service;

import com.luv2code.ecommerce.entity.User;
import com.luv2code.ecommerce.entity.Cart;
import com.luv2code.ecommerce.dao.UserRepository;
import com.luv2code.ecommerce.dao.CartRepository;
import com.luv2code.ecommerce.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@Transactional
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(String username, String email, String password,
                             String firstName, String lastName) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username taken");
        }

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already in use");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(true);

        Set<String> roles = new HashSet<>();
        roles.add("USER");
        user.setRoles(roles);

        User saved = userRepository.save(user);

        Cart cart = new Cart();
        cart.setUser(saved);
        cart.setCartItems(new HashSet<>());
        Cart savedCart = cartRepository.save(cart);
        saved.setCart(savedCart);

        return saved;
    }

    public String loginUser(String username, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            return jwtUtils.generateJwtToken(authentication);
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Invalid username or password");
        } catch (AuthenticationException e) {
            throw new RuntimeException("Authentication failed");
        }
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            String username = authentication.getName();
            return userRepository.findByUsername(username).orElse(null);
        }
        return null;
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    public User updateUser(Long userId, String firstName, String lastName,
                           String phoneNumber, String address, String city,
                           String state, String zipCode, String country) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (firstName != null)   user.setFirstName(firstName);
        if (lastName != null)    user.setLastName(lastName);
        if (phoneNumber != null) user.setPhoneNumber(phoneNumber);
        if (address != null)     user.setAddress(address);
        if (city != null)        user.setCity(city);
        if (state != null)       user.setState(state);
        if (zipCode != null)     user.setZipCode(zipCode);
        if (country != null)     user.setCountry(country);

        return userRepository.save(user);
    }
}
