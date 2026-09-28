import type { PullRequest, SubmitPrRequest } from "../../../src/types";

const BASE_URL = "http://localhost:8080/api";

async function handleResponse<T>(res: Response): Promise<T> {
  if (!res.ok) {
    const text = await res.text().catch(() => res.statusText);
    throw new Error(`Request failed (${res.status}): ${text}`);
  }
  return res.json() as Promise<T>;
}

export const api = {
  listPullRequests(): Promise<PullRequest[]> {
    return fetch(`${BASE_URL}/pull-requests`).then((r) => handleResponse(r));
  },

  getPullRequest(id: string): Promise<PullRequest> {
    return fetch(`${BASE_URL}/pull-requests/${id}`).then((r) => handleResponse(r));
  },

  submitPullRequest(payload: SubmitPrRequest): Promise<PullRequest> {
    return fetch(`${BASE_URL}/pull-requests`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    }).then((r) => handleResponse(r));
  },

  triggerReview(id: string): Promise<PullRequest> {
    return fetch(`${BASE_URL}/pull-requests/${id}/review`, {
      method: "POST",
    }).then((r) => handleResponse(r));
  },
};