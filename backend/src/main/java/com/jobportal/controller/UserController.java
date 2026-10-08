package com.jobportal.controller;

import com.jobportal.model.User;
import com.jobportal.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable @NonNull Long id, Authentication authentication) {
        requireSelfOrAdmin(id, authentication);
        return userService.findById(id);
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable @NonNull Long id, @RequestBody User user, Authentication authentication) {
        requireSelfOrAdmin(id, authentication);
        return userService.updateUser(id, user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable @NonNull Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    private void requireSelfOrAdmin(Long userId, Authentication authentication) {
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        if (!admin && !userService.findByEmail(authentication.getName()).getId().equals(userId)) {
            throw new AccessDeniedException("You are not allowed to access this user");
        }
    }
}
