# PR Guardian — AI Code Review Agent with Persistent Memory

PR Guardian is an AI-powered pull request review system that analyzes code changes, identifies potential issues, and provides structured review feedback.

The system uses **Hindsight as a persistent memory layer**, allowing PR Guardian to retain knowledge from previous pull-request reviews and recall relevant context when reviewing future pull requests.

Instead of treating every pull request as an isolated task, PR Guardian can build a history of review knowledge and use that history to provide more context-aware reviews.

---

## Problem Statement

Traditional AI code-review agents generally review each pull request independently.

This creates a limitation: the agent may identify an issue in one pull request but have no persistent knowledge of that review when a similar issue appears in a future pull request.

For example:

- A developer introduces a particular coding pattern in PR #1.
- PR Guardian identifies a problem and provides review feedback.
- A similar pattern appears in PR #2.
- A stateless review agent analyzes PR #2 from scratch.

PR Guardian addresses this limitation by introducing **persistent agent memory using Hindsight**.

---

## Solution

PR Guardian combines AI-powered code analysis with persistent memory.

The workflow is:

```text
Pull Request
     |
     v
PR Guardian
     |
     +--------------------+
     |                    |
     v                    v
Code Analysis       Hindsight Memory
     |                    |
     |              Recall relevant
     |              previous reviews
     |                    |
     +---------+----------+
               |
               v
       Context-aware Review
               |
               v
       Review Knowledge
          retained
        in Hindsight


## Files Excluded from GitHub

For security and repository cleanliness, certain local files and generated files are intentionally excluded using `.gitignore`.

These include:

- `.env` and other environment files containing secrets
- Build output such as `target/` and `frontend/dist/`
- Dependency folders such as `frontend/node_modules/`
- Local IDE configuration
- Logs
- Local MongoDB data
- Local Hindsight data
- Other runtime-generated files

The repository includes `.env.example` as a safe template for required environment variables.

> **Note:** API keys, passwords, database credentials, and other secrets are never committed to the repository.