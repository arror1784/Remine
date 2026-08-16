<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-08-17 | Updated: 2026-08-17 -->

# client-openai

## Purpose
Despite the root `CLAUDE.md` guidance that "AI/knowledge processing belongs in a dedicated Python service, not reimplemented in Kotlin," this module is a genuine direct client for **OpenAI's own Chat Completions API** (`https://api.openai.com/v1/chat/completions`) — it is not a proxy to the project's internal Python AI service. It is intentionally domain-agnostic: `OpenAiClient.completeJson(systemPrompt, userPrompt)` takes two prompt strings and hands back the raw `choices[0].message.content` string from a JSON-mode chat completion, with no knowledge of what schema the caller expects back. Callers own prompt construction and response parsing entirely. This is exactly the kind of vendor-isolation the root docs call for `client-*` modules to provide: if the backend ever needs to swap providers or route through the internal Python AI service instead, only this module's internals change.

## Key Files
| File | Description |
|------|-------------|
| `src/main/kotlin/com/remine/client/openai/OpenAiClient.kt` | `@Component` wrapping `RestTemplate` calls to OpenAI's Chat Completions endpoint in JSON mode |
| `src/main/kotlin/com/remine/client/openai/OpenAiClientException.kt` | Single `RuntimeException` subtype for all client failures (missing API key, HTTP error, empty response) |

## Subdirectories
None — both files live directly under `src/main/kotlin/com/remine/client/openai`.

## For AI Agents

### Working In This Directory
- **Isolation boundary**: `OpenAiClient` exposes exactly one public method, `completeJson(systemPrompt: String, userPrompt: String): String`. It has zero knowledge of any Remine domain concept (no `Memory`, `Quiz`, etc.) — callers in other modules must build their own prompts and parse the returned JSON string into their own domain types. Do not add domain-specific methods to this module (e.g. `generateQuizFromMemory(...)`); that logic belongs in the calling module's application layer, using this client as a dependency.
- **Vendor detail leakage**: nothing about the OpenAI wire format (`response_format`, `messages` array, model name) should leak past this module. If a caller needs something OpenAI-specific beyond `completeJson`, extend this client's public surface rather than reaching into `RestTemplate`/HTTP details from outside.
- **JSON mode caveat**: OpenAI's `response_format: json_object` requires the literal word "json" to appear somewhere in the prompts, or the API call fails — this constraint is documented in the method's KDoc; callers must satisfy it.
- **Config**: `openai.api-key` (required, no default — throws `OpenAiClientException` if blank) and `openai.model` (optional, defaults to `gpt-4o-mini`) are read via `@Value`. Per root `CLAUDE.md`, these should be injected via Vault (`${OPENAI_API_KEY}`-style env interpolation in `application.yml`), never hardcoded.
- A fresh `RestTemplate()` is constructed per-instance (not injected/shared) — if connection pooling, timeouts, or interceptors are ever needed, that's the place to add them.

### Testing Requirements
No test files exist under `src/test/kotlin` for this module yet.

### Common Patterns
- All failure modes (blank API key, HTTP error response, transport-level exception, empty/missing message content) are normalized into the single `OpenAiClientException`, always with a descriptive message and, where available, the original cause chained — callers only need to catch one exception type.
- Uses Kotlin `mapOf(...)` literals (serialized via Jackson) to build the request body rather than dedicated request DTO classes, since the request shape is fully internal to this client.

## Dependencies

### Internal
None — this module does not depend on any other Remine_backend module (no dependency on `common` or elsewhere in `build.gradle.kts`).

### External
- `spring-boot-starter-web` — provides `RestTemplate`, `HttpEntity`/`HttpHeaders`, and Spring's `RestClientException`/`RestClientResponseException` types
- `com.fasterxml.jackson.module:jackson-module-kotlin` — Kotlin-friendly JSON deserialization of the OpenAI response into `JsonNode`

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
