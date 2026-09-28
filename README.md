PR Guardian Lite

An AI-native pull request review assistant that grounds LLM code review in your actual codebase — not just the diff.

Most AI PR reviewers look at a diff in isolation and hallucinate context. PR Guardian Lite fixes that by retrieving relevant code context from the repository before generating a review, then validating the LLM's output through a two-layer guardrail before it's ever shown to a developer.

How it works
Retrieval (RAG): Code changes are embedded and compared against a custom in-memory vector store using cosine similarity, pulling in the most relevant existing code as context — so the LLM reviews with knowledge of the surrounding system, not just the isolated diff.
Review generation: The retrieved context + PR diff are sent to an LLM (Anthropic Claude), prompted to return a structured verdict: risk score, issues found, and reasoning.
Guardrails: A two-layer validation system checks the LLM's output for structural correctness (valid JSON, expected fields) and semantic sanity (verdict consistency, score bounds) before it's persisted — so a malformed or nonsensical model response never reaches the dashboard.
Dashboard: A React + TypeScript frontend surfaces the verdict, risk score, and the actual retrieved evidence the model used to reach its conclusion — so the reasoning is auditable, not a black box.
Architecture
Backend: Spring Boot + MongoDB, with clean DTO/entity separation
Retrieval: Custom in-memory vector store with cosine similarity (a deliberate scale tradeoff over a dedicated vector DB, given the project's scope)
Frontend: React + TypeScript (Vite), dark "control room" aesthetic
LLM Integration: Anthropic API for review generation
Guardrails: Two-layer output validation (structural + semantic) before verdicts are persisted
Infra: Docker multi-stage builds with non-root users, orchestrated via Docker Compose
CI/CD: GitHub Actions pipeline that runs the tool's own self-review on its own pull requests
Why this design

The project treats reliability as a first-class concern rather than an afterthought — an LLM review pipeline is only trustworthy if you can guarantee its output is structurally valid and its reasoning is inspectable. The guardrail layer and evidence-surfacing dashboard exist specifically to make the AI's judgment auditable rather than opaque.
