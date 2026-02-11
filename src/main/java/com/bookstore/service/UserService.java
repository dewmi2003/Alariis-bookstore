package com.bookstore.service;

import com.bookstore.dto.UserRegistrationDto;
import com.bookstore.entity.User;
import java.util.List;

public interface UserService {
    void saveUser(UserRegistrationDto registrationDto);

    User findUserByEmail(String email);

    List<UserRegistrationDto> findAllUsers();
}
