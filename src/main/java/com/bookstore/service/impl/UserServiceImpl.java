package com.bookstore.service.impl;

import com.bookstore.dto.UserDto;
import com.bookstore.dto.UserRegistrationDto;
import com.bookstore.entity.Role;
import com.bookstore.entity.User;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.repository.RoleRepository;
import com.bookstore.repository.UserRepository;
import com.bookstore.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void saveUser(UserRegistrationDto registrationDto) {
        log.info("Registering new user with email: {}", registrationDto.getEmail());

        User user = new User();
        user.setFullName(registrationDto.getFullName());
        user.setEmail(registrationDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));

        Role role = roleRepository.findByName("ROLE_CUSTOMER");
        if (role == null) {
            role = createCustomerRole();
        }
        user.setRoles(List.of(role));
        userRepository.save(user);
    }

    @Override
    public User findUserByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    public List<UserDto> findAllUsers() {
        log.debug("Finding all users");
        List<User> users = userRepository.findAll();
        return users.stream()
                .map((user) -> mapToUserDto(user))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user with id: {}", id);
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void updateUser(User user) {
        log.info("Updating user with id: {}", user.getId());
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + user.getId()));

        existingUser.setFullName(user.getFullName());
        existingUser.setPhoneNumber(user.getPhoneNumber());
        existingUser.setAddress(user.getAddress());
        existingUser.setCity(user.getCity());
        existingUser.setZipCode(user.getZipCode());

        // Password updates should be handled separately if needed,
        // but for profile updates we don't encode here to avoid double encoding.
        // If password needs changing, there should be a dedicated flow.
        userRepository.save(existingUser);
    }

    private UserDto mapToUserDto(User user) {
        return new UserDto(user.getId(), user.getFullName(), user.getEmail(), user.getPhoneNumber(),
                user.getCity(), user.getRoles(), user.getCreatedAt());
    }

    private Role createCustomerRole() {
        log.info("Creating ROLE_CUSTOMER because it doesn't exist");
        Role role = new Role();
        role.setName("ROLE_CUSTOMER");
        return roleRepository.save(role);
    }
}
