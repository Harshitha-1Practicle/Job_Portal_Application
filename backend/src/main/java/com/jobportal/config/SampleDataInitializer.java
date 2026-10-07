package com.jobportal.config;

import com.jobportal.model.Job;
import com.jobportal.model.Role;
import com.jobportal.model.User;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
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
        if (userRepository.findByEmail("admin@jobconnect.com").isEmpty()) {
            User admin = new User();
            admin.setName("Admin User");
            admin.setEmail("admin@jobconnect.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setLocation("Bangalore");
            admin.setRoles(Set.of(Role.ROLE_ADMIN));
            userRepository.save(admin);
        }

        if (userRepository.findByEmail("recruiter@jobconnect.com").isEmpty()) {
            User recruiter = new User();
            recruiter.setName("Recruiter User");
            recruiter.setEmail("recruiter@jobconnect.com");
            recruiter.setPassword(passwordEncoder.encode("recruiter123"));
            recruiter.setLocation("Hyderabad");
            recruiter.setRoles(Set.of(Role.ROLE_RECRUITER));
            userRepository.save(recruiter);
        }

        if (userRepository.findByEmail("seeker@jobconnect.com").isEmpty()) {
            User seeker = new User();
            seeker.setName("Job Seeker");
            seeker.setEmail("seeker@jobconnect.com");
            seeker.setPassword(passwordEncoder.encode("seeker123"));
            seeker.setLocation("Chennai");
            seeker.setRoles(Set.of(Role.ROLE_JOB_SEEKER));
            userRepository.save(seeker);
        }

        if (jobRepository.count() == 0) {
            User recruiter = userRepository.findByEmail("recruiter@jobconnect.com").orElseThrow();

            Job javaJob = new Job();
            javaJob.setTitle("Java Developer");
            javaJob.setCompany("Infosys");
            javaJob.setLocation("Bangalore");
            javaJob.setJobType("Full Time");
            javaJob.setSalary("₹12 LPA");
            javaJob.setExperience("2-4 years");
            javaJob.setDescription("Develop backend services using Java and Spring Boot.");
            javaJob.setResponsibilities("Build APIs, integrate with databases, troubleshoot production issues.");
            javaJob.setSkills("Java, Spring Boot, MySQL, REST APIs");
            javaJob.setQualifications("B.E/B.Tech in Computer Science");
            javaJob.setBenefits("Health insurance, flexible timing, learning stipend");
            javaJob.setRecruiter(recruiter);
            jobRepository.save(javaJob);

            Job frontendJob = new Job();
            frontendJob.setTitle("Frontend Developer");
            frontendJob.setCompany("TCS");
            frontendJob.setLocation("Hyderabad");
            frontendJob.setJobType("Full Time");
            frontendJob.setSalary("₹9 LPA");
            frontendJob.setExperience("1-3 years");
            frontendJob.setDescription("Create interactive user interfaces for enterprise products.");
            frontendJob.setResponsibilities("Build UI components and work closely with design team.");
            frontendJob.setSkills("React, JavaScript, CSS, UI Design");
            frontendJob.setQualifications("Bachelor's degree in IT or related field");
            frontendJob.setBenefits("Career growth, remote flexibility, bonus schemes");
            frontendJob.setRecruiter(recruiter);
            jobRepository.save(frontendJob);

            Job pythonJob = new Job();
            pythonJob.setTitle("Python Developer");
            pythonJob.setCompany("Wipro");
            pythonJob.setLocation("Chennai");
            pythonJob.setJobType("Full Time");
            pythonJob.setSalary("₹10 LPA");
            pythonJob.setExperience("2 years");
            pythonJob.setDescription("Work on automation and backend systems using Python.");
            pythonJob.setResponsibilities("Build scripts and maintain API integrations.");
            pythonJob.setSkills("Python, Django, PostgreSQL, APIs");
            pythonJob.setQualifications("Bachelor's degree in Computer Science");
            pythonJob.setBenefits("Paid training, performance bonus, work-life balance");
            pythonJob.setRecruiter(recruiter);
            jobRepository.save(pythonJob);

            Job internJob = new Job();
            internJob.setTitle("Software Intern");
            internJob.setCompany("Tech Solutions");
            internJob.setLocation("Remote");
            internJob.setJobType("Internship");
            internJob.setSalary("₹20,000/month");
            internJob.setExperience("Freshers welcome");
            internJob.setDescription("Internship focused on web application development and testing.");
            internJob.setResponsibilities("Learn, contribute, and deliver project modules under guidance.");
            internJob.setSkills("HTML, CSS, JavaScript, Git");
            internJob.setQualifications("Students pursuing graduation or recently graduated");
            internJob.setBenefits("Mentorship, certificate, PPO opportunity");
            internJob.setRecruiter(recruiter);
            jobRepository.save(internJob);
        }
    }
}
