import { useState } from "react";
import type { SubmitPrRequest } from "../types";

export function SubmitPrForm({
  onSubmit,
  submitting,
}: {
  onSubmit: (payload: SubmitPrRequest) => void;
  submitting: boolean;
}) {
  const [repoName, setRepoName] = useState("demo-service");
  const [prTitle, setPrTitle] = useState("");
  const [author, setAuthor] = useState("");
  const [changedFiles, setChangedFiles] = useState("");
  const [rawDiff, setRawDiff] = useState("");

  const canSubmit =
    repoName.trim() && prTitle.trim() && author.trim() && changedFiles.trim() && rawDiff.trim();

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!canSubmit) return;
    onSubmit({
      repoName: repoName.trim(),
      prTitle: prTitle.trim(),
      author: author.trim(),
      rawDiff,
      changedFiles: changedFiles.split(",").map((f) => f.trim()).filter(Boolean),
    });
  }

  return (
    <form onSubmit={handleSubmit} style={styles.form}>
      <div style={styles.label}>Submit a PR for review</div>
      <div style={styles.row}>
        <input
          style={styles.input}
          placeholder="Repo name"
          value={repoName}
          onChange={(e) => setRepoName(e.target.value)}
        />
        <input
          style={styles.input}
          placeholder="Author"
          value={author}
          onChange={(e) => setAuthor(e.target.value)}
        />
      </div>
      <input
        style={{ ...styles.input, width: "100%" }}
        placeholder="PR title"
        value={prTitle}
        onChange={(e) => setPrTitle(e.target.value)}
      />
      <input
        style={{ ...styles.input, width: "100%" }}
        placeholder="Changed files (comma-separated, e.g. UserService.java)"
        value={changedFiles}
        onChange={(e) => setChangedFiles(e.target.value)}
      />
      <textarea
        style={styles.textarea}
        placeholder={"Paste the diff here, e.g.\n- return userRepository.findById(id).orElse(null);\n+ userRepository.deleteById(id);"}
        value={rawDiff}
        onChange={(e) => setRawDiff(e.target.value)}
        rows={5}
      />
      <button type="submit" disabled={!canSubmit || submitting} style={styles.button}>
        {submitting ? "Submitting…" : "Submit for review"}
      </button>
    </form>
  );
}

const styles: Record<string, React.CSSProperties> = {
  form: {
    display: "flex",
    flexDirection: "column",
    gap: 8,
    padding: 16,
    background: "var(--surface)",
    border: "1px solid var(--border)",
    borderRadius: "var(--radius)",
  },
  label: {
    fontFamily: "var(--font-mono)",
    fontSize: 11,
    fontWeight: 600,
    letterSpacing: 0.6,
    textTransform: "uppercase",
    color: "var(--text-dim)",
    marginBottom: 2,
  },
  row: {
    display: "flex",
    gap: 8,
  },
  input: {
    flex: 1,
    background: "var(--surface-raised)",
    border: "1px solid var(--border)",
    borderRadius: 4,
    padding: "8px 10px",
    color: "var(--text-primary)",
    fontSize: 13,
    fontFamily: "var(--font-body)",
  },
  textarea: {
    background: "var(--surface-raised)",
    border: "1px solid var(--border)",
    borderRadius: 4,
    padding: "8px 10px",
    color: "var(--text-primary)",
    fontSize: 12.5,
    fontFamily: "var(--font-mono)",
    resize: "vertical",
  },
  button: {
    marginTop: 4,
    background: "var(--accent)",
    color: "#fff",
    border: "none",
    borderRadius: 4,
    padding: "9px 14px",
    fontSize: 13,
    fontWeight: 600,
  },
};