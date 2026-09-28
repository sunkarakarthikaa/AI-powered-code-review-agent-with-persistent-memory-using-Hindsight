package com.prguardian.llm;

import tools.jackson.databind.ObjectMapper;
import com.prguardian.model.ReviewVerdict;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * THE GUARDRAIL ENFORCEMENT POINT.
 *
 * Prompting the model to "only return JSON" is a request, not a contract.
 * Models sometimes wrap JSON in markdown fences, add a stray sentence
 * before it, use a severity value we didn't ask for, or return a risk
 * score outside 0-10. This class is the single place where we decide:
 * does this response earn the right to become a stored, trusted verdict?
 *
 * Two layers of defense here:
 *   1. STRUCTURAL — can we even parse this into the ReviewVerdict shape?
 *   2. SEMANTIC   — even if it parses, does it obey our own rules
 *      (score range, allowed severities)?
 *
 * If either layer fails, we throw InvalidVerdictException with the raw
 * response attached — callers can log/inspect it, but it never becomes a
 * trusted, stored ReviewVerdict. This is what "auditable" actually means
 * in practice, not just a buzzword.
 */
@Component
public class ReviewVerdictParser {

    private static final Set<String> ALLOWED_SEVERITIES = Set.of("LOW", "MEDIUM", "HIGH", "CRITICAL");

    private final ObjectMapper objectMapper;

    public ReviewVerdictParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ReviewVerdict parseAndValidate(String rawLlmResponse) {
        String cleaned = stripMarkdownFences(rawLlmResponse);

        ReviewVerdict verdict;
        try {
            verdict = objectMapper.readValue(cleaned, ReviewVerdict.class);
        } catch (Exception e) {
            throw new InvalidVerdictException(
                    "LLM response could not be parsed as valid JSON matching ReviewVerdict schema",
                    rawLlmResponse, e);
        }

        validate(verdict, rawLlmResponse);
        return verdict;
    }

    private void validate(ReviewVerdict verdict, String rawResponse) {
        if (verdict.getRiskScore() < 0 || verdict.getRiskScore() > 10) {
            throw new InvalidVerdictException(
                    "riskScore " + verdict.getRiskScore() + " is outside allowed range 0-10",
                    rawResponse);
        }

        if (verdict.getSummary() == null || verdict.getSummary().isBlank()) {
            throw new InvalidVerdictException("summary is missing or blank", rawResponse);
        }

        List<ReviewVerdict.Issue> issues = verdict.getIssues();
        if (issues != null) {
            for (ReviewVerdict.Issue issue : issues) {
                if (issue.getSeverity() == null || !ALLOWED_SEVERITIES.contains(issue.getSeverity())) {
                    throw new InvalidVerdictException(
                            "issue severity '" + issue.getSeverity() + "' is not one of " + ALLOWED_SEVERITIES,
                            rawResponse);
                }
                if (issue.getDescription() == null || issue.getDescription().isBlank()) {
                    throw new InvalidVerdictException("issue description is missing", rawResponse);
                }
            }
        }
    }

    /**
     * Models frequently wrap JSON in ```json ... ``` fences even when told
     * not to. Rather than fighting that with ever-more-forceful prompting,
     * we defensively strip fences before parsing. Prompting reduces how
     * often this happens; this handles it when prompting isn't enough —
     * defense in depth, not reliance on the model behaving perfectly.
     */
    private String stripMarkdownFences(String text) {
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```(json)?", "");
            if (trimmed.endsWith("```")) {
                trimmed = trimmed.substring(0, trimmed.length() - 3);
            }
        }
        return trimmed.trim();
    }
}