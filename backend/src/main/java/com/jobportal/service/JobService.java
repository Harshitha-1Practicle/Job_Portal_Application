package com.jobportal.service;

import com.jobportal.dto.JobRequest;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.model.Job;
import com.jobportal.model.User;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.UserRepository;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    public JobService(JobRepository jobRepository, UserRepository userRepository) {
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
    }

    public List<Job> getAllJobs() {
        return jobRepository.findAllWithRecruiter();
    }

    public Job getJobById(@NonNull Long id) {
        return jobRepository.findJobWithRecruiterById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));
    }

    public List<Job> searchJobs(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAllJobs();
        }
        return jobRepository.findByTitleContainingIgnoreCaseOrSkillsContainingIgnoreCase(keyword, keyword);
    }

    public Job createJob(@NonNull Long recruiterId, JobRequest request) {
        User recruiter = userRepository.findById(recruiterId)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter not found with id: " + recruiterId));

        Job job = new Job();
        job.setTitle(request.getTitle());
        job.setCompany(request.getCompany());
        job.setLocation(request.getLocation());
        job.setJobType(request.getJobType());
        job.setSalary(request.getSalary());
        job.setExperience(request.getExperience());
        job.setDescription(request.getDescription());
        job.setResponsibilities(request.getResponsibilities());
        job.setSkills(request.getSkills());
        job.setQualifications(request.getQualifications());
        job.setBenefits(request.getBenefits());
        job.setRecruiter(recruiter);
        return jobRepository.save(job);
    }

    public Job updateJob(@NonNull Long jobId, @NonNull Long recruiterId, JobRequest request) {
        Job existing = getJobById(jobId);
        if (!existing.getRecruiter().getId().equals(recruiterId)) {
            throw new AccessDeniedException("This recruiter is not allowed to update this job");
        }

        existing.setTitle(request.getTitle());
        existing.setCompany(request.getCompany());
        existing.setLocation(request.getLocation());
        existing.setJobType(request.getJobType());
        existing.setSalary(request.getSalary());
        existing.setExperience(request.getExperience());
        existing.setDescription(request.getDescription());
        existing.setResponsibilities(request.getResponsibilities());
        existing.setSkills(request.getSkills());
        existing.setQualifications(request.getQualifications());
        existing.setBenefits(request.getBenefits());

        return jobRepository.save(existing);
    }

    public void deleteJob(@NonNull Long jobId, @NonNull Long recruiterId) {
        Job job = getJobById(jobId);
        if (!job.getRecruiter().getId().equals(recruiterId)) {
            throw new AccessDeniedException("This recruiter is not allowed to delete this job");
        }
        jobRepository.delete(job);
    }

    public List<Job> getRecruiterJobs(@NonNull Long recruiterId) {
        User recruiter = userRepository.findById(recruiterId)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter not found with id: " + recruiterId));
        return jobRepository.findByRecruiter(recruiter);
    }
}
