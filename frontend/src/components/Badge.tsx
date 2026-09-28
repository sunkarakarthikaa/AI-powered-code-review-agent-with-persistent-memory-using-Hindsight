import type { PrStatus, Severity } from "../types";

const STATUS_STYLES: Record<PrStatus, { bg: string; fg: string; label: string }> = {
  PENDING_REVIEW: { bg: "#8B92A022", fg: "#8B92A0", label: "Pending Review" },
  REVIEWED: { bg: "#3DD68C22", fg: "#3DD68C", label: "Reviewed" },
  FAILED: { bg: "#E5484D22", fg: "#E5484D", label: "Guardrail Failed" },
};

const SEVERITY_STYLES: Record<Severity, { bg: string; fg: string }> = {
  LOW: { bg: "#3DD68C22", fg: "#3DD68C" },
  MEDIUM: { bg: "#F5A62322", fg: "#F5A623" },
  HIGH: { bg: "#E5484D22", fg: "#E5484D" },
  CRITICAL: { bg: "#FF5C7A22", fg: "#FF5C7A" },
};

const badgeStyle = (bg: string, fg: string): React.CSSProperties => ({
  display: "inline-flex",
  alignItems: "center",
  padding: "3px 9px",
  borderRadius: 4,
  fontFamily: "var(--font-mono)",
  fontSize: 11,
  fontWeight: 600,
  letterSpacing: 0.4,
  textTransform: "uppercase",
  background: bg,
  color: fg,
  whiteSpace: "nowrap",
});

export function StatusBadge({ status }: { status: PrStatus }) {
  const s = STATUS_STYLES[status];
  return <span style={badgeStyle(s.bg, s.fg)}>{s.label}</span>;
}

export function SeverityBadge({ severity }: { severity: Severity }) {
  const s = SEVERITY_STYLES[severity];
  return <span style={badgeStyle(s.bg, s.fg)}>{severity}</span>;
}

export function riskScoreColor(score: number): string {
  if (score >= 9) return "var(--risk-critical)";
  if (score >= 6) return "var(--risk-high)";
  if (score >= 3) return "var(--risk-medium)";
  return "var(--risk-low)";
}