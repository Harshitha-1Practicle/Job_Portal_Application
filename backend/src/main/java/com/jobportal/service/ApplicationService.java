package com.jobportal.service;

import com.jobportal.dto.ApplicationRequest;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.model.Application;
import com.jobportal.model.ApplicationStatus;
import com.jobportal.model.Job;
import com.jobportal.model.User;
import com.jobportal.repository.ApplicationRepository;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;

    public ApplicationService(ApplicationRepository applicationRepository,
                              UserRepository userRepository,
                              JobRepository jobRepository) {
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
    }

    public List<Application> getAllApplications() {
        return applicationRepository.findAll();
    }

    public Application getApplicationById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));
    }

    public List<Application> getApplicationsForUser(Long userId) {
        User applicant = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return applicationRepository.findByApplicant(applicant);
    }

    public Application applyForJob(Long applicantId, ApplicationRequest request) {
        User applicant = userRepository.findById(applicantId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + applicantId));
        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + request.getJobId()));

        if (applicationRepository.findByApplicantAndJob(applicant, job).isPresent()) {
            throw new IllegalArgumentException("You have already applied to this job");
        }

        Application application = new Application();
        application.setApplicant(applicant);
        application.setJob(job);
        application.setResume(request.getResume());
        application.setCoverLetter(request.getCoverLetter());
        application.setStatus(ApplicationStatus.APPLIED);
        return applicationRepository.save(application);
    }

    public Application updateStatus(Long applicationId, String statusValue) {
        Application application = getApplicationById(applicationId);
        ApplicationStatus status = ApplicationStatus.valueOf(statusValue.toUpperCase());
        application.setStatus(status);
        return applicationRepository.save(application);
    }

    public void deleteApplication(Long id) {
        if (!applicationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Application not found with id: " + id);
        }
        applicationRepository.deleteById(id);
    }

    public List<Application> getApplicationsForRecruiter(Long recruiterId) {
        User recruiter = userRepository.findById(recruiterId)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter not found with id: " + recruiterId));
        return applicationRepository.findByJobRecruiter(recruiter);
    }
}
