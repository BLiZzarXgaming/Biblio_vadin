package com.example.application.service.implementation;

import com.example.application.entity.DTO.UserDto;
import com.example.application.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserServiceV2 {
    List<UserDto> findAll();
    Optional<UserDto> findById(Long id);
    Optional<UserDto> findByUsername(String username);
    UserDto save(UserDto user);
    void deleteById(Long id);
    UserDto findUserByUsernameAndPassword(String username);
}
