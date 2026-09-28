package com.prguardian.model;

import java.util.List;

/**
 * The GUARDRAIL contract.
 *
 * This is the whole point of the "guardrails" part of the project: we never
 * let the LLM hand back free-form prose as the final answer. We force it
 * into this exact shape. If the model's response can't be parsed into this
 * class, the review is marked FAILED rather than silently accepting garbage.
 *
 * This is what makes the system auditable — you can query "every PR with
 * riskScore > 7", alert on it, or block a merge automatically. You can't do
 * that with a paragraph of prose.
 */
public class ReviewVerdict {

    private int riskScore; // 0-10, forced numeric so it's sortable/alertable

    private List<Issue> issues;

    private String summary; // one or two sentences, human-readable

    private List<String> retrievedContextUsed; // which code chunks informed this verdict — transparency

    public static class Issue {
        private String severity;   // LOW / MEDIUM / HIGH / CRITICAL
        private String description;
        private String suggestedFix;
        private String violatedPolicy; // null if not a policy violation, just a code-quality note

        public Issue() {
        }

        public Issue(String severity, String description, String suggestedFix, String violatedPolicy) {
            this.severity = severity;
            this.description = description;
            this.suggestedFix = suggestedFix;
            this.violatedPolicy = violatedPolicy;
        }

        public String getSeverity() {
            return severity;
        }

        public void setSeverity(String severity) {
            this.severity = severity;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getSuggestedFix() {
            return suggestedFix;
        }

        public void setSuggestedFix(String suggestedFix) {
            this.suggestedFix = suggestedFix;
        }

        public String getViolatedPolicy() {
            return violatedPolicy;
        }

        public void setViolatedPolicy(String violatedPolicy) {
            this.violatedPolicy = violatedPolicy;
        }
    }

    public ReviewVerdict() {
    }

    public int getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
    }

    public List<Issue> getIssues() {
        return issues;
    }

    public void setIssues(List<Issue> issues) {
        this.issues = issues;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<String> getRetrievedContextUsed() {
        return retrievedContextUsed;
    }

    public void setRetrievedContextUsed(List<String> retrievedContextUsed) {
        this.retrievedContextUsed = retrievedContextUsed;
    }
}