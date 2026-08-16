# AI 추억 퀴즈 자동생성 — 설계 문서

> 상태: **백엔드 완성 (빌드/유닛테스트 통과, 실 OpenAI 키로는 미검증). 프론트엔드 진행 중.**
> OpenAI API 키를 아직 발급받지 못해 실제 호출 테스트는 못한 상태다. 키를 받으면 "검증 절차"
> 섹션부터 이어서 진행하면 된다.
>
> ⚠️ 이 문서는 한 번 개정됐다 — 최초 설계(v1)는 AI가 질문+보기+정답을 한 번에 다 생성하는 방식이었는데,
> **AI가 가족의 실제 추억에 대한 진짜 정답을 알 수 없다는 근본적인 문제**가 있어서 폐기했다. 아래는
> 그 문제를 해결한 v2(현재 구현) 설계다.

## 배경 / 왜 이 기능인가

Remine의 핵심 차별점은 "가족이 등록한 옛날 사진/추억을 바탕으로 AI가 개인화된 회상 질문·인지
퀴즈를 생성"하는 것이다(루트 `CLAUDE.md`의 서비스 개요 참고).

## 결정된 사항

1. **아키텍처: Kotlin 백엔드에서 OpenAI를 직접 호출한다.** 별도 Python AI 서비스는 만들지 않는다.
   원래 루트 `CLAUDE.md`엔 "AI/지식 처리는 별도 Python 서비스, Kotlin은 프록시만"이라 적혀 있지만,
   임베딩/RAG처럼 복잡한 파이프라인이 아니라 "프롬프트 보내고 JSON 받기" 수준이라 Python 레이어의
   이점이 없다는 판단. 향후 실제 임베딩/RAG/STT가 필요해지면 그때 재검토.
2. **왜 2단계인가 (v1 → v2 개정 이유)**: AI에게 사진 제목/라벨만 주고 "질문+정답+오답을 전부
   만들어라"라고 하면, AI는 그 가족의 진짜 추억(누구랑 갔는지, 정확히 언제인지 등)을 알 방법이
   없어서 정답을 그냥 지어낼 수밖에 없다. 그래서 **정답은 반드시 실제로 그 추억을 아는 사람(자녀)이
   입력**해야 하고, AI의 역할은 (a) 질문을 던지는 것과 (b) 그 정답 주변에 그럴듯한 오답을 만드는 것
   두 가지로 나뉜다.
3. **비용/키:** OpenAI 크레딧을 사용자가 직접 보유. 키는 `OPENAI_API_KEY` 환경변수로만 주입,
   코드/설정 파일에 절대 하드코딩하지 않음.
4. 일상 데이터 기반 AI 코칭 추천(홈 화면 "오늘 오후 산책 어떠세요?" 류 메시지를 AI가 실제로 생성)은
   **이 퀴즈 생성 로직이 안정화된 뒤에 착수**하기로 함 — 지금은 전부 UI 하드코딩.

## 3단계 플로우

```
1단계 — 질문 생성 (AI 호출 #1)              2단계 — 정답 입력 (사람)         3단계 — 오답 생성 + 완성 (AI 호출 #2)
──────────────────────────────              ────────────────────            ──────────────────────────────────
자녀가 사진 업로드                                                          자녀가 각 질문에 실제 정답 입력
  title + memoryLabel
        │
        ▼
POST /memories/{id}/quiz/generate-questions  자녀 화면에 질문만 표시    →   POST /memories/{id}/quiz/complete-with-answers
  → AI: photo 정보로 질문 3개만 생성              (보기/정답 없음)              body: [{questionId, answer}, ...]
  → MemoryQuizDraftQuestion으로 저장                                          → AI: (질문, 정답) 쌍마다 그럴듯한
    (아직 options/correctOptionIndex 없음)                                      오답 3개씩 생성
        │                                                                     → 정답 + AI 오답을 섞어 최종
        ▼                                                                       MemoryQuizQuestion(4지선다)으로 저장
GET /memories/{id}/quiz/draft-questions                                       → 화면 새로고침해도 안 끊기게
  (새로고침 대비 재조회용)                                                       questionId로 draft ↔ 정답 매칭
```

같은 사진에 1단계를 다시 호출하면(재생성) 이전 draft 질문을 깨끗이 지우고 새로 저장한다 —
중복 누적 안 됨(v1에 있던 버그, v2 설계 시 같이 고침).

기존 수동 입력 플로우(`POST /memories/{id}/quiz`, `CreateMemoryQuizCommand`, 가족이 문제를 직접
손으로 써서 통째로 넘기는 방식)는 그대로 유지 — 이번 자동생성 기능과는 별개로, 나란히 존재한다.

## 아키텍처

```
memory 모듈                                          client-openai 모듈
┌───────────────────────────────────────┐            ┌──────────────────────────┐
│ MemoryController                        │            │ OpenAiClient              │
│  POST .../quiz/generate-questions       │            │  completeJson(            │
│  GET  .../quiz/draft-questions          │            │    systemPrompt, userPrompt│
│  POST .../quiz/complete-with-answers    │            │  ): String                │
│         │              │                │            │  - JSON 모드, 도메인 지식  │
│         ▼              ▼                │            │    전혀 없는 범용 래퍼     │
│ GenerateMemoryQuizQuestions   Complete   │──calls────▶│                           │
│ Command/Service               MemoryQuiz │            └──────────────────────────┘
│         │              WithAnswers       │
│         ▼              Command/Service   │
│ MemoryQuizGeneratorPort (outbound)        │
│  - generateDraftQuestions(photo, count)   │
│  - generateDistractors(question+answer 쌍)│
│         │ 구현                            │
│         ▼                                │
│ OpenAiMemoryQuizGenerator                 │
│  (두 종류 프롬프트: 질문 생성용 / 오답 생성용)│
└───────────────────────────────────────┘
```

**새 도메인**: `MemoryQuizDraftQuestion` (id, memoryPhotoId, question, sortOrder — options/정답
없이 질문 텍스트만 들고 있는, 확정 전 임시 상태) — 별도 테이블 `memory_quiz_draft_question`
(V10 마이그레이션, FK 없이 memory_photo_id 인덱스만, 다른 테이블과 동일 컨벤션).

**의존성 방향**: `memory` → `client-openai`만 허용 (반대 없음). `client-openai`는 퀴즈/오답 같은
도메인 개념을 전혀 모르고 "프롬프트 넣으면 JSON 나온다"만 안다 — 프롬프트 내용/파싱은 전부
`OpenAiMemoryQuizGenerator`(memory 모듈) 책임.

## API 계약 (최종, 검증됨)

```
POST /api/v1/memories/{id}/quiz/generate-questions
(요청 바디 없음)
→ [{ id, memoryPhotoId, question, sortOrder, createdAt, updatedAt }, ...]   # 옵션/정답 없음

GET /api/v1/memories/{id}/quiz/draft-questions
→ 위와 동일한 형태 (새로고침 시 재조회용)

POST /api/v1/memories/{id}/quiz/complete-with-answers
body: { "answers": [{ "questionId": "...", "answer": "..." }, ...] }
→ [{ id, memoryPhotoId, question, options: [...4개], correctOptionIndex, sortOrder }, ...]
```

(전부 `Authorization: Bearer <token>` 필요, `{ data, error }` 래퍼로 응답)

## 프롬프트 설계

- **1단계(질문 생성)** 입력: `photo.title`, `photo.memoryLabel`. 사진 URL은 아직 안 씀(아래 "다음
  단계" 참고). 대상은 고령 사용자 — 어렵거나 잡학상식스러운 문제가 아니라 자기 가족 추억을
  되짚어보는 따뜻하고 쉬운 회상 질문("이 사진은 언제쯤 찍은 걸까요?" 류). 기본 3문항.
- **3단계(오답 생성)** 입력: 1단계 질문 + 자녀가 입력한 실제 정답. 각 문항마다 정답과 헷갈릴 만한
  그럴듯한 오답 3개를 만들게 함. 정답+오답 4개를 섞어 `correctOptionIndex` 계산.

## 새로 생기는/바뀐 파일 (v2 기준)

### `client-openai` 모듈 — 변경 없음 (v1과 동일, `OpenAiClient.completeJson` 그대로 재사용)

### `memory` 모듈
- `domain/MemoryQuizDraftQuestion.kt` — 신규
- `application/port/outbound/MemoryQuizGeneratorPort.kt` — `generateDraftQuestions(photo, count)`, `generateDistractors(question+answer 목록)` 두 메서드로 개정
- `adapter/infrastructure/ai/OpenAiMemoryQuizGenerator.kt` — 두 프롬프트 처리하도록 개정
- `application/port/inbound/GenerateMemoryQuizQuestionsCommand.kt` — 신규 (1단계)
- `application/port/inbound/CompleteMemoryQuizWithAnswersCommand.kt` — 신규 (3단계)
- `application/port/inbound/GetMemoryQuizDraftQuestionsQuery.kt` — 신규 (재조회용)
- `application/service/{GenerateMemoryQuizQuestionsService, CompleteMemoryQuizWithAnswersService, GetMemoryQuizDraftQuestionsService}.kt` — 신규
- `adapter/infrastructure/jpa/MemoryQuizDraftQuestion{JpaEntity,JpaRepository,RepositoryAdapter}.kt` — 신규
- `adapter/presentation/web/MemoryController.kt` — 3개 엔드포인트 추가 (위 API 계약 참고)
- (v1에서 만들었던 `GenerateMemoryQuizCommand`/`GenerateMemoryQuizService`는 이번에 삭제됨 — 위 3개로 대체)

### 마이그레이션
- `migration/.../V10__create_memory_quiz_draft_question_table.sql`

### 설정 — v1과 동일, 변경 없음
```yaml
openai:
  api-key: ${OPENAI_API_KEY:}
  model: ${OPENAI_MODEL:gpt-4o-mini}
```

## 검증 절차 (API 키를 받으면 여기부터)

1. `~/.zshrc`(또는 `~/.zprofile`)에 `export OPENAI_API_KEY=sk-...` 추가 — Claude Code의 Bash
   도구는 매 호출마다 셸 프로필을 새로 읽으므로 이후 실행되는 모든 명령에 자동 적용됨.
2. `cd Remine_backend && JAVA_HOME="/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home" ./gradlew build`
3. `./gradlew :app-api:bootRun`
4. curl로 3단계 전체 체인 검증:
   ```bash
   TOKEN=$(curl -s -X POST localhost:8080/api/v1/auth/demo-login -H "Content-Type: application/json" -d '{"role":"CHILD"}' | python3 -c "import json,sys;print(json.load(sys.stdin)['data']['accessToken'])")

   # 사진 업로드 (photoUrl은 아무 문자열이나 가능, 실제 파일 스토리지 아직 없음)
   PHOTO_ID=$(curl -s -X POST localhost:8080/api/v1/memories -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{"title":"속초 여행","photoUrl":"https://example.com/x.jpg","memoryLabel":"2022년 여름"}' | python3 -c "import json,sys;print(json.load(sys.stdin)['data']['id'])")

   # 1단계: 질문 생성
   curl -s -X POST localhost:8080/api/v1/memories/$PHOTO_ID/quiz/generate-questions -H "Authorization: Bearer $TOKEN" | python3 -m json.tool

   # (여기서 나온 각 질문의 id를 받아서, 실제 답을 입력했다고 가정)

   # 3단계: 정답 제출 → 최종 퀴즈 완성
   curl -s -X POST localhost:8080/api/v1/memories/$PHOTO_ID/quiz/complete-with-answers -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{"answers":[{"questionId":"<위에서 받은 id>","answer":"속초"}]}' | python3 -m json.tool
   ```
5. 1단계 응답의 질문이 자연스러운 한국어 회상 질문인지, 3단계 응답의 오답들이 정답과 그럴듯하게
   헷갈리는지, `correctOptionIndex`가 실제 정답 위치를 정확히 가리키는지 확인.
6. 같은 사진에 1단계를 두 번 호출해서 draft 질문이 누적 안 되고 교체되는지도 확인.

## 프론트엔드 (진행 중)

- `src/api/memory.ts` — `uploadPhoto`, `generateQuizQuestions`, `getDraftQuestions`, `completeQuizWithAnswers`
- `src/pages/child/memories/AddPhoto.tsx` — 실 업로드 연결, 사진은 아직 실 파일 스토리지가 없어서
  로컬 샘플 이미지 중 선택하는 방식(임시), 업로드 직후 1단계(질문 생성) 자동 호출
- `src/pages/child/memories/AnswerQuiz.tsx` (신규, `/child/memories/:photoId/answer-quiz`) — 2단계
  정답 입력 화면 → 제출 시 3단계 호출
- **디자인 미확정** — 이 플로우는 아직 디자이너에게 전달되지 않아서, 기존 컴포넌트/스타일 패턴만
  재사용한 임시 UI로 구현 중. 실제 디자인 나오면 별도로 다시 다듬어야 함.

## 다음 단계 (이번 범위 밖)

- **사진 기반(vision) 생성**: 지금은 `title`/`memoryLabel` 텍스트만 사용. 실 파일 스토리지가 붙어서
  `photoUrl`이 진짜 접근 가능한 URL이 되면, gpt-4o 같은 비전 모델에 사진을 직접 보여주고 질문을
  만들게 할 수 있음.
- **AI 코칭 추천**: 홈 화면 활동 추천 메시지의 AI 생성 — 퀴즈 로직 안정화 후 착수하기로 사용자와
  합의했고, 실제로 착수함. 설계는 [`ai-activity-coaching.md`](./ai-activity-coaching.md) 참고.
- **비용/레이트리밋**: 아직 호출 제약 없음. 사용량 늘면 고려.
- **실 파일 스토리지**: `photoUrl`을 받기만 하고 실제 업로드/저장 인프라(S3 등)는 아직 없음.
