package br.leetjourney.taskmanager.controller;

import br.leetjourney.taskmanager.dto.request.LoginRequest;
import br.leetjourney.taskmanager.dto.request.RegisterRequest;
import br.leetjourney.taskmanager.dto.response.AuthResponse;
import br.leetjourney.taskmanager.dto.response.UserResponse;
import br.leetjourney.taskmanager.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/auth")
@RestController
public class AuthController {


    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);

    }
}
