// Mirrors com.prguardian.model.PullRequest and ReviewVerdict on the backend.
// Kept in lockstep with the Java model deliberately — the frontend should
// never guess at a shape the backend didn't actually promise.

export type PrStatus = "PENDING_REVIEW" | "REVIEWED" | "FAILED";

export type Severity = "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";

export interface Issue {
  severity: Severity;
  description: string;
  suggestedFix: string;
  violatedPolicy: string | null;
}

export interface ReviewVerdict {
  riskScore: number;
  issues: Issue[];
  summary: string;
  retrievedContextUsed: string[];
}

export interface PullRequest {
  id: string;
  repoName: string;
  prTitle: string;
  author: string;
  rawDiff: string;
  changedFiles: string[];
  status: PrStatus;
  verdict: ReviewVerdict | null;
  submittedAt: string;
  reviewedAt: string | null;
}

export interface SubmitPrRequest {
  repoName: string;
  prTitle: string;
  author: string;
  rawDiff: string;
  changedFiles: string[];
}