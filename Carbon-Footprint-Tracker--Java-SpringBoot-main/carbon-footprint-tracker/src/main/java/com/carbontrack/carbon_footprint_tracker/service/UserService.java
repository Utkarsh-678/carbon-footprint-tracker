package com.carbontrack.carbon_footprint_tracker.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carbontrack.carbon_footprint_tracker.entity.User;
import com.carbontrack.carbon_footprint_tracker.repository.UserRepository;

@Service
@Transactional
public class UserService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found with username: {}", username);
                    return new UsernameNotFoundException("User not found with username: " + username);
                });
        
        log.debug("User found: {} with role: {}", user.getUsername(), user.getRole());
        return user;
    }

    public User registerUser(User user) {
        log.info("Attempting to register new user: {}", user.getUsername());
        
        if (userRepository.existsByUsername(user.getUsername())) {
            log.warn("Registration failed - username already exists: {}", user.getUsername());
            throw new RuntimeException("Username '" + user.getUsername() + "' is already taken");
        }
        
        if (userRepository.existsByEmail(user.getEmail())) {
            log.warn("Registration failed - email already exists: {}", user.getEmail());
            throw new RuntimeException("Email '" + user.getEmail() + "' is already registered");
        }
        
        String originalPassword = user.getPassword();
        user.setPassword(passwordEncoder.encode(originalPassword));
        
        user.setRole(User.Role.USER);
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        
        User savedUser = userRepository.save(user);
        log.info("User successfully registered: {} with ID: {}", savedUser.getUsername(), savedUser.getId());
        
        return savedUser;
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        log.debug("Finding user by username: {}", username);
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        log.debug("Finding user by ID: {}", id);
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        log.debug("Retrieving all users");
        return userRepository.findAll();
    }

    public User updateUser(User user) {
        log.info("Updating user: {}", user.getUsername());
        
        Optional<User> existingUser = userRepository.findById(user.getId());
        if (existingUser.isEmpty()) {
            log.error("Cannot update - user not found with ID: {}", user.getId());
            throw new RuntimeException("User not found with ID: " + user.getId());
        }
        
        User existing = existingUser.get();
        
        existing.setFullName(user.getFullName());
        existing.setEmail(user.getEmail());
        existing.setEnabled(user.isEnabled());
        
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            if (!user.getPassword().startsWith("$2a$")) {
                existing.setPassword(passwordEncoder.encode(user.getPassword()));
                log.debug("Password updated for user: {}", user.getUsername());
            }
        }
        
        User updatedUser = userRepository.save(existing);
        log.info("User successfully updated: {}", updatedUser.getUsername());
        
        return updatedUser;
    }

    public void deleteUser(Long userId) {
        log.info("Deleting user with ID: {}", userId);
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            log.error("Cannot delete - user not found with ID: {}", userId);
            throw new RuntimeException("User not found with ID: " + userId);
        }
        
        User user = userOpt.get();
        String username = user.getUsername();
        
        userRepository.deleteById(userId);
        log.info("User successfully deleted: {}", username);
    }

    @Transactional(readOnly = true)
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    @Transactional(readOnly = true)
    public long getUserCount() {
        return userRepository.count();
    }

    public void createDefaultAdminUser() {
        log.info("Checking for default admin user");
        
        if (!userRepository.existsByUsername("admin")) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@carbontracker.com");
            adminUser.setFullName("System Administrator");
            adminUser.setPassword("admin123");
            adminUser.setRole(User.Role.ADMIN);
            adminUser.setEnabled(true);
            adminUser.setCreatedAt(LocalDateTime.now());
            
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            userRepository.save(adminUser);
            
            log.info("Default admin user created successfully");
        } else {
            log.debug("Default admin user already exists");
        }
    }
}