# AI 활동 코칭 추천 — 설계 문서

> 상태: **백엔드 + 프론트엔드 연동 완성 (빌드/유닛테스트 통과, 실 OpenAI 키로는 미검증).**
> [`ai-quiz-generation.md`](./ai-quiz-generation.md)(추억 퀴즈 자동생성)에 이은 두 번째 AI 기능.
> 공통 인프라(`client-openai` 모듈)를 그대로 재사용한다.

## 배경

루트 `CLAUDE.md`의 서비스 개요: "일상 분석 & AI 코칭 — 수면, 활동, 외출, 사회 활동을 분석해
생활습관/인지건강 관리 행동을 추천, **본인의 기존 기록과 비교**(다른 사람과 비교 아님)." 지금까지
홈 화면의 "AI 추천" 카드(부모 화면)와 부모 상태 알림 카드(자녀 화면)는 전부 하드코딩된 문구였다.
이 문서는 그걸 실제 활동 데이터 기반 AI 생성으로 바꾼 설계다.

퀴즈 기능과 달리 이 기능은 **AI가 답을 모르는 문제가 없다** — 오늘의 활동 수치(걸음 수, 수면,
외출, 사회 접촉)와 개인 목표는 이미 DB에 정확히 있으므로, 사람 개입 없이 한 번의 AI 호출로
완결된다(퀴즈처럼 2~3단계로 나눌 필요 없음).

## 설계

- **비교 기준**: 다른 사용자가 아니라 **본인의 개인 목표**(`sleepGoalMinutes`, `stepsGoal`,
  `outingGoal`, `socialGoal`) 대비 달성률(%). 이미 `GetTodaySummaryQuery`가 계산하던 것과 동일한
  로직(`(값 * 100) / 목표`, 100 상한)을 재사용.
- **한 번의 AI 호출로 두 가지 톤 + 액션 타입을 동시에 생성**:
  - `parentMessage` — 부모 본인에게 하는 2인칭 말투 (예: "오늘 오후 산책 어떠세요?")
  - `childMessage` — 자녀에게 부모 상태를 알리는 3인칭 말투 (예: "어머니가 오늘 아직 외출을 못
    하셨어요")
  - `actionType` — `WALK` / `CALL` / `QUIZ` / `NONE` 중 하나. 기존에 이미 만들어져 있던 정적
    리마인더 화면 3종(`WalkReminder`, `CallReminder`, `QuizReminder`)과 매칭되는 개념 — 4개
    퍼센트 중 가장 낮은 게 무엇인지에 따라 AI가 고름(걸음수 낮으면 WALK, 사회활동 낮으면 CALL 등).
- **하루 1회 생성 + 캐싱**: 홈 화면 로드마다 AI를 부르면 비용이 계속 나가므로, `(userId, statDate)`
  단위로 한 번 생성해서 저장하고, 같은 날 재요청 시 저장된 값을 그대로 반환한다(`DailyActivityRecommendationService`가
  먼저 캐시 조회 → 있으면 즉시 반환, 없으면 생성 후 저장).
- **오늘 활동 기록이 아예 없는 날**: AI를 호출하지 않고 무난한 기본 문구로 대체(`DEFAULT_PARENT_MESSAGE`
  / `DEFAULT_CHILD_MESSAGE`, `actionType = NONE`) — 빈 데이터로 AI에게 무리하게 추천을 짜내라고
  하지 않음.
- 요청자가 PARENT든 CHILD든 항상 같은 부모의 오늘자 데이터를 대상으로 하고(`principal.parentUserId()`,
  이 컨트롤러의 다른 엔드포인트와 동일 패턴), 응답에 `parentMessage`/`childMessage` 둘 다 담아
  프론트엔드가 자기 role에 맞는 걸 골라 쓰게 한다.

## 아키텍처

```
activity 모듈                                        client-openai 모듈 (재사용, 변경 없음)
┌────────────────────────────────────────┐
│ ActivityController                       │
│  GET /api/v1/activities/recommendation   │
│         │                                │
│         ▼                                │
│ GetDailyActivityRecommendationQuery       │
│ / DailyActivityRecommendationService      │
│  1. (userId, statDate) 캐시 조회          │
│     있으면 즉시 반환 ─────────────────┐  │
│  2. 없으면 오늘의 DailyActivityStat 조회 │  │
│     없으면 기본 문구 반환(AI 호출 안 함) │  │
│  3. 퍼센트 계산 → AI 생성 → 저장         │  │
│         │                                │  │
│         ▼                                │  │
│ ActivityRecommendationGeneratorPort       │  │
│  (outbound)                              │  │
│         │ 구현                            │  │
│         ▼                                │  │
│ OpenAiActivityCoach (adapter)             │──calls──▶ OpenAiClient.completeJson
│  - stat+퍼센트로 프롬프트 구성            │
│  - JSON 파싱 →                           │
│    GeneratedRecommendation                │
└────────────────────────────────────────┘
```

**새 도메인**: `DailyActivityRecommendation` (id, userId, statDate, parentMessage, childMessage,
actionType) — 새 테이블 `daily_activity_recommendation` (V11 마이그레이션, FK 없이
`(user_id, stat_date)` 인덱스만).

## API 계약

```
GET /api/v1/activities/recommendation
Authorization: Bearer <token>

→ {
    "data": {
      "id": "...", "userId": "...", "statDate": "2026-08-16",
      "parentMessage": "...", "childMessage": "...",
      "actionType": "WALK" | "CALL" | "QUIZ" | "NONE"
    },
    "error": null
  }
```

`actionLabel`(버튼 문구) 같은 별도 필드는 없음 — 프론트에서 `actionType`을 기존 리마인더 3종의
제목/라벨과 매핑해서 쓰면 됨(예: `WALK` → "산책 시작하기" / `/parent/reminders/walk`).

## 검증 절차 (API 키를 받으면)

`ai-quiz-generation.md`의 절차와 동일하게 `OPENAI_API_KEY`를 셸 프로필에 넣고 서버를 띄운 뒤:

```bash
TOKEN=$(curl -s -X POST localhost:8080/api/v1/auth/demo-login -H "Content-Type: application/json" -d '{"role":"PARENT"}' | python3 -c "import json,sys;print(json.load(sys.stdin)['data']['accessToken'])")

# 오늘 활동 기록 (걸음수/사회활동 낮게 넣어서 WALK 또는 CALL 유도)
curl -s -X POST localhost:8080/api/v1/activities -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d "{\"statDate\":\"$(date +%Y-%m-%d)\",\"sleepMinutes\":420,\"steps\":1500,\"outingCount\":1,\"socialContactCount\":0}"

# 추천 조회 (처음엔 AI 호출, 두 번째부터는 캐시된 값 그대로 — 응답 id가 동일해야 함)
curl -s localhost:8080/api/v1/activities/recommendation -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
curl -s localhost:8080/api/v1/activities/recommendation -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

확인할 것: (1) 메시지가 자연스러운 한국어이고 실제 걸음수/사회활동 수치를 반영하는지, (2)
`actionType`이 실제로 가장 낮은 지표와 맞는지, (3) 두 번째 호출의 `id`가 첫 번째와 동일한지(캐싱
확인, AI 재호출 안 됐는지).

## 프론트엔드 (완료)

- `src/api/activity.ts` — `getRecommendation()`
- `parent/Home.tsx`의 "AI 추천" 카드, `child/Home.tsx`의 "상태 알림" 카드가 실 데이터 사용
- `actionType` → 기존 리마인더 3종 라우팅/라벨 매핑 완료(`WALK`→`/parent/reminders/walk`,
  `CALL`→`/parent/reminders/call`, `QUIZ`→`/parent/reminders/quiz`, `NONE`→버튼 숨김)
- 데이터 로딩 전/실패 시엔 기존 정적 문구를 그대로 유지(깜빡임/에러 배너 없음)

## 다음 단계 (범위 밖)

- **리마인더 화면 3종 자체는 여전히 정적**: `WalkReminder`/`CallReminder`/`QuizReminder`의
  제목/설명 문구는 이번 범위에 포함 안 됨 — 홈 화면 카드만 실제 AI 생성으로 바뀜.
- **주간 트렌드 반영**: 지금은 오늘 하루 수치만 씀. "지난주보다 나아졌어요" 같은 비교는 다음 단계.
