package com.example.application.service.implementation;

import com.example.application.entity.User;
import com.example.application.repository.UserRepository;
import com.example.application.service.UserService;

public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User findUserByUsernameAndPassword(String username) {
        return userRepository.findUserByUsernameAndPassword(username);
    }
}
