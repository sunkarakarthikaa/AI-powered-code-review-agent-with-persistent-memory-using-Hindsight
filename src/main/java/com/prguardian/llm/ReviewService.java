package com.prguardian.llm;

import com.prguardian.memory.HindsightMemoryClient;
import com.prguardian.model.PullRequest;
import com.prguardian.model.ReviewVerdict;
import com.prguardian.repository.PullRequestRepository;
import com.prguardian.retrieval.CodeChunk;
import com.prguardian.retrieval.RetrievalService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ReviewService {

    private final PullRequestRepository pullRequestRepository;
    private final RetrievalService retrievalService;
    private final AnthropicClient anthropicClient;
    private final ReviewVerdictParser verdictParser;
    private final HindsightMemoryClient hindsightMemoryClient;

    private final PromptBuilder promptBuilder = new PromptBuilder();

    public ReviewService(
            PullRequestRepository pullRequestRepository,
            RetrievalService retrievalService,
            AnthropicClient anthropicClient,
            ReviewVerdictParser verdictParser,
            HindsightMemoryClient hindsightMemoryClient) {

        this.pullRequestRepository = pullRequestRepository;
        this.retrievalService = retrievalService;
        this.anthropicClient = anthropicClient;
        this.verdictParser = verdictParser;
        this.hindsightMemoryClient = hindsightMemoryClient;
    }

    public PullRequest reviewPr(String prId) {

        PullRequest pr = pullRequestRepository.findById(prId)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "No PR found with id " + prId));

        /*
         * STEP 1: Retrieve relevant code and policy context.
         */
        String retrievalQuery =
                pr.getPrTitle() + "\n" + pr.getRawDiff();

        List<CodeChunk> retrievedContext =
                retrievalService.retrieveRelevantContext(retrievalQuery);

        /*
         * STEP 2: RECALL previous PR knowledge from Hindsight.
         *
         * This is what gives PR Guardian persistent memory.
         */
        String memoryQuery =
                "Previous reviews, decisions, coding patterns and security "
                + "lessons relevant to this pull request:\n"
                + pr.getPrTitle() + "\n"
                + pr.getRawDiff();

        List<String> previousMemories =
                hindsightMemoryClient.recall(memoryQuery);

        /*
         * STEP 3: Build the normal PR Guardian prompt.
         */
        String systemPrompt =
                promptBuilder.buildSystemPrompt();

        String userMessage =
                promptBuilder.buildUserMessage(
                        pr,
                        retrievedContext);

        /*
         * Inject Hindsight memories into the LLM context.
         */
        if (!previousMemories.isEmpty()) {

            String memoryContext =
                    "\n\n--- PREVIOUS PR GUARDIAN MEMORY ---\n"
                    + String.join("\n- ", previousMemories)
                    + "\n--- END PREVIOUS MEMORY ---\n"
                    + "\nUse these previous lessons when relevant. "
                    + "Do not blindly follow them if they conflict with "
                    + "the current code or policy.\n";

            userMessage = userMessage + memoryContext;

            System.out.println(
                    "[Hindsight] Injected "
                    + previousMemories.size()
                    + " memories into review prompt.");
        }

        try {

            /*
             * STEP 4: Call LLM.
             */
            String rawResponse =
                    anthropicClient.call(
                            systemPrompt,
                            userMessage);

            /*
             * STEP 5: Validate LLM response.
             */
            ReviewVerdict verdict =
                    verdictParser.parseAndValidate(rawResponse);

            /*
             * STEP 6: Save successful review.
             */
            pr.setVerdict(verdict);
            pr.setStatus(PullRequest.PrStatus.REVIEWED);
            pr.setReviewedAt(Instant.now());

            /*
             * STEP 7: RETAIN the new review in Hindsight.
             *
             * Future PRs can recall this knowledge.
             */
            String memory = buildReviewMemory(pr, verdict);

            hindsightMemoryClient.retain(
                    memory,
                    "pr-review-" + pr.getId());

        } catch (InvalidVerdictException e) {

            System.err.println(
                    "[ReviewService] Guardrail rejected LLM response for PR "
                            + prId + ": " + e.getMessage());

            System.err.println(
                    "[ReviewService] Raw response was: "
                            + e.getRawResponse());

            pr.setStatus(PullRequest.PrStatus.FAILED);
        }

        return pullRequestRepository.save(pr);
    }

    private String buildReviewMemory(
            PullRequest pr,
            ReviewVerdict verdict) {

        StringBuilder memory = new StringBuilder();

        memory.append("PR review learning.\n");
        memory.append("Repository: ")
                .append(pr.getRepoName())
                .append("\n");

        memory.append("Pull request: ")
                .append(pr.getPrTitle())
                .append("\n");

        memory.append("Changed files: ")
                .append(pr.getChangedFiles())
                .append("\n");

        memory.append("Review summary: ")
                .append(verdict.getSummary())
                .append("\n");

        memory.append("Risk score: ")
                .append(verdict.getRiskScore())
                .append("\n");

        if (verdict.getIssues() != null) {

            for (ReviewVerdict.Issue issue :
                    verdict.getIssues()) {

                memory.append("Issue severity: ")
                        .append(issue.getSeverity())
                        .append("\n");

                memory.append("Issue: ")
                        .append(issue.getDescription())
                        .append("\n");

                if (issue.getSuggestedFix() != null) {
                    memory.append("Suggested fix: ")
                            .append(issue.getSuggestedFix())
                            .append("\n");
                }

                if (issue.getViolatedPolicy() != null) {
                    memory.append("Violated policy: ")
                            .append(issue.getViolatedPolicy())
                            .append("\n");
                }
            }
        }

        return memory.toString();
    }
}