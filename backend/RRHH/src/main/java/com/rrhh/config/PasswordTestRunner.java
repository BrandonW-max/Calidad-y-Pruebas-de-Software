package com.rrhh.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/*@Component
public class PasswordTestRunner
        implements CommandLineRunner {

    private final PasswordEncoder passwordEncoder;

    public PasswordTestRunner(
            PasswordEncoder passwordEncoder
    ) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        String hash =
                passwordEncoder.encode("admin123");

        System.out.println("\nHASH BCrypt:");

        System.out.println(hash);
    }
}*/