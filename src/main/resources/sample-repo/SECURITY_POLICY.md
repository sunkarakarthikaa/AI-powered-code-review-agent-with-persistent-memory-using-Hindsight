# Security Policy (excerpt used for retrieval demo)

## Section 3: Destructive Operations

All operations that delete or irreversibly modify user data MUST call
`AuditLogger.log(action, actor, targetId)` before executing the operation.

This applies to:
- User deletion
- Account deactivation
- Payment record removal

## Section 5: Secrets

API keys, tokens, and credentials must never be hardcoded in source files.
Always read secrets from environment variables or a secrets manager.

## Section 7: Null Handling

Service-layer methods that look up a single entity by ID follow the
existing convention of returning `null` when not found, matching
`UserService.findById`. New services should stay consistent with this
convention rather than mixing `Optional` and `null` return styles in the
same codebase.