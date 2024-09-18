package com.example.application.service;

import com.example.application.entity.User;

public interface UserService {

    public User findUserByUsernameAndPassword(String username);
}
