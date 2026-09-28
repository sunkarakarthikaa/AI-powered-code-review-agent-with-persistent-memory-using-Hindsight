package com.prguardian.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * What the client sends us. Kept separate from the PullRequest entity
 * on purpose: the entity has fields (status, verdict, timestamps) that
 * the client should never set directly. Mixing "what comes in from the
 * wire" with "what we store" is a common source of bugs (and a security
 * smell — mass-assignment vulnerabilities) — so DTO and entity stay split.
 */
public class SubmitPrRequest {

    @NotBlank(message = "repoName is required")
    private String repoName;

    @NotBlank(message = "prTitle is required")
    private String prTitle;

    @NotBlank(message = "author is required")
    private String author;

    @NotBlank(message = "rawDiff is required")
    private String rawDiff;

    @NotEmpty(message = "changedFiles must include at least one file")
    private List<String> changedFiles;

    public SubmitPrRequest() {
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getPrTitle() {
        return prTitle;
    }

    public void setPrTitle(String prTitle) {
        this.prTitle = prTitle;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getRawDiff() {
        return rawDiff;
    }

    public void setRawDiff(String rawDiff) {
        this.rawDiff = rawDiff;
    }

    public List<String> getChangedFiles() {
        return changedFiles;
    }

    public void setChangedFiles(List<String> changedFiles) {
        this.changedFiles = changedFiles;
    }
}