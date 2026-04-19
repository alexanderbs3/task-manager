package br.leetjourney.taskmanager.service;

import br.leetjourney.taskmanager.dto.response.UserResponse;
import br.leetjourney.taskmanager.exception.ResourceNotFoundException;
import br.leetjourney.taskmanager.mapper.UserMapper;
import br.leetjourney.taskmanager.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserMapper userMapper, UserRepository userRepository) {
        this.userMapper = userMapper;
        this.userRepository = userRepository;
    }


    @Transactional(readOnly = true)
    public UserResponse me(){
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));
    }
}
