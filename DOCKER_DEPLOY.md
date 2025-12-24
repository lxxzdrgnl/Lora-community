# Docker 배포 가이드

## 📋 사전 준비

### 1. 외부 서비스 준비
- **MySQL (RDS)**: 데이터베이스 서버
- **Redis (Upstash)**: 캐시 및 작업 큐
- **AWS S3**: 파일 저장소 (3개 버킷)
- **Google OAuth2**: 로그인 인증
- **FastAPI/Modal**: AI 학습/생성 서버

### 2. 필수 도구
- Docker 20.10 이상
- Docker Compose 2.0 이상

---

## 🚀 배포 방법

### 1. 환경변수 설정

프로젝트 루트에 `.env` 파일을 생성합니다:

```bash
cp .env.example .env
```

`.env` 파일을 편집하여 실제 값을 입력합니다:

```bash
vi .env
```

**필수 환경변수:**
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`
- `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`
- `JWT_SECRET` (최소 256비트)
- `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`
- `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`
- `FASTAPI_URL`
- `FRONTEND_URL`, `CALLBACK_URL`

### 2. Docker Compose 실행

```bash
# 백그라운드에서 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f app

# 상태 확인
docker-compose ps
```

### 3. Health Check 확인

```bash
# 컨테이너 health 상태 확인
docker ps

# Actuator health 엔드포인트 직접 확인
curl http://localhost:8080/actuator/health
```

**정상 응답:**
```json
{
  "status": "UP"
}
```

### 4. Swagger UI 확인

브라우저에서 접속:
```
http://localhost:8080/swagger-ui.html
```

---

## 🔧 관리 명령어

### 컨테이너 관리

```bash
# 컨테이너 중지
docker-compose stop

# 컨테이너 시작
docker-compose start

# 컨테이너 재시작
docker-compose restart

# 컨테이너 삭제 (데이터는 유지)
docker-compose down

# 컨테이너 + 볼륨 삭제 (데이터도 삭제)
docker-compose down -v
```

### 로그 확인

```bash
# 실시간 로그 확인
docker-compose logs -f app

# 최근 100줄 로그 확인
docker-compose logs --tail=100 app

# 타임스탬프 포함 로그
docker-compose logs -f -t app
```

### 이미지 재빌드

코드 변경 후 재배포:

```bash
# 이미지 재빌드 + 재시작
docker-compose up -d --build

# 캐시 없이 완전히 새로 빌드
docker-compose build --no-cache
docker-compose up -d
```

---

## 🛠️ 트러블슈팅

### 1. 컨테이너가 시작되지 않을 때

```bash
# 로그 확인
docker-compose logs app

# 컨테이너 내부 접속
docker-compose exec app sh
```

### 2. Health Check 실패

```bash
# 컨테이너 내부에서 health check 직접 실행
docker-compose exec app curl -f http://localhost:8080/actuator/health

# Spring Boot 로그 확인
docker-compose logs -f app | grep ERROR
```

### 3. 환경변수가 제대로 적용되지 않을 때

```bash
# 컨테이너 환경변수 확인
docker-compose exec app env | grep DB_

# .env 파일 확인
cat .env

# 컨테이너 재생성 (환경변수 다시 로드)
docker-compose down
docker-compose up -d
```

### 4. 빌드 실패

```bash
# Gradle 캐시 삭제 후 재빌드
docker-compose build --no-cache

# 로컬에서 빌드 테스트
./gradlew clean build -x test
```

---

## 📊 모니터링

### 컨테이너 리소스 사용량

```bash
# 실시간 리소스 모니터링
docker stats lora-app

# 디스크 사용량
docker system df
```

### 애플리케이션 메트릭

```bash
# Health 상태
curl http://localhost:8080/actuator/health

# JVM 메모리 정보 (보안상 비활성화됨, 필요시 application.yml 수정)
# curl http://localhost:8080/actuator/metrics/jvm.memory.used
```

---

## 🔐 보안 권장사항

1. **Non-root 사용자**: Dockerfile에서 이미 `spring` 사용자로 실행
2. **환경변수 관리**: `.env` 파일은 절대 Git에 커밋하지 마세요
3. **JWT Secret**: 256비트 이상의 강력한 키 사용
4. **DB 접근**: RDS 보안 그룹 설정 확인
5. **Redis SSL**: Upstash Redis는 SSL 필수 (`REDIS_SSL_ENABLED=true`)

---

## 📝 환경별 설정

### 개발 환경 (로컬)

```bash
# .env
SPRING_PROFILES_ACTIVE=local
DB_HOST=localhost
REDIS_HOST=localhost
```

### 프로덕션 환경

```bash
# .env
SPRING_PROFILES_ACTIVE=prod
DB_HOST=your-rds-endpoint.ap-northeast-2.rds.amazonaws.com
REDIS_HOST=your-redis.upstash.io
```

---

## 🔄 업데이트 배포 절차

```bash
# 1. 코드 업데이트
git pull origin main

# 2. 이미지 재빌드
docker-compose build

# 3. 무중단 재시작 (새 컨테이너 시작 → 기존 컨테이너 중지)
docker-compose up -d --no-deps --build app

# 4. 로그 확인
docker-compose logs -f app
```

---

## 📞 문제 발생 시

1. **로그 확인**: `docker-compose logs -f app`
2. **Health Check**: `curl http://localhost:8080/actuator/health`
3. **환경변수**: `docker-compose exec app env`
4. **컨테이너 재시작**: `docker-compose restart app`

---

## 📌 참고 링크

- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Docker Compose](https://docs.docker.com/compose/)
- [프로젝트 README](./README.md)
