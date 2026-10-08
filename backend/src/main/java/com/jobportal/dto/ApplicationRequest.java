package com.jobportal.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.lang.NonNull;

import java.util.Objects;

public class ApplicationRequest {
    @NotNull
    @Positive
    private Long jobId;

    @Size(max = 255)
    private String resume;

    @Size(max = 2000)
    private String coverLetter;

    public @NonNull Long getJobId() {
        return Objects.requireNonNull(jobId, "jobId is required");
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getResume() {
        return resume;
    }

    public void setResume(String resume) {
        this.resume = resume;
    }

    public String getCoverLetter() {
        return coverLetter;
    }

    public void setCoverLetter(String coverLetter) {
        this.coverLetter = coverLetter;
    }
}
