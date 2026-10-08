package com.jobportal.controller;

import com.jobportal.dto.ApplicationRequest;
import com.jobportal.model.Application;
import com.jobportal.model.User;
import com.jobportal.service.ApplicationService;
import com.jobportal.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

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
        return new ResponseEntity<>(applicationService.applyForJob(Objects.requireNonNull(applicant.getId()), request), HttpStatus.CREATED);
    }

    @GetMapping("/applications")
    public List<Application> getApplications() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User applicant = userService.findByEmail(authentication.getName());
        return applicationService.getApplicationsForUser(Objects.requireNonNull(applicant.getId()));
    }

    @GetMapping("/applications/{id}")
    public Application getApplication(@PathVariable @NonNull Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Application application = applicationService.getApplicationById(id);
        requireApplicantRecruiterOrAdmin(application, authentication);
        return application;
    }

    @PutMapping("/applications/{id}/status")
    public Application updateStatus(@PathVariable @NonNull Long id, @RequestParam String status) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        requireRecruiterOrAdmin(applicationService.getApplicationById(id), authentication);
        return applicationService.updateStatus(id, status);
    }

    @DeleteMapping("/applications/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable @NonNull Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Application application = applicationService.getApplicationById(id);
        User requester = userService.findByEmail(authentication.getName());
        boolean applicant = application.getApplicant() != null
                && application.getApplicant().getId().equals(requester.getId());
        if (!hasAdminRole(authentication) && !applicant) {
            throw new AccessDeniedException("You are not allowed to delete this application");
        }
        applicationService.deleteApplication(id);
        return ResponseEntity.noContent().build();
    }

    private void requireApplicantRecruiterOrAdmin(Application application, Authentication authentication) {
        User requester = userService.findByEmail(authentication.getName());
        boolean applicant = application.getApplicant() != null
                && application.getApplicant().getId().equals(requester.getId());
        boolean recruiter = application.getJob() != null
                && application.getJob().getRecruiter() != null
                && application.getJob().getRecruiter().getId().equals(requester.getId());
        if (!hasAdminRole(authentication) && !applicant && !recruiter) {
            throw new AccessDeniedException("You are not allowed to access this application");
        }
    }

    private void requireRecruiterOrAdmin(Application application, Authentication authentication) {
        User requester = userService.findByEmail(authentication.getName());
        boolean recruiter = application.getJob() != null
                && application.getJob().getRecruiter() != null
                && application.getJob().getRecruiter().getId().equals(requester.getId());
        if (!hasAdminRole(authentication) && !recruiter) {
            throw new AccessDeniedException("You are not allowed to update this application");
        }
    }

    private boolean hasAdminRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }
}
