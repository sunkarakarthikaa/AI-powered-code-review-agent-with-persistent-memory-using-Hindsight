package com.prguardian.controller;

import com.prguardian.dto.SubmitPrRequest;
import com.prguardian.llm.ReviewService;
import com.prguardian.model.PullRequest;
import com.prguardian.repository.PullRequestRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Entry point of the whole system.
 * Submission (Step 1) and review (Step 3) are kept as separate endpoints
 * on purpose — submitting a PR should respond instantly (just a DB write),
 * while review involves an LLM call that can take a few seconds. Splitting
 * them means the API never makes a client wait on the slow path unless
 * they're explicitly asking for it. In a production system, review would
 * likely be triggered asynchronously (e.g. a queue) rather than by a
 * second explicit call — this synchronous version keeps the demo simple
 * and each stage independently inspectable.
 */
@RestController
@RequestMapping("/api/pull-requests")
public class PullRequestController {

    private final PullRequestRepository repository;
    private final ReviewService reviewService;

    public PullRequestController(PullRequestRepository repository, ReviewService reviewService) {
        this.repository = repository;
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<PullRequest> submitPr(@Valid @RequestBody SubmitPrRequest request) {
        PullRequest pr = new PullRequest(
                request.getRepoName(),
                request.getPrTitle(),
                request.getAuthor(),
                request.getRawDiff(),
                request.getChangedFiles()
        );
        PullRequest saved = repository.save(pr);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<PullRequest>> listAll() {
        return ResponseEntity.ok(repository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PullRequest> getById(@PathVariable String id) {
        Optional<PullRequest> pr = repository.findById(id);
        return pr.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/repo/{repoName}")
    public ResponseEntity<List<PullRequest>> listByRepo(@PathVariable String repoName) {
        return ResponseEntity.ok(repository.findByRepoNameOrderBySubmittedAtDesc(repoName));
    }

    /**
     * Triggers the full RAG + LLM review pipeline for an already-submitted PR.
     * Returns the PR with either a populated verdict (status=REVIEWED) or
     * status=FAILED if the guardrail rejected the model's response.
     */
    @PostMapping("/{id}/review")
    public ResponseEntity<PullRequest> reviewPr(@PathVariable String id) {
        PullRequest reviewed = reviewService.reviewPr(id);
        return ResponseEntity.ok(reviewed);
    }
}