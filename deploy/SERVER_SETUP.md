# 가비아 서버 초기 설정 가이드

`.omc/plans/gabia-deploy-plan.md`의 Step 0/6/7을 그대로 옮긴 실행 절차입니다. 순서대로 진행하세요.

## 0. 서버 스펙 실측

```bash
ssh <server> "free -h && nproc && df -h"
docker pull hello-world   # (Docker 설치 후) 레지스트리 대역폭 사전 확인
```

- **RAM < 4GB**: 아래 절차 그대로(GHCR 이미지 pull 방식) 진행하고, 아래 "스왑 설정"을 반드시 수행합니다.
- **RAM ≥ 4GB**: `docker-compose.prod.yml`을 서버에서 직접 빌드하는 방식으로 단순화할 수 있습니다(이 문서는 GHCR 방식을 기준으로 작성됨 — 직접 빌드로 전환 시 `.github/workflows/deploy.yml`의 build-and-push job을 생략하고 deploy job에서 `docker compose build`를 실행하도록 바꾸세요).

## 1. Docker 설치

클린 Ubuntu에는 `docker-compose-plugin` 패키지가 기본 저장소에 없습니다. 공식 설치 스크립트를 사용하세요.

```bash
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER
# 재로그인 (또는 newgrp docker) 필요 — 그룹 반영은 새 세션부터 적용됩니다.
```

## 2. certbot 설치

```bash
sudo apt-get update && sudo apt-get install -y certbot
```

## 3. 방화벽 (ufw)

```bash
sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw enable
```

> **경고**: Docker는 published port를 iptables `DOCKER-USER` 체인에 직접 기록하여 **ufw 규칙을 우회합니다.** `docker-compose.prod.yml`의 `postgres`/`redis` 서비스에는 절대 `ports:`를 추가하지 마세요 — 이번 배포는 `demo-login` 무인증 JWT 발급 엔드포인트를 그대로 두므로, DB가 인터넷에 노출되면 사실상 전체가 노출됩니다.

## 4. DuckDNS

1. https://www.duckdns.org 에서 계정 생성, 서브도메인 발급 (예: `remine-demo`)
2. A 레코드를 가비아에서 할당받은 공인 IP로 설정
3. 이 문서 전체에서 `<domain>`은 `remine-demo.duckdns.org` 형태로 치환

## 5. 레포 클론 + git 인증

레포가 private이면 비대화형 `git fetch`가 동작하도록 read-only deploy key를 먼저 등록하세요 (GitHub repo → Settings → Deploy keys → Add).

```bash
sudo mkdir -p /opt/remine && sudo chown $USER:$USER /opt/remine
git clone git@github.com:<org>/Remine.git /opt/remine
cd /opt/remine
```

## 6. 시크릿 배치

```bash
cp .env.example /opt/remine.env
# /opt/remine.env를 채워 넣으세요: JWT_SECRET, DB_USER/PASSWORD, OPENAI_API_KEY,
# STORAGE_PUBLIC_BASE_URL=https://<domain>, CORS_ALLOWED_ORIGINS=https://<domain>,
# GHCR_OWNER=<org 또는 github 계정명>
chmod 600 /opt/remine.env
```

**`.env`는 레포 클론 디렉터리 밖(`/opt/remine.env`)에 둡니다** — `docker-compose.prod.yml`은 `app-api.env_file`을 `/opt/remine.env`(절대경로)로 참조합니다. 매 배포마다 `/opt/remine`이 `git reset --hard`로 초기화되므로, 클론 디렉터리 안에 두면 지워집니다.

동일한 값을 GitHub Secrets에도 백업해 두세요 — 서버가 사라지면 `/opt/remine.env`가 유일한 시크릿 원본이 되어버립니다.

## 7. GHCR 접근 (레포가 private인 경우 기본값)

이미지에 시크릿은 없지만("prod" 프로필은 전부 런타임 env로 주입됨), 소스 레포가 private인데 GHCR 이미지를 public으로 돌리면 컴파일된 Kotlin 클래스가 그대로 공개됩니다. **레포가 private이면 GHCR 패키지도 private으로 유지**하고, 서버에서 1회 로그인하세요:

```bash
echo "<read-only PAT with read:packages>" | docker login ghcr.io -u <github-username> --password-stdin
```

(레포가 public인 경우에만 GHCR 패키지도 public으로 전환해 이 스텝을 생략할 수 있습니다.)

## 8. 첫 배포 순서

GHCR 패키지는 최초 push 시 기본 private으로 생성됩니다. 첫 워크플로 실행 시 build job 직후 deploy job이 곧바로 pull을 시도하면 401로 실패할 수 있으므로:

1. `main`에 최초 push → build-and-push job만 성공 확인
2. Step 7의 `docker login`을 서버에서 미리 수행해 뒀다면 그대로 deploy job도 통과합니다. (PAT 없이 public으로 전환하는 경우 GitHub 웹 UI → Packages → 각 패키지 → Package settings → Change visibility)
3. 이후 push부터는 전체 워크플로우가 문제없이 통과합니다.

## 9. 스왑 설정 (RAM < 4GB인 경우 필수)

```bash
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
```

## 10. 최초 인증서 발급 (2단계 부트스트랩)

```bash
cd /opt/remine
mkdir -p certbot-webroot

# 1단계: HTTP 전용 conf로 frontend만 기동 (app-api 없이도 안전)
cp deploy/nginx/nginx.http-only.conf deploy/nginx/active.conf
GHCR_OWNER=<org> docker compose --env-file /opt/remine.env -f docker-compose.prod.yml up -d frontend

# 인증서 발급
sudo certbot certonly --webroot -w /opt/remine/certbot-webroot -d <domain>

# 2단계: HTTPS conf로 교체 후 전체 스택 기동
cp deploy/nginx/nginx.https.conf deploy/nginx/active.conf
# nginx.https.conf 안의 <domain> 플레이스홀더를 실제 도메인으로 치환하세요:
sed -i "s/<domain>/<실제-도메인>/g" deploy/nginx/active.conf
GHCR_OWNER=<org> docker compose --env-file /opt/remine.env -f docker-compose.prod.yml up -d
```

> **주의**: `active.conf`는 항상 `cp`로 생성하세요. 심볼릭 링크로 만들면 Docker가 컨테이너 기동 시점에 링크 대상을 해석해 바인드하므로, 이후 링크만 바꿔도 `nginx -s reload`가 옛 설정을 계속 읽습니다.

## 11. 인증서 자동 갱신

apt가 설치한 `certbot.timer`(기본 활성화) 하나만 사용하고 별도 cron은 추가하지 마세요(이중 실행 시 Let's Encrypt 레이트 리밋 소진 위험).

```bash
sudo mkdir -p /etc/letsencrypt/renewal-hooks/deploy
sudo tee /etc/letsencrypt/renewal-hooks/deploy/00-reload-nginx.sh > /dev/null <<'EOF'
#!/bin/sh
docker compose -f /opt/remine/docker-compose.prod.yml exec -T frontend nginx -s reload
EOF
sudo chmod +x /etc/letsencrypt/renewal-hooks/deploy/00-reload-nginx.sh

# 검증 (--run-deploy-hooks 없이는 훅 자체가 검증되지 않음)
sudo certbot renew --dry-run --run-deploy-hooks
```

## 12. 검증

`.omc/plans/gabia-deploy-plan.md`의 Verification Steps 0~10을 순서대로 실행하세요. 핵심만 요약하면:

```bash
docker compose --env-file /opt/remine.env -f docker-compose.prod.yml ps        # 전 서비스 healthy
docker compose --env-file /opt/remine.env -f docker-compose.prod.yml exec app-api env | grep SPRING_PROFILES_ACTIVE   # prod
curl -fsSL https://<domain>/actuator/health                                     # {"status":"UP"}
```

그리고 브라우저로 `https://<domain>` 접속 → demo-login → 체크리스트 → **1MB 초과 사진 업로드 후 정상 표시** → 응원 메시지 AI 생성 → 추억 퀴즈까지 골든 패스 1회 수행.

## 참고: 재부팅 후 자동 기동 확인

```bash
sudo systemctl is-enabled docker   # enabled 여야 함 (get.docker.com 스크립트가 보통 자동 설정)
docker compose --env-file /opt/remine.env -f docker-compose.prod.yml ps
```

## 참고: 롤백

```bash
ssh <server> "cd /opt/remine && git checkout <이전-sha> -- docker-compose.prod.yml deploy/ && IMAGE_TAG=<이전-sha> docker compose --env-file /opt/remine.env -f docker-compose.prod.yml up -d"
```
