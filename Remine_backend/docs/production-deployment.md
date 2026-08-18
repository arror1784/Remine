# 프로덕션 배포 런북 (Production Deployment Runbook)

> 상태: **백엔드 설정 명세 완료 (Spring Boot `prod` 프로필 기준).**
> 대상 모듈: `Remine_backend/app-api` (포트 8080)
> 설정 소스: `application.yml`, `application-prod.yml`

> ⚠️ **왜 이것이 중요한가 (Why This Matters)**
> 
> 로컬 개발 편의를 위해 `application.yml`에는 공개된 개발용 JWT 시크릿(`dev-only-secret-key-change-in-production-...`)이 기본값으로 포함되어 있습니다. 만약 프로덕션 환경에서 환경변수가 누락되었을 때 기본값으로 자동 폴백된다면, **저장소 접근 권한이 있는 누구나 임의의 사용자/역할(PARENT, CHILD)로 유효한 JWT를 위조**할 수 있는 심각한 보안 취약점이 발생합니다.
> 
> 이를 방지하기 위해 `application-prod.yml`은 `jwt.secret: ${JWT_SECRET}`으로 선언되어 기본값을 일체 허용하지 않습니다. `JWT_SECRET` 환경변수가 설정되지 않은 프로덕션 부팅은 **즉시 실패(Fail-Fast)**하도록 강제되었습니다(보안 감사 커밋 `6ba2007` 참조).

---

## 1. 개요

Remine 백엔드는 Kotlin / Spring Boot 기반의 헥사고날 아키텍처(Hexagonal Architecture) 멀티 모듈 프로젝트입니다.

프로덕션 실행 시에는 반드시 Spring Profile을 `prod`로 활성화해야 합니다(`--spring.profiles.active=prod` 또는 `SPRING_PROFILES_ACTIVE=prod`). `prod` 프로필 활성화 시:
- H2 로컬 파일 DB 대신 **PostgreSQL** 데이터소스가 활성화됩니다.
- H2 웹 콘솔(`/h2-console`)이 비활성화(`enabled: false`)됩니다.
- JPA DDL 자동 생성이 비활성화되고 검증(`ddl-auto: validate`) 모드로 동작하며, Flyway 마이그레이션(`db/migration`)이 적용됩니다.
- 필수 환경변수 누락 시 부팅 단계에서 실패합니다.

---

## 2. 프로덕션 필수 환경 변수 명세

모든 설정 값은 `app-api/src/main/resources/application.yml` 및 `application-prod.yml` 파일에 선언되어 있습니다.

### 환경 변수 요약표

| 환경 변수명 | 설정 경로 (YAML) | 필수 여부 | 프로덕션 기본값 | 설명 |
| :--- | :--- | :---: | :---: | :--- |
| `SPRING_PROFILES_ACTIVE` | (실행 인자/env) | **필수** | *없음 — 미지정 시 `dev` 프로필로 조용히 부팅* | `prod`로 지정해야 Postgres/Flyway/`JWT_SECRET` fail-fast가 활성화됨. 이 변수 자체는 YAML에 선언되지 않으므로 빠뜨려도 아무 에러 없이 H2로 부팅하는 것이 가장 위험한 실패 모드임 — Docker Compose 배포에서는 `docker-compose.prod.yml`이 이 값을 하드코딩해 원천 차단함 |
| `JWT_SECRET` | `jwt.secret` | **필수** | *없음 (부팅 실패)* | JWT 토큰 서명 및 검증용 비밀키 |
| `DB_URL` | `spring.datasource.url` | **필수** | *없음 (부팅 실패)* | PostgreSQL JDBC 연결 URL |
| `DB_USER` | `spring.datasource.username` | **필수** | *없음 (부팅 실패)* | PostgreSQL 데이터베이스 사용자 계정 |
| `DB_PASSWORD` | `spring.datasource.password` | **필수** | *없음 (부팅 실패)* | PostgreSQL 데이터베이스 비밀번호 |
| `REDIS_HOST` | `spring.data.redis.host` | **필수** | *없음 (부팅 실패)* | Redis 서버 호스트명 / IP |
| `REDIS_PORT` | `spring.data.redis.port` | 선택 | `6379` | Redis 서버 포트 |
| `STORAGE_UPLOAD_DIR` | `storage.upload-dir` | 선택 (권장) | `./uploads` | 추억 사진 업로드 파일이 저장되는 디스크 경로. 컨테이너 배포 시 named volume과 반드시 일치시켜야 재배포 후에도 파일이 유지됨 |
| `STORAGE_PUBLIC_BASE_URL` | `storage.public-base-url` | 선택 (강력 권장) | `http://localhost:8080` | 업로드된 사진의 절대 URL을 만들 때 쓰는 base — **이 값은 DB에 영구 저장**되므로, 기본값을 그대로 두면 `http://localhost:8080/uploads/...`가 행에 박혀 나중에 값을 바꿔도 기존 사진은 계속 깨져 보임. 프로덕션 도메인(`https://<domain>`)으로 반드시 지정 |
| `OPENAI_API_KEY` | `openai.api-key` | 선택 (권장) | `""` (빈 문자열) | OpenAI API 인증 키 |
| `OPENAI_MODEL` | `openai.model` | 선택 | `gpt-4o-mini` | OpenAI 모델 식별자 |
| `GOOGLE_OAUTH_CLIENT_ID` | `google.oauth.client-id` | 선택 (미연동) | `""` (빈 문자열) | Google OAuth 클라이언트 ID |
| `GOOGLE_OAUTH_CLIENT_SECRET` | `google.oauth.client-secret` | 선택 (미연동) | `""` (빈 문자열) | Google OAuth 클라이언트 시크릿 |
| `GOOGLE_OAUTH_REDIRECT_URI` | `google.oauth.redirect-uri` | 선택 (미연동) | `""` (빈 문자열) | Google OAuth 리다이렉트 URI |
| `CORS_ALLOWED_ORIGINS` | `cors.allowed-origins` | 선택 (권장) | `http://localhost:5173,http://localhost:5174` | API 호출을 허용할 브라우저 origin (콤마로 구분). 프로덕션 프론트엔드 도메인으로 반드시 교체 — 기본값은 로컬 개발용 Vite 포트라 프로덕션에서 그대로 두면 실제 프론트엔드의 API 호출이 CORS로 막힘. nginx가 프론트/백엔드를 같은 origin에서 서빙하는 배포(Docker Compose 구성)에서는 브라우저가 CORS 자체를 트리거하지 않아 기능상 영향은 없음 |
| `TZ` | (JVM 옵션 `-Duser.timezone`) | 선택 (강력 권장) | 컨테이너 기본값(UTC) | 체크리스트/일일 활동 로직이 `LocalDate.now()`(JVM 타임존 기준)로 "오늘"을 판정하므로, `Asia/Seoul`로 지정하지 않으면 KST 00:00~09:00 구간의 활동이 하루 어긋난 날짜로 저장됨 |

---

### 상세 항목 설명

#### (1) JWT 설정 (`jwt`)
- **`JWT_SECRET` (필수)**:
  - `application.yml`에서는 로컬 개발 편의를 위해 `dev-only-secret-key-change-in-production-0123456789`를 기본값으로 지정합니다.
  - `application-prod.yml`에서는 `jwt.secret: ${JWT_SECRET}`로 재정의하여 기본값을 제거했습니다. 프로덕션 환경변수에 `JWT_SECRET`이 주입되지 않으면 컨텍스트 로딩 시점에 즉시 부팅이 실패합니다.
- **`jwt.expiration-ms` (기본값 유지 가능)**:
  - `application.yml`에 `604800000`(7일, 밀리초 단위)로 정의되어 있습니다. 별도의 오버라이드가 필요하지 않다면 기본값을 그대로 사용해도 무방합니다.

#### (2) 데이터베이스 설정 (`spring.datasource`)
- **`DB_URL` / `DB_USER` / `DB_PASSWORD` (필수)**:
  - PostgreSQL 드라이버(`org.postgresql.Driver`)를 사용합니다.
  - 예시 `DB_URL`: `jdbc:postgresql://<db-host>:5432/<db-name>?sslmode=require`
  - 테이블 스키마는 애플리케이션 기동 시 Flyway(`V1` ~ `V12`)를 통해 자동 마이그레이션됩니다 (`ddl-auto: validate`).

#### (3) Redis 캐시 설정 (`spring.data.redis`)
- **`REDIS_HOST` (필수)**: 프로덕션에서는 기본값이 없으므로 Redis 엔드포인트 주소를 반드시 지정해야 합니다.
- **`REDIS_PORT` (선택)**: 미지정 시 기본 포트 `6379`가 적용됩니다.

#### (4) OpenAI AI 서비스 연동 (`openai`)
- **`OPENAI_API_KEY` (선택 / 기능 필수)**:
  - 추억 퀴즈 자동 생성(`POST /api/v1/memories/{id}/quiz/generate-questions`) 및 활동 코칭 추천(`GET /api/v1/activities/recommendation`)에 사용됩니다.
  - ⚠️ **주의사항 (알려진 결함)**: `OPENAI_API_KEY`가 설정되지 않거나 비어있는 경우, `GET /api/v1/activities/recommendation` 호출 시 `OpenAiClientException`이 발생하며 현재 `GlobalExceptionHandler`에 전용 예외 핸들러가 없어 **HTTP 500 (`INTERNAL_ERROR`)**이 반환됩니다(프론트엔드는 폴백 처리로 UI 에러를 숨김). 실 서비스 운영 시에는 반드시 유효한 키가 주입되어야 합니다.
- **`OPENAI_MODEL` (선택)**:
  - 미지정 시 `gpt-4o-mini`가 사용됩니다.

#### (5) Google OAuth 연동 (`google.oauth`)
- **`GOOGLE_OAUTH_CLIENT_ID` / `GOOGLE_OAUTH_CLIENT_SECRET` / `GOOGLE_OAUTH_REDIRECT_URI` (선택 / 미사용)**:
  - `GoogleOAuthProperties` 빈으로 바인딩되며, `User` 엔티티 및 DB 스키마에 `google_id` 컬럼과 인덱스가 마련되어 있습니다.
  - **현재 상태**: 설정 프로퍼티 및 데이터 모델만 준비된 상태(Config-only)이며, 실제 `/api/v1/auth/google/callback` 핸들러 및 소셜 로그인 리다이렉션 흐름은 아직 컨트롤러/서비스 계층에 구현되어 있지 않습니다. 빈 값으로 두어도 부팅에 영향을 주지 않습니다.

---

## 3. 배포 전 점검 체크리스트 (Pre-Deployment Checklist)

프로덕션 환경 배포 전 아래 체크리스트를 순서대로 확인하십시오.

- [ ] **Spring 프로필**: `SPRING_PROFILES_ACTIVE=prod` 또는 `--spring.profiles.active=prod` 지정 확인
- [ ] **JWT_SECRET**: 충분한 엔트로피(최소 256비트 이상)를 가진 비밀키 주입 확인 (미설정 시 부팅 불가)
- [ ] **PostgreSQL 접속 정보**:
  - [ ] `DB_URL` (PostgreSQL JDBC URL) 설정
  - [ ] `DB_USER` 설정
  - [ ] `DB_PASSWORD` 설정
- [ ] **Redis 접속 정보**:
  - [ ] `REDIS_HOST` 설정
  - [ ] `REDIS_PORT` 설정 (기본값 6379 또는 사용자 정의 포트)
- [ ] **OpenAI API**:
  - [ ] `OPENAI_API_KEY` 주입 확인 (활동 추천 500 에러 및 퀴즈 생성 실패 방지)
  - [ ] `OPENAI_MODEL` 확인 (미지정 시 `gpt-4o-mini`)
- [ ] **데모 로그인 엔드포인트 격리 상태 확인**: 실제 사용자 데이터베이스 환경인 경우 데모 로그인 차단 조치 여부 점검 (아래 4장 참고)

---

## 4. 보안 주의사항: 데모 로그인 엔드포인트 (`POST /api/v1/auth/demo-login`)

### 현황 및 리스크
`AuthController.kt`에 정의된 `POST /api/v1/auth/demo-login` 엔드포인트는 데모 시연 및 평가자의 무인증 테스트를 위한 편의 기능입니다:
- `SecurityConfig`에서 `/api/v1/auth/**` 경로가 `permitAll()`로 열려 있습니다.
- 자격 증명(패스워드, 소셜 인증 등) 없이 `{"role": "PARENT"}` 또는 `{"role": "CHILD"}` 요청 바디만 전달하면 Flyway 시드 계정(`V8__seed_demo_users.sql`)에 대한 **실제 유효한 7일짜리 JWT 토큰을 즉시 발급**합니다.

```
클라이언트 요청 (인증 없음) ──▶ POST /api/v1/auth/demo-login {"role":"PARENT"}
                          ──▶ 시드 부모 계정 JWT 토큰 즉시 발급 (7일 유효)
```

### 권장 조치사항 (오픈 아이템)
- 데모/스테이징 배포에서는 정상 동작하지만, **실제 사용자 데이터가 존재하는 상용 프로덕션 환경에 그대로 노출될 경우 명백한 인증 우회(Authentication Bypass) 지점**이 됩니다.
- 실제 상용 런칭 전 `AuthController` 클래스에 `@Profile("!prod")` 애노테이션을 부여하여 `prod` 환경에서는 해당 컨트롤러 빈 자체가 등록되지 않도록 격리해야 합니다.
- *참고: 현재 코드베이스에는 `@Profile("!prod")`가 아직 적용되어 있지 않으며, 문서화된 의도적 오픈 아이템 상태입니다.*

### 공개 도메인 배포 시 추가 고려사항 (Certificate Transparency 노출)

HTTPS를 위해 Let's Encrypt 등에서 발급받는 인증서는 **Certificate Transparency(CT) 로그에 공개 게시**됩니다. 즉 도메인에 인증서를 발급받는 순간, 그 서브도메인이 존재한다는 사실 자체가 전 세계에서 검색 가능해지고, 자동화된 취약점 스캐너가 `/api/v1/auth/demo-login`을 발견할 확률이 로컬/사설망 배포보다 크게 높아집니다. 팀 내부·평가자용 데모처럼 `demo-login`을 의도적으로 남겨둔 배포에서는, 코드 변경 없이 아래 중 하나를 병행하는 것을 권장합니다:

- nginx `auth_basic`으로 사이트 전체에 ID/PW 1개를 걸어 평가자에게만 공유
- 방화벽(ufw)에서 알려진 팀 IP 대역만 80/443 허용

---

## 5. 부팅 및 헬스체크 검증 절차

### 1) 프로덕션 빌드 및 실행

```bash
# 1. 환경변수 주입 (예시)
export SPRING_PROFILES_ACTIVE=prod
export JWT_SECRET="your-256-bit-production-secret-key-here"
export DB_URL="jdbc:postgresql://localhost:5432/remine"
export DB_USER="remine_user"
export DB_PASSWORD="remine_password"
export REDIS_HOST="localhost"
export REDIS_PORT="6379"
export OPENAI_API_KEY="sk-..."

# 2. 빌드 실행
cd Remine_backend
./gradlew clean :app-api:bootJar

# 3. JAR 실행
java -jar -Dspring.profiles.active=prod app-api/build/libs/app-api-0.0.1-SNAPSHOT.jar
```

### 2) 헬스체크 검증

Spring Boot Actuator의 헬스체크 엔드포인트는 `SecurityConfig`에 의해 `permitAll()`로 개방되어 있습니다:

```bash
# 헬스체크 확인
curl -s http://localhost:8080/actuator/health | python3 -m json.tool
```

정상 응답:
```json
{
  "status": "UP"
}
```

---

## 6. Docker Compose 기반 배포 (가비아 등 단일 VM)

이 문서의 4~5장은 `java -jar` 직접 실행을 기준으로 설명하지만, 실제 배포는 `docker-compose.prod.yml`(레포 루트)로 백엔드/프론트/PostgreSQL/Redis를 컨테이너 4개로 묶어 운영합니다. 전체 절차는 [`deploy/SERVER_SETUP.md`](../../deploy/SERVER_SETUP.md)를 참고하세요 — Docker/certbot 설치, DuckDNS+Let's Encrypt HTTPS 2단계 부트스트랩, GitHub Actions CI/CD(`​.github/workflows/deploy.yml`)까지 포함된 실행 절차입니다. 배포 계획의 근거(설계 결정, 대안 검토, 3라운드 리뷰 내역)는 [`.omc/plans/gabia-deploy-plan.md`](../../.omc/plans/gabia-deploy-plan.md)에 있습니다.

### DB 백업

named volume(`pgdata`)은 컨테이너 재생성으로부터 데이터를 보호하지만 디스크 자체 손상이나 실수로 인한 데이터 삭제까지 막아주진 않습니다. 최소한의 백업 크론 예시:

```bash
# /etc/cron.d/remine-pg-backup (매일 03:00 KST)
0 3 * * * root docker compose --env-file /opt/remine.env -f /opt/remine/docker-compose.prod.yml exec -T postgres pg_dump -U $POSTGRES_USER $POSTGRES_DB | gzip > /opt/remine-backups/$(date +\%F).sql.gz
```

### 장애 시 롤백

`docker-compose.prod.yml`은 배포마다 `git reset --hard`로 최신 커밋으로 고정되므로, 이미지 태그만 되돌리는 것으로는 충분하지 않을 수 있습니다(compose/nginx 설정 자체가 바뀐 경우). 인프라 정의와 이미지를 함께 되돌리세요:

```bash
cd /opt/remine
git checkout <이전-정상-커밋-sha> -- docker-compose.prod.yml deploy/
IMAGE_TAG=<이전-정상-커밋-sha> docker compose --env-file /opt/remine.env -f docker-compose.prod.yml up -d
```

---

## 7. 미해결 과제 및 오픈 아이템 (Known Open Items)

1. **`AuthController`의 `@Profile("!prod")` 프로필 격리**: 실 상용 릴리즈 전 데모 로그인 비활성화 처리.
2. **`GlobalExceptionHandler` 내 `OpenAiClientException` 매핑**: `OPENAI_API_KEY` 누락 또는 OpenAI 장애 시 500 에러 대신 기본 문구(`DEFAULT_PARENT_MESSAGE`) 반환 또는 적절한 에러 응답 처리.
3. **Google OAuth 실 흐름 완성**: `GoogleOAuthProperties` 설정을 실제 소셜 로그인 및 토큰 발급 핸들러(`/api/v1/auth/google/callback`)와 연결.
