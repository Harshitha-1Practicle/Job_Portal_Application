package com.jobportal.repository;

import com.jobportal.model.Job;
import com.jobportal.model.User;
import org.springframework.lang.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
public interface JobRepository extends JpaRepository<Job, Long> {
    @EntityGraph(attributePaths = "recruiter")
    @Query("select job from Job job")
    List<Job> findAllWithRecruiter();

    @EntityGraph(attributePaths = "recruiter")
    Optional<Job> findJobWithRecruiterById(@NonNull Long id);

    @EntityGraph(attributePaths = "recruiter")
    List<Job> findByRecruiter(User recruiter);

    @EntityGraph(attributePaths = "recruiter")
    List<Job> findByTitleContainingIgnoreCase(String title);

    @EntityGraph(attributePaths = "recruiter")
    List<Job> findByCompanyContainingIgnoreCase(String company);

    @EntityGraph(attributePaths = "recruiter")
    List<Job> findByLocationContainingIgnoreCase(String location);

    @EntityGraph(attributePaths = "recruiter")
    List<Job> findByTitleContainingIgnoreCaseOrSkillsContainingIgnoreCase(String title, String skills);
}
