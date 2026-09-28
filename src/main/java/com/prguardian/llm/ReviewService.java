package com.prguardian.llm;

import com.prguardian.model.PullRequest;
import com.prguardian.model.ReviewVerdict;
import com.prguardian.repository.PullRequestRepository;
import com.prguardian.retrieval.CodeChunk;
import com.prguardian.retrieval.RetrievalService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Orchestrates the full review pipeline for a single PR:
 *
 *   1. RETRIEVE  — pull relevant existing code/policy for this diff (Step 2)
 *   2. PROMPT    — assemble diff + retrieved context + schema instructions
 *   3. CALL      — send to the LLM (Step 3a/3b)
 *   4. VALIDATE  — enforce the guardrail schema (Step 3c) — this can fail,
 *                  and that failure is a first-class outcome, not a bug
 *   5. PERSIST   — update the PR with the verdict (or FAILED status)
 *
 * This class is intentionally the only place that knows the full pipeline
 * order — everything it calls is a single-responsibility collaborator.
 */
@Service
public class ReviewService {

    private final PullRequestRepository pullRequestRepository;
    private final RetrievalService retrievalService;
    private final AnthropicClient anthropicClient;
    private final ReviewVerdictParser verdictParser;
    private final PromptBuilder promptBuilder = new PromptBuilder();

    public ReviewService(PullRequestRepository pullRequestRepository,
                          RetrievalService retrievalService,
                          AnthropicClient anthropicClient,
                          ReviewVerdictParser verdictParser) {
        this.pullRequestRepository = pullRequestRepository;
        this.retrievalService = retrievalService;
        this.anthropicClient = anthropicClient;
        this.verdictParser = verdictParser;
    }

    public PullRequest reviewPr(String prId) {
        PullRequest pr = pullRequestRepository.findById(prId)
                .orElseThrow(() -> new NoSuchElementException("No PR found with id " + prId));

        // Step 1: retrieve relevant context using the diff itself as the query.
        // Combining title + diff gives the embedding more signal than the diff alone.
        String retrievalQuery = pr.getPrTitle() + "\n" + pr.getRawDiff();
        List<CodeChunk> retrievedContext = retrievalService.retrieveRelevantContext(retrievalQuery);

        // Step 2: build the prompt from PR + retrieved context
        String systemPrompt = promptBuilder.buildSystemPrompt();
        String userMessage = promptBuilder.buildUserMessage(pr, retrievedContext);

        try {
            // Step 3: call the LLM
            String rawResponse = anthropicClient.call(systemPrompt, userMessage);

            // Step 4: guardrail — parse and validate, throws if the model misbehaved
            ReviewVerdict verdict = verdictParser.parseAndValidate(rawResponse);

            // Step 5a: success path
            pr.setVerdict(verdict);
            pr.setStatus(PullRequest.PrStatus.REVIEWED);
            pr.setReviewedAt(Instant.now());

        } catch (InvalidVerdictException e) {
            // Step 5b: failure path — we NEVER store an unvalidated verdict.
            // The PR is marked FAILED so a human can investigate; the raw
            // response is logged for debugging the prompt, not silently lost.
            System.err.println("[ReviewService] Guardrail rejected LLM response for PR "
                    + prId + ": " + e.getMessage());
            System.err.println("[ReviewService] Raw response was: " + e.getRawResponse());
            pr.setStatus(PullRequest.PrStatus.FAILED);
        }

        return pullRequestRepository.save(pr);
    }
}