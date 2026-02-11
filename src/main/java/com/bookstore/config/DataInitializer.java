package com.bookstore.config;

import com.bookstore.entity.Role;
import com.bookstore.entity.User;
import com.bookstore.repository.RoleRepository;
import com.bookstore.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            Role adminRole = roleRepository.findByName("ROLE_ADMIN");
            if (adminRole == null) {
                adminRole = new Role("ROLE_ADMIN");
                roleRepository.save(adminRole);
            }

            Role customerRole = roleRepository.findByName("ROLE_CUSTOMER");
            if (customerRole == null) {
                customerRole = new Role("ROLE_CUSTOMER");
                roleRepository.save(customerRole);
            }

            if (userRepository.findByEmail("admin@bookstore.com").isEmpty()) {
                User admin = new User();
                admin.setFullName("Admin User");
                admin.setEmail("admin@bookstore.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setAddress("System Admin Address");
                admin.setRoles(Arrays.asList(adminRole));
                userRepository.save(admin);
            }

        };
    }
}
