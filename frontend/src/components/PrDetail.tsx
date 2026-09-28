import type { PullRequest } from "../types";
import { StatusBadge } from "./Badge";
import { VerdictLedger } from "./VerdictLedger";

export function PrDetail({
  pr,
  onReview,
  reviewing,
}: {
  pr: PullRequest;
  onReview: () => void;
  reviewing: boolean;
}) {
  return (
    <div style={styles.container}>
      <div style={styles.header}>
        <div>
          <div style={styles.repoLabel}>{pr.repoName}</div>
          <h1 style={styles.title}>{pr.prTitle}</h1>
          <div style={styles.meta}>
            {pr.author} · {pr.changedFiles.join(", ")}
          </div>
        </div>
        <StatusBadge status={pr.status} />
      </div>

      <div style={styles.section}>
        <div style={styles.sectionLabel}>Diff</div>
        <pre style={styles.diff}>{pr.rawDiff}</pre>
      </div>

      {pr.status === "PENDING_REVIEW" && (
        <button onClick={onReview} disabled={reviewing} style={styles.reviewButton}>
          {reviewing ? "Running RAG + LLM pipeline…" : "Run AI review"}
        </button>
      )}

      {pr.status === "FAILED" && (
        <div style={styles.failedNote}>
          The AI response failed guardrail validation and was not stored as a
          trusted verdict. Check the backend logs for the raw model response,
          then retry.
          <button onClick={onReview} disabled={reviewing} style={styles.retryButton}>
            {reviewing ? "Retrying…" : "Retry review"}
          </button>
        </div>
      )}

      {pr.verdict && (
        <div style={styles.section}>
          <div style={styles.sectionLabel}>AI Verdict</div>
          <VerdictLedger verdict={pr.verdict} />
        </div>
      )}
    </div>
  );
}

const styles: Record<string, React.CSSProperties> = {
  container: {
    display: "flex",
    flexDirection: "column",
    gap: 20,
  },
  header: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "flex-start",
  },
  repoLabel: {
    fontFamily: "var(--font-mono)",
    fontSize: 12,
    color: "var(--text-dim)",
    marginBottom: 4,
  },
  title: {
    fontSize: 20,
    fontWeight: 600,
    margin: "0 0 6px 0",
    color: "var(--text-primary)",
  },
  meta: {
    fontSize: 13,
    color: "var(--text-secondary)",
  },
  section: {},
  sectionLabel: {
    fontFamily: "var(--font-mono)",
    fontSize: 11,
    fontWeight: 600,
    letterSpacing: 0.6,
    textTransform: "uppercase",
    color: "var(--text-dim)",
    marginBottom: 10,
  },
  diff: {
    background: "var(--surface)",
    border: "1px solid var(--border)",
    borderRadius: "var(--radius)",
    padding: 14,
    fontSize: 12.5,
    lineHeight: 1.6,
    color: "var(--text-primary)",
    overflowX: "auto",
    margin: 0,
  },
  reviewButton: {
    alignSelf: "flex-start",
    background: "var(--accent)",
    color: "#fff",
    border: "none",
    borderRadius: 4,
    padding: "10px 16px",
    fontSize: 13,
    fontWeight: 600,
    fontFamily: "var(--font-mono)",
  },
  failedNote: {
    background: "#E5484D14",
    border: "1px solid #E5484D44",
    borderRadius: "var(--radius)",
    padding: 14,
    fontSize: 13,
    color: "var(--text-secondary)",
    lineHeight: 1.6,
  },
  retryButton: {
    display: "block",
    marginTop: 10,
    background: "transparent",
    color: "var(--risk-high)",
    border: "1px solid var(--risk-high)",
    borderRadius: 4,
    padding: "7px 12px",
    fontSize: 12.5,
    fontWeight: 600,
  },
};