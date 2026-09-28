package com.prguardian.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

/**
 * Represents a single PR submitted for AI review.
 * Stored as a MongoDB document — deliberately schema-flexible because
 * different PRs surface different shapes of issues (a security PR's verdict
 * looks nothing like a style-convention PR's verdict). Forcing this into a
 * rigid relational schema would mean constant migrations.
 */
@Document(collection = "pull_requests")
public class PullRequest {

    @Id
    private String id;

    private String repoName;
    private String prTitle;
    private String author;
    private String rawDiff;

    private List<String> changedFiles;

    private PrStatus status;

    private ReviewVerdict verdict; // null until the AI review completes

    private Instant submittedAt;
    private Instant reviewedAt;

    public enum PrStatus {
        PENDING_REVIEW,
        REVIEWED,
        FAILED
    }

    public PullRequest() {
    }

    public PullRequest(String repoName, String prTitle, String author,
                        String rawDiff, List<String> changedFiles) {
        this.repoName = repoName;
        this.prTitle = prTitle;
        this.author = author;
        this.rawDiff = rawDiff;
        this.changedFiles = changedFiles;
        this.status = PrStatus.PENDING_REVIEW;
        this.submittedAt = Instant.now();
    }

    // --- Getters and setters ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public PrStatus getStatus() {
        return status;
    }

    public void setStatus(PrStatus status) {
        this.status = status;
    }

    public ReviewVerdict getVerdict() {
        return verdict;
    }

    public void setVerdict(ReviewVerdict verdict) {
        this.verdict = verdict;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(Instant reviewedAt) {
        this.reviewedAt = reviewedAt;
    }
}