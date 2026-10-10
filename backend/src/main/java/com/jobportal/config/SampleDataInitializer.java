package com.jobportal.config;

import com.jobportal.model.Job;
import com.jobportal.model.Role;
import com.jobportal.model.User;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.UserRepository;
import org.springframework.lang.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Profile("dev")
public class SampleDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final PasswordEncoder passwordEncoder;

    public SampleDataInitializer(UserRepository userRepository, JobRepository jobRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedJobs();
    }

    private void seedUsers() {
        createUserIfAbsent("Admin User",        "admin@jobconnect.com",        "admin123",        "Bangalore", Set.of(Role.ROLE_ADMIN));
        createUserIfAbsent("Recruiter User",    "recruiter@jobconnect.com",    "recruiter123",    "Hyderabad", Set.of(Role.ROLE_RECRUITER));
        createUserIfAbsent("Job Seeker",        "seeker@jobconnect.com",       "seeker123",       "Chennai",   Set.of(Role.ROLE_JOB_SEEKER));
        createUserIfAbsent("HR Manager",        "hr@jobconnect.com",           "hr123",           "Mumbai",    Set.of(Role.ROLE_HR_MANAGER));
        createUserIfAbsent("Interviewer",       "interviewer@jobconnect.com",  "interviewer123",  "Pune",      Set.of(Role.ROLE_INTERVIEWER));
        createUserIfAbsent("Company Admin",     "companyadmin@jobconnect.com", "companyadmin123", "Delhi",     Set.of(Role.ROLE_COMPANY_ADMIN));
    }

    private void seedJobs() {
        if (jobRepository.count() != 0) return;
        User recruiter = userRepository.findByEmail("recruiter@jobconnect.com").orElseThrow();
        jobRepository.save(buildJob("Java Developer",    "Infosys",       "Bangalore", "Full Time",  "₹12 LPA",       "2-4 years",      "Develop backend services using Java and Spring Boot.",              "Build APIs, integrate with databases, troubleshoot production issues.", "Java, Spring Boot, MySQL, REST APIs", "B.E/B.Tech in Computer Science",                        "Health insurance, flexible timing, learning stipend", recruiter));
        jobRepository.save(buildJob("Frontend Developer", "TCS",          "Hyderabad", "Full Time",  "₹9 LPA",        "1-3 years",      "Create interactive user interfaces for enterprise products.",       "Build UI components and work closely with design team.",               "React, JavaScript, CSS, UI Design",  "Bachelor's degree in IT or related field",              "Career growth, remote flexibility, bonus schemes",    recruiter));
        jobRepository.save(buildJob("Python Developer",  "Wipro",         "Chennai",   "Full Time",  "₹10 LPA",       "2 years",        "Work on automation and backend systems using Python.",              "Build scripts and maintain API integrations.",                         "Python, Django, PostgreSQL, APIs",   "Bachelor's degree in Computer Science",                 "Paid training, performance bonus, work-life balance", recruiter));
        jobRepository.save(buildJob("Software Intern",   "Tech Solutions", "Remote",   "Internship", "₹20,000/month", "Freshers welcome", "Internship focused on web application development and testing.", "Learn, contribute, and deliver project modules under guidance.",       "HTML, CSS, JavaScript, Git",         "Students pursuing graduation or recently graduated",    "Mentorship, certificate, PPO opportunity",            recruiter));
    }

    @NonNull
    private Job buildJob(String title, String company, String location, String jobType,
                         String salary, String experience, String description,
                         String responsibilities, String skills, String qualifications,
                         String benefits, User recruiter) {
        Job job = new Job();
        job.setTitle(title);
        job.setCompany(company);
        job.setLocation(location);
        job.setJobType(jobType);
        job.setSalary(salary);
        job.setExperience(experience);
        job.setDescription(description);
        job.setResponsibilities(responsibilities);
        job.setSkills(skills);
        job.setQualifications(qualifications);
        job.setBenefits(benefits);
        job.setRecruiter(recruiter);
        return job;
    }

    private void createUserIfAbsent(String name, String email, String rawPassword, String location, Set<Role> roles) {
        if (userRepository.findByEmail(email).isPresent()) return;
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setLocation(location);
        user.setRoles(roles);
        userRepository.save(user);
    }
}