package br.leetjourney.taskmanager.controller;

import br.leetjourney.taskmanager.dto.response.UserResponse;
import br.leetjourney.taskmanager.dto.response.UserSummaryResponse;
import br.leetjourney.taskmanager.service.TaskService;
import br.leetjourney.taskmanager.service.UserService;
import jakarta.persistence.GeneratedValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/users")

public class UserController {

    private final UserService userService;
    private final TaskService taskService;


    public UserController(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserResponse me(){
        return userService.me();
    }

    @GetMapping("/me/summary")
    public UserSummaryResponse summary(){
        return taskService.summary();
    }


}
