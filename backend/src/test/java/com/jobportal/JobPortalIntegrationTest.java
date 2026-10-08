package com.jobportal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobportal.model.Job;
import com.jobportal.repository.ApplicationRepository;
import com.jobportal.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@SuppressWarnings("null")
class JobPortalIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void clearApplications() {
        applicationRepository.deleteAll();
    }

    @Test
    void publicJobListSerializesRecruiterMetadata() throws Exception {
        mockMvc.perform(get("/api/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").isNotEmpty())
                .andExpect(jsonPath("$[0].recruiterName").value("Recruiter User"));
    }

    @Test
    void recruiterCanPublishAndReadOwnJob() throws Exception {
        String jobBody = objectMapper.writeValueAsString(Map.of(
                "title", "QA Integration Engineer",
                "company", "Example Company",
                "location", "Remote",
                "jobType", "Full Time",
                "salary", "$100,000",
                "experience", "3 years",
                "description", "Create automated test coverage."
        ));

        mockMvc.perform(post("/api/jobs")
                        .with(user("recruiter@jobconnect.com").roles("RECRUITER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jobBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("QA Integration Engineer"))
                .andExpect(jsonPath("$.recruiterName").value("Recruiter User"));

        mockMvc.perform(get("/api/recruiter/jobs")
                        .with(user("recruiter@jobconnect.com").roles("RECRUITER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].title", hasItem("QA Integration Engineer")));
    }

    @Test
    void malformedBearerTokenReturnsUnauthorizedJson() throws Exception {
        mockMvc.perform(get("/api/jobs").header("Authorization", "Bearer not-a-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("401"));
    }

        @Test
        void invalidJobPayloadReturnsFieldValidationErrors() throws Exception {
                mockMvc.perform(post("/api/jobs")
                                                .with(user("recruiter@jobconnect.com").roles("RECRUITER"))
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("{}"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.errors.title").exists())
                                .andExpect(jsonPath("$.errors.description").exists());
        }

    @Test
    void seekerCanApplyReadApplicationAndRecruiterCanUpdateStatus() throws Exception {
        Job job = jobRepository.findAll().get(0);
        String applicationBody = objectMapper.writeValueAsString(java.util.Map.of(
                "jobId", job.getId(),
                "resume", "https://example.test/resume.pdf",
                "coverLetter", "Integration test application"
        ));

        String response = mockMvc.perform(post("/api/applications")
                        .with(user("seeker@jobconnect.com").roles("JOB_SEEKER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(applicationBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.job.title").value(job.getTitle()))
                .andExpect(jsonPath("$.applicantName").value("Job Seeker"))
                .andExpect(jsonPath("$.applicantEmail").value("seeker@jobconnect.com"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode created = objectMapper.readTree(response);
        long applicationId = created.get("id").asLong();

        mockMvc.perform(post("/api/applications")
                        .with(user("seeker@jobconnect.com").roles("JOB_SEEKER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(applicationBody))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/applications")
                        .with(user("seeker@jobconnect.com").roles("JOB_SEEKER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].job.title").value(job.getTitle()))
                .andExpect(jsonPath("$[0].applicantEmail").value("seeker@jobconnect.com"));

        mockMvc.perform(put("/api/applications/{id}/status", applicationId)
                        .param("status", "INTERVIEW")
                        .with(user("recruiter@jobconnect.com").roles("RECRUITER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INTERVIEW"));
    }

    @Test
    void anonymousUsersCannotCreateJobsOrRegisterAsAdmin() throws Exception {
        mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Test Admin","email":"test-admin@example.com","password":"test1234","role":"ROLE_ADMIN"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminUserResponseDoesNotContainPassword() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .with(user("admin@jobconnect.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].password").doesNotExist())
                .andExpect(jsonPath("$[*].email", hasItem("seeker@jobconnect.com")));
    }
}
