package com.example.mirea_testing.service;

import com.example.mirea_testing.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final ConcurrentHashMap<String, User> base = new ConcurrentHashMap<>();

    public UserService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;

        base.put("simple_user", new User(
                "simple_user",
                passwordEncoder.encode("test"),
                "user",
                Instant.now()
        ));

        base.put("admin_user", new User(
                "admin_user",
                passwordEncoder.encode("test"),
                "admin",
                Instant.now()
        ));
    }

    public User createUser(String username, String password) {
        User newUser = new User(
                username,
                passwordEncoder.encode(password),
                "user",
                Instant.now()
        );

        base.put(username, newUser);
        return newUser;
    }

    public Optional<User> getUserByUsername(String username) {
        return Optional.ofNullable(base.get(username));
    }
}
