package com.jobportal.controller;

import com.jobportal.dto.JobRequest;
import com.jobportal.model.Job;
import com.jobportal.model.User;
import com.jobportal.service.JobService;
import com.jobportal.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api")
public class JobController {

    private final JobService jobService;
    private final UserService userService;

    public JobController(JobService jobService, UserService userService) {
        this.jobService = jobService;
        this.userService = userService;
    }

    @GetMapping("/jobs")
    public List<Job> getAllJobs() {
        return jobService.getAllJobs();
    }

    @GetMapping("/jobs/{id}")
    public Job getJobById(@PathVariable @NonNull Long id) {
        return jobService.getJobById(id);
    }

    @GetMapping("/jobs/search")
    public List<Job> searchJobs(@RequestParam(required = false) String keyword) {
        return jobService.searchJobs(keyword);
    }

    @PostMapping("/jobs")
    public ResponseEntity<Job> createJob(@Valid @RequestBody JobRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User recruiter = userService.findByEmail(email);
        return new ResponseEntity<>(jobService.createJob(Objects.requireNonNull(recruiter.getId()), request), HttpStatus.CREATED);
    }

    @PutMapping("/jobs/{id}")
    public ResponseEntity<Job> updateJob(@PathVariable @NonNull Long id, @Valid @RequestBody JobRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User recruiter = userService.findByEmail(email);
        return ResponseEntity.ok(jobService.updateJob(id, Objects.requireNonNull(recruiter.getId()), request));
    }

    @DeleteMapping("/jobs/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable @NonNull Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User recruiter = userService.findByEmail(email);
        jobService.deleteJob(id, Objects.requireNonNull(recruiter.getId()));
        return ResponseEntity.noContent().build();
    }
}
