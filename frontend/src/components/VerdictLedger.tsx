import type { ReviewVerdict } from "../types";
import { SeverityBadge, riskScoreColor } from "./Badge";

/**
 * The signature visual element of the dashboard. Deliberately styled like
 * an audit ledger / receipt rather than a generic "AI chat bubble" — this
 * is a governance tool, and the visual language should say "this is a
 * record you can trust and inspect," not "this is a chatbot reply."
 *
 * Every field shown here maps 1:1 to the ReviewVerdict Java class from
 * Step 3 — nothing here is invented on the frontend. That traceability is
 * the point: what the guardrail validated is exactly what gets displayed.
 */
export function VerdictLedger({ verdict }: { verdict: ReviewVerdict }) {
  return (
    <div style={styles.ledger}>
      <div style={styles.scoreRow}>
        <div>
          <div style={styles.scoreLabel}>Risk Score</div>
          <div style={{ ...styles.scoreValue, color: riskScoreColor(verdict.riskScore) }}>
            {verdict.riskScore}
            <span style={styles.scoreMax}>/10</span>
          </div>
        </div>
        <div style={styles.summaryBlock}>
          <div style={styles.scoreLabel}>Summary</div>
          <div style={styles.summaryText}>{verdict.summary}</div>
        </div>
      </div>

      <div style={styles.divider} />

      <div style={styles.section}>
        <div style={styles.sectionLabel}>
          Issues Found — {verdict.issues.length}
        </div>
        {verdict.issues.length === 0 ? (
          <div style={styles.emptyState}>No issues flagged.</div>
        ) : (
          <div style={styles.issueList}>
            {verdict.issues.map((issue, i) => (
              <div key={i} style={styles.issueRow}>
                <div style={styles.issueHeader}>
                  <SeverityBadge severity={issue.severity} />
                  {issue.violatedPolicy && (
                    <span style={styles.policyTag}>{issue.violatedPolicy}</span>
                  )}
                </div>
                <div style={styles.issueDescription}>{issue.description}</div>
                <div style={styles.fixRow}>
                  <span style={styles.fixLabel}>Suggested fix →</span>
                  <span style={styles.fixText}>{issue.suggestedFix}</span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      <div style={styles.divider} />

      <div style={styles.section}>
        <div style={styles.sectionLabel}>Evidence Used</div>
        <div style={styles.evidenceRow}>
          {verdict.retrievedContextUsed.length === 0 ? (
            <span style={styles.emptyState}>No retrieved context was cited.</span>
          ) : (
            verdict.retrievedContextUsed.map((file) => (
              <span key={file} style={styles.evidenceChip}>
                {file}
              </span>
            ))
          )}
        </div>
      </div>
    </div>
  );
}

const styles: Record<string, React.CSSProperties> = {
  ledger: {
    background: "var(--surface)",
    border: "1px solid var(--border)",
    borderRadius: "var(--radius)",
    padding: 20,
  },
  scoreRow: {
    display: "flex",
    gap: 32,
    alignItems: "flex-start",
  },
  scoreLabel: {
    fontFamily: "var(--font-mono)",
    fontSize: 11,
    fontWeight: 600,
    letterSpacing: 0.6,
    textTransform: "uppercase",
    color: "var(--text-dim)",
    marginBottom: 6,
  },
  scoreValue: {
    fontFamily: "var(--font-mono)",
    fontSize: 36,
    fontWeight: 700,
    lineHeight: 1,
  },
  scoreMax: {
    fontSize: 16,
    color: "var(--text-dim)",
    fontWeight: 500,
  },
  summaryBlock: {
    flex: 1,
    paddingTop: 2,
  },
  summaryText: {
    fontSize: 14,
    lineHeight: 1.5,
    color: "var(--text-primary)",
  },
  divider: {
    height: 1,
    background: "var(--border)",
    margin: "18px 0",
  },
  section: {},
  sectionLabel: {
    fontFamily: "var(--font-mono)",
    fontSize: 11,
    fontWeight: 600,
    letterSpacing: 0.6,
    textTransform: "uppercase",
    color: "var(--text-dim)",
    marginBottom: 12,
  },
  emptyState: {
    fontSize: 13,
    color: "var(--text-dim)",
    fontStyle: "italic",
  },
  issueList: {
    display: "flex",
    flexDirection: "column",
    gap: 12,
  },
  issueRow: {
    background: "var(--surface-raised)",
    border: "1px solid var(--border)",
    borderRadius: 4,
    padding: 12,
  },
  issueHeader: {
    display: "flex",
    gap: 8,
    alignItems: "center",
    marginBottom: 8,
  },
  policyTag: {
    fontFamily: "var(--font-mono)",
    fontSize: 11,
    color: "var(--accent)",
  },
  issueDescription: {
    fontSize: 13,
    color: "var(--text-primary)",
    lineHeight: 1.5,
    marginBottom: 8,
  },
  fixRow: {
    fontSize: 12.5,
    lineHeight: 1.5,
  },
  fixLabel: {
    color: "var(--text-dim)",
    marginRight: 6,
    fontFamily: "var(--font-mono)",
  },
  fixText: {
    color: "var(--text-secondary)",
  },
  evidenceRow: {
    display: "flex",
    flexWrap: "wrap",
    gap: 8,
  },
  evidenceChip: {
    fontFamily: "var(--font-mono)",
    fontSize: 12,
    padding: "5px 10px",
    background: "var(--accent-dim)",
    color: "var(--accent)",
    borderRadius: 4,
    border: "1px solid #5B8DEF44",
  },
};