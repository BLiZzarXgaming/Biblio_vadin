package com.example.application.service;

import com.example.application.entity.DTO.UserDto;
import com.example.application.entity.Mapper.UserMapper;
import com.example.application.repository.UserRepositoryV2;
import com.example.application.service.implementation.UserServiceV2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImplV2 implements UserServiceV2 {
    private final UserRepositoryV2 userRepository;
    private final UserMapper userMapper;

    public UserServiceImplV2(UserRepositoryV2 userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public List<UserDto> findAll() {
        return userRepository.findAll().stream().map(userMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public Optional<UserDto> findById(Long id) {
        return userRepository.findById(id).map(userMapper::toDto);
    }

    @Override
    public Optional<UserDto> findByUsername(String username) {
        return userRepository.findByUsername(username).map(userMapper::toDto);
    }

    @Override
    public UserDto save(UserDto user) {
        return userMapper.toDto(userRepository.save(userMapper.toEntity(user)));
    }

    @Override
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public UserDto findUserByUsernameAndPassword(String username) {
        return userMapper.toDto(userRepository.findUserByUsernameAndPassword(username));
    }
}
