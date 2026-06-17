package com.booksorderingsystem.config;

import com.booksorderingsystem.entity.Role;
import com.booksorderingsystem.entity.User;
import com.booksorderingsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        createUserIfNotExists("admin", "1234", Role.ADMIN);
        createUserIfNotExists("user", "1234", Role.USER);
    }

    private void createUserIfNotExists(String username, String password, Role role) {
        if (userRepository.existsByUsername(username)) {
            return;
        }

        User user = User.builder()
                .username(username)
                .password(password)
                .role(role)
                .build();

        userRepository.save(user);
    }
}