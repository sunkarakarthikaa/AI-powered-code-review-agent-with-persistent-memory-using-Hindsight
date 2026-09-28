import { useEffect, useState, useCallback } from "react";
import type { PullRequest, SubmitPrRequest } from "./types";
import { api } from "./api/client";
import { PrList } from "./components/PrList";
import { SubmitPrForm } from "./components/SubmitPrForm";
import { PrDetail } from "./components/PrDetail";

export default function App() {
  const [prs, setPrs] = useState<PullRequest[]>([]);
  const [selectedId, setSelectedId] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [reviewing, setReviewing] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  const refresh = useCallback(async () => {
    try {
      const data = await api.listPullRequests();
      setPrs(data);
      setError(null);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Failed to load pull requests");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    refresh();
  }, [refresh]);

  const selected = prs.find((p) => p.id === selectedId) ?? null;

  async function handleSubmit(payload: SubmitPrRequest) {
    setSubmitting(true);
    try {
      const created = await api.submitPullRequest(payload);
      setPrs((prev) => [created, ...prev]);
      setSelectedId(created.id);
      setError(null);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Failed to submit PR");
    } finally {
      setSubmitting(false);
    }
  }

  async function handleReview() {
    if (!selected) return;
    setReviewing(true);
    try {
      const reviewed = await api.triggerReview(selected.id);
      setPrs((prev) => prev.map((p) => (p.id === reviewed.id ? reviewed : p)));
      setError(null);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Review failed");
    } finally {
      setReviewing(false);
    }
  }

  return (
    <div style={styles.app}>
      <aside style={styles.sidebar}>
        <div style={styles.brand}>
          <div style={styles.brandMark}>PR Guardian</div>
          <div style={styles.brandSub}>AI review governance</div>
        </div>

        <SubmitPrForm onSubmit={handleSubmit} submitting={submitting} />

        <div style={styles.listHeader}>
          Pull Requests {loading ? "" : `(${prs.length})`}
        </div>
        <div style={styles.listScroll}>
          <PrList prs={prs} selectedId={selectedId} onSelect={setSelectedId} />
        </div>
      </aside>

      <main style={styles.main}>
        {error && <div style={styles.errorBanner}>{error}</div>}

        {!selected ? (
          <div style={styles.placeholder}>
            {loading
              ? "Loading…"
              : "Select a pull request, or submit a new one, to see its review."}
          </div>
        ) : (
          <PrDetail pr={selected} onReview={handleReview} reviewing={reviewing} />
        )}
      </main>
    </div>
  );
}

const styles: Record<string, React.CSSProperties> = {
  app: {
    display: "flex",
    height: "100vh",
  },
  sidebar: {
    width: 380,
    flexShrink: 0,
    borderRight: "1px solid var(--border)",
    display: "flex",
    flexDirection: "column",
    padding: 16,
    gap: 16,
    overflow: "hidden",
  },
  brand: {
    padding: "4px 4px 8px 4px",
  },
  brandMark: {
    fontFamily: "var(--font-mono)",
    fontSize: 16,
    fontWeight: 700,
    letterSpacing: -0.3,
  },
  brandSub: {
    fontSize: 12,
    color: "var(--text-dim)",
    marginTop: 2,
  },
  listHeader: {
    fontFamily: "var(--font-mono)",
    fontSize: 11,
    fontWeight: 600,
    letterSpacing: 0.6,
    textTransform: "uppercase",
    color: "var(--text-dim)",
  },
  listScroll: {
    flex: 1,
    overflowY: "auto",
  },
  main: {
    flex: 1,
    overflowY: "auto",
    padding: 32,
  },
  placeholder: {
    color: "var(--text-dim)",
    fontSize: 14,
    marginTop: 60,
    textAlign: "center",
  },
  errorBanner: {
    background: "#E5484D14",
    border: "1px solid #E5484D44",
    borderRadius: 6,
    padding: "10px 14px",
    fontSize: 13,
    color: "var(--risk-high)",
    marginBottom: 20,
  },
};