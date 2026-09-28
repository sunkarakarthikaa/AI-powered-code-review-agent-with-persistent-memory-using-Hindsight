import type { PullRequest } from "../types";
import { StatusBadge } from "./Badge";

export function PrList({
  prs,
  selectedId,
  onSelect,
}: {
  prs: PullRequest[];
  selectedId: string | null;
  onSelect: (id: string) => void;
}) {
  if (prs.length === 0) {
    return (
      <div style={styles.empty}>
        No pull requests submitted yet. Use the form above to submit one.
      </div>
    );
  }

  return (
    <div style={styles.list}>
      {prs.map((pr) => (
        <button
          key={pr.id}
          onClick={() => onSelect(pr.id)}
          style={{
            ...styles.item,
            background: pr.id === selectedId ? "var(--surface-raised)" : "transparent",
            borderColor: pr.id === selectedId ? "var(--accent)" : "var(--border)",
          }}
        >
          <div style={styles.itemHeader}>
            <span style={styles.repoName}>{pr.repoName}</span>
            <StatusBadge status={pr.status} />
          </div>
          <div style={styles.title}>{pr.prTitle}</div>
          <div style={styles.meta}>
            {pr.author} · {pr.changedFiles.length} file
            {pr.changedFiles.length !== 1 ? "s" : ""}
            {pr.verdict && (
              <span style={{ color: "var(--text-dim)" }}>
                {" "}
                · risk {pr.verdict.riskScore}/10
              </span>
            )}
          </div>
        </button>
      ))}
    </div>
  );
}

const styles: Record<string, React.CSSProperties> = {
  list: {
    display: "flex",
    flexDirection: "column",
    gap: 8,
  },
  empty: {
    padding: 20,
    fontSize: 13,
    color: "var(--text-dim)",
    lineHeight: 1.6,
  },
  item: {
    textAlign: "left",
    border: "1px solid var(--border)",
    borderRadius: "var(--radius)",
    padding: "10px 12px",
    color: "var(--text-primary)",
    transition: "background 0.12s ease, border-color 0.12s ease",
  },
  itemHeader: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: 6,
  },
  repoName: {
    fontFamily: "var(--font-mono)",
    fontSize: 11,
    color: "var(--text-dim)",
  },
  title: {
    fontSize: 13.5,
    fontWeight: 500,
    marginBottom: 4,
    overflow: "hidden",
    textOverflow: "ellipsis",
    whiteSpace: "nowrap",
  },
  meta: {
    fontSize: 12,
    color: "var(--text-secondary)",
  },
};