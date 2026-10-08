package com.jobportal.controller;

import com.jobportal.model.Application;
import com.jobportal.model.Job;
import com.jobportal.model.User;
import com.jobportal.service.ApplicationService;
import com.jobportal.service.JobService;
import com.jobportal.service.UserService;
import java.util.Objects;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recruiter")
public class RecruiterController {

    private final JobService jobService;
    private final ApplicationService applicationService;
    private final UserService userService;

    public RecruiterController(JobService jobService, ApplicationService applicationService, UserService userService) {
        this.jobService = jobService;
        this.applicationService = applicationService;
        this.userService = userService;
    }

    @GetMapping("/jobs")
    public List<Job> getRecruiterJobs() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User recruiter = userService.findByEmail(authentication.getName());
        return jobService.getRecruiterJobs(Objects.requireNonNull(recruiter.getId()));
    }

    @GetMapping("/applications")
    public List<Application> getRecruiterApplications() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User recruiter = userService.findByEmail(authentication.getName());
        return applicationService.getApplicationsForRecruiter(Objects.requireNonNull(recruiter.getId()));
    }
}
