# AI 추억 퀴즈 자동생성 — 설계 문서

> 상태: **구조 설계 및 코드 스캐폴딩 완료, 실 API 키로 검증 전.** OpenAI API 키를 아직 발급받지 못해
> 실제 호출 테스트는 못한 상태다. 키를 받으면 "검증 절차" 섹션부터 이어서 진행하면 된다.

## 배경 / 왜 이 기능인가

Remine의 핵심 차별점은 "가족이 등록한 옛날 사진/추억을 바탕으로 AI가 개인화된 회상 질문·인지
퀴즈를 생성"하는 것이다(루트 `CLAUDE.md`의 서비스 개요 참고). 그런데 백엔드에 이미 퀴즈 저장용
CQRS 엔드포인트(`POST /api/v1/memories/{id}/quiz`)는 있었지만, 실제로 **문제를 만드는 주체가
없었다** — 호출하는 쪽이 이미 완성된 문제 목록을 넘겨줘야 하는 구조였다. 이 문서는 그 "문제를
실제로 생성하는" 부분을 어떻게 채워 넣을지에 대한 설계다.

## 결정된 사항 (사용자 확인 완료)

1. **아키텍처: Kotlin 백엔드에서 OpenAI를 직접 호출한다.** 별도 Python AI 서비스는 만들지 않는다.
   - 원래 루트 `CLAUDE.md`에는 "AI/지식 처리(검색, 임베딩, RAG, STT)는 별도 Python 서비스로 분리,
     Kotlin은 프록시만"이라고 적혀 있다. 이 규칙은 임베딩/RAG처럼 복잡한 AI 파이프라인을 염두에 둔
     것인데, 지금 필요한 건 "프롬프트를 보내고 JSON으로 답을 받는" 단순 LLM 호출 하나뿐이라 Python
     레이어를 두는 이점이 거의 없다는 판단으로, 사용자가 명시적으로 직접 호출을 선택했다.
   - **향후 실제로 임베딩/RAG/STT 같은 복잡한 AI 파이프라인이 필요해지면 그때 Python 서비스 분리를
     재검토**하면 된다. 지금 이 결정이 그 가능성을 막는 건 아니다.
2. **첫 기능: 추억 사진 → 퀴즈 자동생성.** 일상 데이터 기반 AI 코칭 추천(홈 화면의 "오늘 오후 산책
   어떠세요?" 류 메시지를 실제 AI가 생성하도록 하는 것)은 그다음 단계로 미룬다 — 지금은 전부 UI에
   하드코딩된 문구다.
3. **비용/키:** OpenAI 크레딧을 사용자가 직접 보유. 키는 `OPENAI_API_KEY` 환경변수로만 주입하고
   코드/설정 파일에 절대 하드코딩하지 않는다(루트 `CLAUDE.md`의 "Secrets/config via Vault
   injection" 규칙과 동일한 패턴, 지금은 Vault 대신 로컬 환경변수로 대체).

## 아키텍처

```
memory 모듈                              client-openai 모듈 (신규)
┌─────────────────────────────┐          ┌──────────────────────────┐
│ MemoryController             │          │ OpenAiClient              │
│  POST /memories/{id}/quiz/   │          │  fun completeJson(        │
│       generate  (신규)       │          │    systemPrompt, userPrompt│
│         │                    │          │  ): String                │
│         ▼                    │          │  - RestTemplate로 OpenAI  │
│ GenerateMemoryQuizCommand    │          │    Chat Completions API   │
│  (신규 inbound port)         │          │    직접 호출               │
│         │                    │          │  - response_format:       │
│         ▼                    │          │    json_object 모드 사용  │
│ GenerateMemoryQuizService     │          │  - 퀴즈/도메인 지식 전혀   │
│  1. photo 조회 + 소유권 검증  │          │    없음 (범용 래퍼)       │
│  2. MemoryQuizGeneratorPort   │──calls──▶│                           │
│     .generateQuestions(photo)│          └──────────────────────────┘
│  3. 결과를 MemoryQuizQuestion│
│     으로 저장 (기존 저장     │
│     로직과 동일 패턴)        │
│         │                    │
│         ▼                    │
│ MemoryQuizGeneratorPort       │
│  (신규 outbound port)         │
│         │ 구현                │
│         ▼                    │
│ OpenAiMemoryQuizGenerator     │
│  - photo.title/memoryLabel   │
│    으로 한국어 프롬프트 구성  │
│  - OpenAiClient 호출          │
│  - 응답 JSON 파싱             │
│    → List<GeneratedQuestion> │
└─────────────────────────────┘
```

**의존성 방향**: `memory` 모듈 → `client-openai` 모듈 (헥사고날 규칙대로, 도메인 모듈이 외부
연동 모듈에 의존하는 방향만 허용, 반대 방향 없음). `client-openai`는 어떤 도메인 모듈도 몰라야
한다 — 프롬프트 안의 "퀴즈" 관련 내용은 전부 `memory` 모듈 쪽 어댑터(`OpenAiMemoryQuizGenerator`)
책임이고, `client-openai`는 "프롬프트 넣으면 JSON 문자열 나온다"만 안다.

기존에 있던 수동 입력 플로우(`POST /memories/{id}/quiz`, `CreateMemoryQuizCommand`)는 건드리지
않는다 — 이번에 추가하는 자동생성은 그 옆에 나란히 놓이는 새 엔드포인트다(나중에 가족이 직접
문제를 손으로 써넣는 용도로 수동 입력이 계속 쓰일 수 있음).

## 새로 생기는 파일

### `client-openai` 모듈 (이 프로젝트의 첫 `client-*` 모듈)
- `settings.gradle.kts`에 모듈 추가
- `client-openai/build.gradle.kts` — `common` 모듈 구성을 참고, `spring-boot-starter-web`(RestTemplate
  용, WebFlux 불필요)만 최소로
- `client-openai/src/main/kotlin/com/remine/client/openai/OpenAiClient.kt`
  - `@Value("\${openai.api-key}")`, `@Value("\${openai.model:gpt-4o-mini}")`
  - `fun completeJson(systemPrompt: String, userPrompt: String): String`
  - `POST https://api.openai.com/v1/chat/completions`, `response_format: {"type": "json_object"}`
    (⚠️ OpenAI 요구사항: 프롬프트 어딘가에 "json"이라는 단어가 실제로 들어가야 이 모드가 허용됨)
  - 실패 시 상태코드/응답 본문을 포함한 명확한 예외를 던짐

### `memory` 모듈 추가분
- `application/port/outbound/MemoryQuizGeneratorPort.kt` — `generateQuestions(photo, count=3): List<GeneratedQuestion>`
- `adapter/infrastructure/ai/OpenAiMemoryQuizGenerator.kt` — 프롬프트 구성 + JSON 파싱, `OpenAiClient` 사용
- `application/port/inbound/GenerateMemoryQuizCommand.kt` — `In(memoryPhotoId, ownerUserId)` → `Out(questions: List<MemoryQuizQuestion>)`
- `application/service/GenerateMemoryQuizService.kt` — 소유권 검증 → AI 생성 → 저장 → photo 상태를 `QUIZ_ACTIVE`로 전환 (기존 `CreateMemoryQuizService`와 동일한 저장 패턴)
- `adapter/presentation/web/MemoryController.kt`에 `POST /api/v1/memories/{id}/quiz/generate` 추가 (요청 바디 없음)
- `memory/build.gradle.kts`에 `implementation(project(":client-openai"))` 추가

### 설정
- `app-api/build.gradle.kts`에 `implementation(project(":client-openai"))` 추가
- `app-api/src/main/resources/application.yml`에 추가 (기존 `google.oauth` 블록과 동일한 스타일):
  ```yaml
  openai:
    api-key: ${OPENAI_API_KEY:}
    model: ${OPENAI_MODEL:gpt-4o-mini}
  ```

## 프롬프트 설계

- 입력: `photo.title`, `photo.memoryLabel` (사진 URL은 이번 단계에서 쓰지 않음 — 아래 "다음 단계" 참고)
- 대상: 인지건강 관리가 필요한 고령 사용자(부모 역할) — 어렵거나 잡학상식스러운 문제가 아니라,
  자기 가족의 추억을 되짚어보는 따뜻하고 쉬운 회상 질문이어야 함
- 출력 형식(JSON 모드로 강제):
  ```json
  {
    "questions": [
      { "question": "...", "options": ["...", "...", "...", "..."], "correctOptionIndex": 0 }
    ]
  }
  ```
- 기본 문항 수: 3개 (호출부에서 `count` 파라미터로 조절 가능하게 설계됨)

## API 계약

```
POST /api/v1/memories/{id}/quiz/generate
Authorization: Bearer <token>
(요청 바디 없음)

응답 (기존 수동 입력 엔드포인트와 동일한 형태):
{
  "data": [
    { "id": "...", "memoryPhotoId": "...", "question": "...", "options": [...], "correctOptionIndex": 0, "sortOrder": 0, "createdAt": "...", "updatedAt": "..." }
  ],
  "error": null
}
```

## 검증 절차 (API 키를 받으면 여기부터)

1. `~/.zshrc`(또는 `~/.zprofile`)에 `export OPENAI_API_KEY=sk-...` 추가 후 새 터미널/세션에서 확인
   — Claude Code의 Bash 도구는 매 호출마다 셸 프로필을 새로 읽으므로, 여기 추가해두면 이후 실행되는
   모든 명령에 자동으로 적용됨. 채팅에 키를 직접 붙여넣을 필요 없음.
2. `cd Remine_backend && JAVA_HOME="/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home" ./gradlew build` — 빌드/유닛테스트 확인 (이 시점 테스트는 OpenAiClient를 스텁으로 대체해서 실제 네트워크 호출 없이 통과하도록 작성돼 있음)
3. `./gradlew :app-api:bootRun` 으로 서버 기동
4. curl로 실제 호출 검증:
   ```bash
   TOKEN=$(curl -s -X POST localhost:8080/api/v1/auth/demo-login -H "Content-Type: application/json" -d '{"role":"PARENT"}' | python3 -c "import json,sys;print(json.load(sys.stdin)['data']['accessToken'])")
   # 실제 memory photo id로 교체해서 호출
   curl -s -X POST localhost:8080/api/v1/memories/<PHOTO_ID>/quiz/generate -H "Authorization: Bearer $TOKEN"
   ```
5. 응답의 질문들이 실제로 한국어로, 그럴듯한 회상 질문 형태로 나오는지, 4지선다 + 정답 인덱스가
   올바른지 육안 확인
6. 문제 있으면 `OpenAiMemoryQuizGenerator`의 프롬프트 문구를 조정 (모델 자체는 `OPENAI_MODEL`
   환경변수로 바꿔볼 수 있음, 기본값 `gpt-4o-mini`)

## 다음 단계 (이번 범위 밖, 나중에 고려)

- **프론트엔드 연동**: 지금은 백엔드 엔드포인트만 있고 프론트에서 호출하는 버튼/화면이 없음.
  `parent/memories/Quiz.tsx` 또는 사진 업로드 직후 흐름에 "AI로 퀴즈 만들기" 버튼을 붙이면 됨.
- **사진 기반(vision) 생성**: 지금은 `title`/`memoryLabel` 텍스트만 사용. 실제 파일 스토리지가
  붙어서 `photoUrl`이 외부에서 접근 가능한 진짜 URL이 되면, gpt-4o 같은 비전 지원 모델에 사진을
  직접 보여주고 사진 속 내용 기반으로 질문을 만들게 할 수 있음 — 지금 단계에서는 사진 URL 접근성이
  보장 안 돼서 의도적으로 제외함.
- **AI 코칭 추천**: 홈 화면의 활동 추천 메시지도 실제 활동 데이터 기반으로 AI가 생성하게 하는 것 —
  아직 설계 안 함, 이 문서 범위 밖.
- **비용/레이트리밋**: 지금은 아무 제약 없이 호출당 바로 OpenAI를 때림. 사용량이 늘면 캐싱(같은
  사진에 대해 재생성 방지)이나 레이트리밋을 고려해야 함.
