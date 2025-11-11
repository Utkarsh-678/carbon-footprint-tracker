package com.carbontrack.carbon_footprint_tracker.controller.api;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carbontrack.carbon_footprint_tracker.config.JwtUtil;
import com.carbontrack.carbon_footprint_tracker.dto.AuthRequest;
import com.carbontrack.carbon_footprint_tracker.dto.AuthResponse;
import com.carbontrack.carbon_footprint_tracker.entity.User;
import com.carbontrack.carbon_footprint_tracker.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthRestController {

    private static final Logger log = LoggerFactory.getLogger(AuthRestController.class);
    
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthRestController(AuthenticationManager authenticationManager, 
                            UserService userService, 
                            JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest authRequest) {
        try {
            log.info("Login attempt for user: {}", authRequest.getUsername());
            
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    authRequest.getUsername(), 
                    authRequest.getPassword()
                )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            String token = jwtUtil.generateToken(userDetails);
            
            log.info("Login successful for user: {}", authRequest.getUsername());
            return ResponseEntity.ok(new AuthResponse(token, user));
            
        } catch (BadCredentialsException e) {
            log.warn("Login failed for user: {} - Invalid credentials", authRequest.getUsername());
            Map<String, String> error = new HashMap<>();
            error.put("message", "Invalid username or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            log.error("Login error for user: {} - {}", authRequest.getUsername(), e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", "Login failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User user) {
        try {
            log.info("Registration attempt for user: {}", user.getUsername());
            
            User registeredUser = userService.registerUser(user);
            UserDetails userDetails = userService.loadUserByUsername(registeredUser.getUsername());
            String token = jwtUtil.generateToken(userDetails);
            
            log.info("Registration successful for user: {}", user.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new AuthResponse(token, registeredUser));
                    
        } catch (RuntimeException e) {
            log.warn("Registration failed for user: {} - {}", user.getUsername(), e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            log.error("Registration error for user: {} - {}", user.getUsername(), e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", "Registration failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "auth/login";
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (jwtUtil.validateToken(token)) {
                    String username = jwtUtil.extractUsername(token);
                    User user = userService.findByUsername(username)
                            .orElseThrow(() -> new RuntimeException("User not found"));
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("valid", true);
                    response.put("user", new AuthResponse.UserDto(user));
                    return ResponseEntity.ok(response);
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            
        } catch (Exception e) {
            log.error("Token validation error: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("message", "Token validation failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}