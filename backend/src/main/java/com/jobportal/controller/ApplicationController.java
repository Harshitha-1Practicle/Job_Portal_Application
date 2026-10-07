package com.jobportal.controller;

import com.jobportal.dto.ApplicationRequest;
import com.jobportal.model.Application;
import com.jobportal.model.User;
import com.jobportal.service.ApplicationService;
import com.jobportal.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final UserService userService;

    public ApplicationController(ApplicationService applicationService, UserService userService) {
        this.applicationService = applicationService;
        this.userService = userService;
    }

    @PostMapping("/applications")
    public ResponseEntity<Application> createApplication(@Valid @RequestBody ApplicationRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User applicant = userService.findByEmail(authentication.getName());
        return new ResponseEntity<>(applicationService.applyForJob(applicant.getId(), request), HttpStatus.CREATED);
    }

    @GetMapping("/applications")
    public List<Application> getApplications() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User applicant = userService.findByEmail(authentication.getName());
        return applicationService.getApplicationsForUser(applicant.getId());
    }

    @GetMapping("/applications/{id}")
    public Application getApplication(@PathVariable Long id) {
        return applicationService.getApplicationById(id);
    }

    @PutMapping("/applications/{id}/status")
    public Application updateStatus(@PathVariable Long id, @RequestParam String status) {
        return applicationService.updateStatus(id, status);
    }

    @DeleteMapping("/applications/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id) {
        applicationService.deleteApplication(id);
        return ResponseEntity.noContent().build();
    }
}
