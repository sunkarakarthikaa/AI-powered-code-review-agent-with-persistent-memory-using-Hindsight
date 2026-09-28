package com.prguardian.llm;

import com.prguardian.model.PullRequest;
import com.prguardian.retrieval.CodeChunk;

import java.util.List;

/**
 * Builds the prompt sent to the LLM. Kept as its own class — separate from
 * ReviewService — because prompt wording is something you tune repeatedly
 * during development, and it should be reviewable/testable on its own
 * without dragging in HTTP clients or persistence.
 *
 * The prompt does three things deliberately:
 *   1. Gives the diff being reviewed.
 *   2. Gives the RETRIEVED context (this is the RAG payoff — the model
 *      sees the actual surrounding codebase/policy, not just the diff).
 *   3. Forces a strict JSON output contract matching ReviewVerdict — this
 *      is the guardrail. We are explicit and repetitive about "JSON only,
 *      no prose" because models drift toward conversational answers unless
 *      constrained hard.
 */
public class PromptBuilder {

    private static final String SYSTEM_PROMPT = """
            You are an automated code review agent operating inside a CI pipeline.
            You review pull requests using ONLY the diff and the retrieved context
            provided to you. You do not have access to the rest of the codebase.

            You MUST respond with ONLY a single valid JSON object — no markdown
            fences, no prose before or after, no explanation outside the JSON.
            If you cannot comply, still return the JSON with an empty issues list
            and a summary explaining why.

            The JSON MUST match this exact shape:
            {
              "riskScore": <integer 0-10>,
              "issues": [
                {
                  "severity": "LOW" | "MEDIUM" | "HIGH" | "CRITICAL",
                  "description": "<what's wrong>",
                  "suggestedFix": "<concrete suggestion>",
                  "violatedPolicy": "<policy name or null if not a policy violation>"
                }
              ],
              "summary": "<1-2 sentence human-readable summary>",
              "retrievedContextUsed": ["<file names you actually relied on>"]
            }

            Scoring guidance:
            - 0-2: trivial/no risk
            - 3-5: minor issues, non-blocking
            - 6-8: real risk, should be fixed before merge
            - 9-10: critical, must not merge (security, data loss, policy violation)
            """;

    public String buildSystemPrompt() {
        return SYSTEM_PROMPT;
    }

    public String buildUserMessage(PullRequest pr, List<CodeChunk> retrievedContext) {
        StringBuilder sb = new StringBuilder();

        sb.append("## Pull Request\n");
        sb.append("Repo: ").append(pr.getRepoName()).append("\n");
        sb.append("Title: ").append(pr.getPrTitle()).append("\n");
        sb.append("Author: ").append(pr.getAuthor()).append("\n");
        sb.append("Changed files: ").append(String.join(", ", pr.getChangedFiles())).append("\n\n");

        sb.append("## Diff\n```\n");
        sb.append(pr.getRawDiff());
        sb.append("\n```\n\n");

        sb.append("## Retrieved Context\n");
        sb.append("The following existing code and policy documents were retrieved as ");
        sb.append("relevant to this diff. Use them to judge the change — do not assume ");
        sb.append("anything about the codebase beyond what's shown here.\n\n");

        if (retrievedContext.isEmpty()) {
            sb.append("(No relevant context was retrieved.)\n");
        } else {
            for (CodeChunk chunk : retrievedContext) {
                sb.append("### ").append(chunk.getFilePath()).append("\n```\n");
                sb.append(chunk.getContent());
                sb.append("\n```\n\n");
            }
        }

        sb.append("Return your review as the JSON object described in the system prompt. ");
        sb.append("Nothing else.");

        return sb.toString();
    }
}