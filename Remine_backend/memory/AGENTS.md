<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-08-17 | Updated: 2026-08-17 -->

# memory

## Purpose
Owns family-registered "memory photos" (old photos + a title/label, uploaded by a child on behalf of a parent) and the AI-generated reminiscence quizzes built from them. Quiz generation is a 3-step flow proxied to a Python-backed OpenAI client rather than implemented in Kotlin: (1) `generate-questions` — AI writes pure reminiscence question text from the photo's title/label, no options; (2) the parent supplies real free-text answers off a `MemoryQuizDraftQuestion`; (3) `complete-with-answers` — AI generates 3 plausible wrong-answer distractors per question, and the service assembles/shuffles the final 4-choice `MemoryQuizQuestion` set. A manual quiz-authoring path (`POST /{id}/quiz`) also exists, bypassing AI entirely for custom family-written quizzes. `GetTodayQuizQuery` additionally picks one not-yet-attempted-today `QUIZ_ACTIVE` photo per owner, using a date-hashed deterministic pick so repeated calls on the same day return the same photo.

## Key Files
| File | Description |
|------|-------------|
| `src/main/kotlin/com/remine/memory/domain/MemoryPhoto.kt` | Core entity: owner (parent), uploader (child), title, photoUrl, memoryLabel, `MemoryPhotoStatus` |
| `src/main/kotlin/com/remine/memory/domain/MemoryPhotoStatus.kt` | `PENDING` (no quiz yet) / `QUIZ_ACTIVE` (final quiz exists) |
| `src/main/kotlin/com/remine/memory/domain/MemoryQuizDraftQuestion.kt` | Step-1 AI output: question text only, no options |
| `src/main/kotlin/com/remine/memory/domain/MemoryQuizQuestion.kt` | Final 4-choice quiz question with `options` + `correctOptionIndex` |
| `src/main/kotlin/com/remine/memory/domain/MemoryQuizAttempt.kt` | A respondent's completed attempt (correctCount/totalCount) |
| `src/main/kotlin/com/remine/memory/application/port/outbound/MemoryQuizGeneratorPort.kt` | AI-proxy port: `generateDraftQuestions`, `generateDistractors` — the Kotlin-side contract the AI adapter implements |
| `src/main/kotlin/com/remine/memory/adapter/infrastructure/ai/OpenAiMemoryQuizGenerator.kt` | Implements `MemoryQuizGeneratorPort` via `client-openai`'s `OpenAiClient.completeJson`; Korean system/user prompts, strict JSON-schema parsing with `InvalidRequestException` on malformed AI output |
| `src/main/kotlin/com/remine/memory/application/service/GenerateMemoryQuizQuestionsService.kt` | Step 1 orchestration; clears prior drafts before regenerating |
| `src/main/kotlin/com/remine/memory/application/service/CompleteMemoryQuizWithAnswersService.kt` | Step 3 orchestration: builds Q&A pairs from drafts+answers, requests distractors, shuffles final options, flips photo status to `QUIZ_ACTIVE` |
| `src/main/kotlin/com/remine/memory/application/service/GetTodayQuizService.kt` | Deterministic daily-pick logic (see Purpose) |
| `src/main/kotlin/com/remine/memory/application/service/MemoryPhotoOwnership.kt` | Shared `requireOwnedByCaller` authorization check reused across services |
| `src/main/kotlin/com/remine/memory/adapter/presentation/web/MemoryController.kt` | REST controller for all memory/quiz endpoints |
| `src/main/kotlin/com/remine/memory/adapter/infrastructure/jpa/StringListJsonConverter.kt` | `AttributeConverter` storing `List<String>` (quiz options) as a JSON column |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `src/main/kotlin/com/remine/memory/domain` | Entities, `MemoryPhotoStatus`, `QuestionView` read-model; no framework deps |
| `src/main/kotlin/com/remine/memory/application/port/inbound` | CQRS Command/Query interfaces (9 total) with `In`/`Out` |
| `src/main/kotlin/com/remine/memory/application/port/outbound` | Repository ports + `MemoryQuizGeneratorPort` (AI proxy port) |
| `src/main/kotlin/com/remine/memory/application/service` | One service class per inbound port, plus shared `MemoryPhotoOwnership` helper |
| `src/main/kotlin/com/remine/memory/adapter/presentation/web` | `MemoryController` plus request/response DTOs |
| `src/main/kotlin/com/remine/memory/adapter/infrastructure/ai` | `OpenAiMemoryQuizGenerator` — the only AI-proxy adapter in this module |
| `src/main/kotlin/com/remine/memory/adapter/infrastructure/jpa` | JPA entities/repositories/adapters, plus `StringListJsonConverter` |

## For AI Agents

### Working In This Directory
- Every command/query keyed by `memoryPhotoId` takes an explicit `ownerUserId` and calls `requireOwnedByCaller(photo, ownerUserId)` — this throws `ForbiddenException` and is the module's only authorization gate. Controllers always pass `principal.parentUserId()` as `ownerUserId`, since memories belong to the parent regardless of which family member is acting.
- Do not implement AI logic (prompt engineering, JSON parsing of model output) outside `adapter/infrastructure/ai/OpenAiMemoryQuizGenerator.kt` — it is the single boundary against `client-openai`, consistent with the "AI/knowledge processing lives in its own Python service, Kotlin only proxies" rule (here the proxy target is `client-openai`, an internal Gradle module wrapping the actual OpenAI/Python integration).
- `OpenAiMemoryQuizGenerator` prompts are JSON-mode, in Korean, and hardcode the literal word "json" per OpenAI's JSON-mode requirement (see the code comment) — preserve that if editing prompts.
- Regenerating draft or final questions always deletes the prior set first (`deleteAllByMemoryPhotoId`) to prevent duplicate accumulation — follow that pattern for any new regeneration flow.
- The controller has no `PATCH` or `DELETE` endpoints at all — every write is a `POST`. If adding an update-in-place endpoint (e.g., editing a photo's caption), follow the root convention and use `PATCH`, not `POST`.

### Testing Requirements
- `src/test/kotlin/com/remine/memory/application/service/MemoryServicesTest.kt` (487 lines) — broad service-layer coverage across multiple services.
- `src/test/kotlin/com/remine/memory/application/service/CompleteMemoryQuizWithAnswersServiceTest.kt` (256 lines) and `GenerateMemoryQuizQuestionsServiceTest.kt` (152 lines) — the two AI-orchestration steps, including malformed/short-distractor error paths.
- `src/test/kotlin/com/remine/memory/adapter/infrastructure/ai/OpenAiMemoryQuizGeneratorTest.kt` (165 lines) — AI adapter's JSON parsing and prompt construction.
- `src/test/kotlin/com/remine/memory/adapter/infrastructure/jpa/StringListJsonConverterTest.kt` (36 lines) — JSON column converter round-trip.
- `src/test/kotlin/com/remine/memory/adapter/presentation/web/MemoryControllerTest.kt` (299 lines) — full controller/endpoint coverage.

### Common Patterns
- One `@Service` per inbound port (unlike `family`, which uses a single service for all 5 ports) — each service implements exactly one Command/Query interface.
- AI adapter throws `InvalidRequestException` (Korean user-facing messages) on any malformed/incomplete AI JSON response rather than silently degrading — callers should expect this to propagate as a client-visible error.
- Final quiz option ordering is randomized per generation (`(listOf(answer) + distractors).shuffled()`), with `correctOptionIndex` derived after shuffling — never assume a fixed position for the correct answer.

## Dependencies

### Internal
- `common` (via `api`) — base entities, `ApiResponse`, shared exceptions
- `auth` — `RemineUserPrincipal`
- `client-openai` — the AI proxy client (`OpenAiClient.completeJson`) used exclusively by `OpenAiMemoryQuizGenerator`

### External
- `spring-boot-starter-data-jpa` — persistence
- `spring-boot-starter-web` — REST controllers
- `spring-boot-starter-validation` — `@Valid` request DTOs
- `spring-boot-starter-security` — `@AuthenticationPrincipal`
- `com.h2database:h2` (test only) — in-memory DB for tests

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
