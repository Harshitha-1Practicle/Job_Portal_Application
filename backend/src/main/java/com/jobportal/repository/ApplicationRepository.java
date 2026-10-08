package com.jobportal.repository;

import com.jobportal.model.Application;
import com.jobportal.model.Job;
import com.jobportal.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    @EntityGraph(attributePaths = {"job", "job.recruiter", "applicant"})
    Optional<Application> findApplicationWithDetailsById(@NonNull Long id);

    @EntityGraph(attributePaths = {"job", "job.recruiter", "applicant"})
    @Query("select application from Application application")
    List<Application> findAllWithDetails();

    @EntityGraph(attributePaths = {"job", "job.recruiter", "applicant"})
    List<Application> findByApplicant(User applicant);

    List<Application> findByJob(Job job);
    Optional<Application> findByApplicantAndJob(User applicant, Job job);

    @EntityGraph(attributePaths = {"job", "job.recruiter", "applicant"})
    List<Application> findByJobRecruiter(User recruiter);
}
