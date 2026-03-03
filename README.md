# LoRA 모델 공유 플랫폼

> 만화/웹툰 캐릭터 LoRA 모델 학습, 생성, 공유를 위한 커뮤니티 플랫폼

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![Java](https://img.shields.io/badge/Java-17-007396?style=flat-square&logo=openjdk&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-8.14-02303A?style=flat-square&logo=gradle&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7.0-DC382D?style=flat-square&logo=redis&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-OAuth2%20%2B%20JWT-6DB33F?style=flat-square&logo=springsecurity&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger-API%20Docs-85EA2D?style=flat-square&logo=swagger&logoColor=black)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square&logo=docker&logoColor=white)
![MinIO](https://img.shields.io/badge/MinIO-S3%20Compatible-C72E49?style=flat-square&logo=minio&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)
![Status](https://img.shields.io/badge/Status-Active-success?style=flat-square)

---

## 📋 목차

- [소개](#소개)
- [문제 정의 및 솔루션](#문제-정의-및-솔루션)
- [주요 기능](#주요-기능)
- [주요 링크](#주요-링크)
- [시스템 아키텍처](#시스템-아키텍처)
- [기술 스택](#기술-스택)
- [프로젝트 구조](#프로젝트-구조)
- [시작하기](#시작하기)
- [환경변수 설정](#환경변수-설정)
- [배포 주소](#배포-주소)
- [인증 방식](#인증-방식)
- [역할 및 권한](#역할-및-권한)
- [테스트 로그인](#테스트-로그인)
- [주요 엔드포인트](#주요-엔드포인트)
- [핵심 기능 상세](#핵심-기능-상세)
- [데이터베이스 스키마](#데이터베이스-스키마)
- [HTTP Status Code](#http-status-code)
- [성능 및 보안](#성능-및-보안)
- [Docker 배포](#docker-배포)
- [CI/CD](#cicd)
- [한계와 개선 계획](#한계와-개선-계획)

---

## 소개

만화/웹툰 캐릭터에 특화된 LoRA 모델을 학습하고, AI 이미지를 생성하며, 커뮤니티와 공유할 수 있는 통합 플랫폼입니다.

### 문제 정의 및 솔루션

#### 문제
- AI 이미지 생성 시 LoRA 모델 학습과 공유가 복잡하고 접근성이 낮음
- 학습된 모델의 효과적인 관리 및 커뮤니티 공유 플랫폼 부재
- 비개발자도 쉽게 LoRA 모델을 학습하고 이미지 생성을 할 수 있는 환경 필요

#### 솔루션
- **웹 기반 LoRA 모델 학습**: FastAPI + Modal을 통한 클라우드 GPU 학습
- **모델 공유 커뮤니티**: 학습된 모델 업로드, 검색, 좋아요, 즐겨찾기
- **이미지 생성**: 학습된 LoRA 모델로 즉시 이미지 생성
- **실시간 진행률**: SSE를 통한 학습/생성 진행률 실시간 알림

---

## 주요 기능

### 핵심 기능

- **LoRA 모델 학습**: FastAPI 기반 분산 학습 시스템 (Modal GPU A10G)
- **AI 이미지 생성**: 실시간 진행률 모니터링 (Modal GPU T4)
- **커뮤니티**: 댓글, 좋아요, 즐겨찾기, 인기 랭킹
- **스마트 검색**: 태그 필터링, 키워드 검색
- **간편 로그인**: Google OAuth2 + Firebase Authentication

### 상세 기능 목록

#### 1. 인증 및 사용자 관리
- ✅ Google OAuth2 소셜 로그인
- ✅ Firebase Authentication (이메일/비밀번호)
- ✅ JWT 기반 토큰 인증 (Access Token + Refresh Token)
- ✅ 역할 기반 접근 제어 (USER, TEST, ADMIN)
- ✅ 토큰 만료 및 위조 처리

#### 2. LoRA 모델 관리
- ✅ 모델 업로드, 수정, 삭제 (CRUD)
- ✅ 태그 기반 분류 및 검색
- ✅ 공개/비공개 설정
- ✅ 샘플 이미지 및 프롬프트 관리
- ✅ 좋아요 및 즐겨찾기

#### 3. LoRA 학습
- ✅ 이미지 데이터셋 업로드 (S3)
- ✅ FastAPI + Modal 연동 학습 작업 생성
- ✅ 실시간 학습 진행률 (SSE)
- ✅ 학습 완료 후 모델 자동 저장

#### 4. 이미지 생성
- ✅ 학습된 LoRA 모델로 이미지 생성
- ✅ 실시간 생성 진행률 (SSE)
- ✅ 생성 이력 관리
- ✅ 생성된 이미지 샘플로 등록

#### 5. 커뮤니티
- ✅ 댓글 및 대댓글
- ✅ 댓글 좋아요
- ✅ 통합 검색 (모델, 유저, 태그)

#### 6. 관리자 기능
- ✅ 유저 관리 (목록, 상세, 삭제, 권한 변경)
- ✅ 학습/생성 작업 모니터링
- ✅ 통계 (일별 유저/모델/학습/생성 현황)

---

## 주요 링크

### Production
- **Frontend**: https://blueming.rheon.kr
- **Backend API**: https://api-blueming.rheon.kr
- **Swagger UI**: https://api-blueming.rheon.kr/swagger-ui.html
- **Health Check**: https://api-blueming.rheon.kr/actuator/health
- **MinIO Storage**: https://storage.rheon.kr
- **MinIO Console**: https://minio.rheon.kr

### Repository Links
- **Backend Github**: https://github.com/lxxzdrgnl/Lora-community
- **AI Server Github**: https://github.com/lxxzdrgnl/Lora-training-api

---

## 시스템 아키텍처

### 과거 (AWS 인프라)

```
[사용자]
   │
   ▼
[AWS CloudFront]──────────────────────────────────────────┐
   │                                                       │
   ▼                                                       ▼
[Vue.js Frontend]          [AWS Elastic Beanstalk]     [FastAPI]
(CloudFront CDN)     ───▶  (Spring Boot 3 Backend)  ───▶ (Modal GPU)
                                     │
                    ┌────────────────┼────────────────┐
                    ▼                ▼                 ▼
             [AWS RDS]          [AWS S3]         [Upstash]
             (MySQL 8.0)       (3개 버킷)         (Redis)
```

**과거 인프라 구성**:
| 서비스 | 구성 |
|--------|------|
| Frontend | AWS CloudFront + S3 Static Hosting |
| Backend | AWS Elastic Beanstalk (Spring Boot 3) |
| Database | AWS RDS MySQL 8.0 |
| Cache | Upstash Redis (SSL) |
| Storage | AWS S3 (학습/모델/생성이미지 3개 버킷) |
| AI Service | FastAPI on Modal (GPU A10G/T4) |

### 현재 (Self-hosted 미니 PC)

```
[사용자]
   │
   ▼
[Cloudflare] (DNS + SSL/TLS)
   │  blueming.rheon.kr       → Frontend
   │  api-blueming.rheon.kr   → Backend API
   │  storage.rheon.kr        → MinIO S3 API
   │  minio.rheon.kr          → MinIO Console
   ▼
[Nginx Proxy Manager] (Reverse Proxy, 80/443)
   │
   ├──────────────────────────────────────┐
   ▼                                      ▼
[Vue.js Frontend]           ┌─────────────────────────────┐
(:3030, Docker)             │     Mini PC (Docker)         │
                            │                             │
                            │  [Spring Boot 3 Backend]    │
                            │  (:8080)                    │
                            │      │                      │
                            │  ┌───┴───┬─────────┐       │
                            │  ▼       ▼         ▼       │
                            │ [MySQL] [Redis] [MinIO]    │
                            │ (:3306) (:6379) (:9000)    │
                            └─────────────────────────────┘
                                       │
                                       ▼ (HTTP 콜백)
                            [FastAPI on Modal] ← 외부 플랫폼
                            (GPU A10G/T4, Serverless)
```

**현재 인프라 구성**:
| 서비스 | 구성 |
|--------|------|
| Frontend | Vue.js (Docker, Nginx Proxy Manager) |
| Backend | Spring Boot 3 (Docker, GitHub Actions CI/CD) |
| Database | MySQL 8.0 (Docker Container) |
| Cache | Redis 7 (Docker Container) |
| Storage | MinIO (Docker, S3 호환) |
| AI Service | FastAPI on Modal (**외부 플랫폼**, GPU A10G/T4) |
| DNS/SSL | Cloudflare |
| Reverse Proxy | Nginx Proxy Manager |

---

## 기술 스택

### Backend
- **Framework**: Spring Boot 3.5.7
- **Language**: Java 17
- **Build Tool**: Gradle 8.14.3
- **ORM**: Spring Data JPA ✅

### Security & Auth
- **Authentication**: OAuth2 (Google) + JWT + Firebase ✅
- **Authorization**: Spring Security (RBAC) ✅
- **Session Management**: Stateless (멀티 인스턴스 지원)
- **Password Hashing**: BCrypt ✅

### Database
- **Development**: H2 (In-memory)
- **Production**: MySQL 8.0 (Docker Container) ✅
- **Cache & Session**: Redis 7 (Docker Container) ✅

### API & Communication
- **API Documentation**: SpringDoc OpenAPI 3 (Swagger UI) ✅
- **Real-time Communication**: Server-Sent Events (SSE)
- **Async HTTP Client**: Spring WebFlux WebClient

### AI Service Integration (외부 플랫폼)
- **Framework**: FastAPI (Modal Serverless)
- **GPU**: Modal A10G (Training), T4 (Generation)
- **Communication**: Reactive Streams (Mono, Flux)
- **Progress Tracking**: SSE Streaming

### Storage
- **현재**: MinIO (S3 호환, Docker Container) ✅
- **과거**: AWS S3 (3개 버킷)

### Frontend
- **Framework**: Vue.js 3 + TypeScript ✅
- **State Management**: Pinia
- **UI Library**: Vuetify
- **Deployment**: Mini PC (Cloudflare + Nginx Proxy Manager) ✅

### DevOps
- **CI/CD**: GitHub Actions → SSH → Mini PC ✅
- **Container**: Docker + Docker Compose ✅
- **Deployment**: Self-hosted Mini PC (Ubuntu 24.04, Ryzen R5 5600U) ✅
- **Proxy**: Nginx Proxy Manager + Cloudflare (SSL/DNS)
- **과거**: AWS Elastic Beanstalk + JCloud (병렬 배포)

---

## 프로젝트 구조

```
src/main/java/rheon/wsd_lora_community/
├── WsdLoraCommunityApplication.java
├── global/                      # 공통 기능
│   ├── client/                 # FastApiClient (HTTP 클라이언트)
│   ├── config/                 # 설정 (Security, Firebase, Redis, S3, Swagger, CORS 등)
│   ├── controller/             # SearchController, HealthCheckController
│   ├── dto/                    # ApiResponse, PageResponse, ErrorResponse, BaseEntity
│   ├── exception/              # CustomException, ErrorCode, GlobalExceptionHandler
│   ├── queue/                  # Redis 큐 관리 (JobQueueWorker, RedisQueueService 등)
│   ├── security/               # JWT, OAuth2, Filter (JwtAuthenticationFilter, OAuth2SuccessHandler)
│   ├── service/                # JobCallbackService, S3Service, S3UploadService
│   ├── sse/                    # SseEmitterService (실시간 진행률 전송)
│   └── util/                   # AuthenticationUtil, TestUserChecker
├── user/                        # 유저 관리
│   ├── entity/                 # User, RefreshToken
│   ├── repository/
│   ├── service/                # UserService, AuthService
│   ├── controller/             # UserController, AuthController
│   └── dto/
├── model/                       # LoRA 모델
│   ├── entity/                 # LoraModel, ModelSample, Tag, ModelPrompt, ModelTag
│   ├── repository/
│   ├── service/                # LoraModelService, SampleService, PromptService, TagService
│   ├── controller/             # LoraModelController, TagController
│   └── dto/
├── community/                   # 커뮤니티
│   ├── entity/                 # Comment, CommentLike, ModelLike, ModelFavorite
│   ├── repository/
│   ├── service/                # CommentService, LikeService, FavoriteService
│   ├── controller/             # CommentController
│   └── dto/
├── training/                    # 학습 관리
│   ├── entity/                 # TrainingJob
│   ├── repository/
│   ├── service/                # TrainingService
│   ├── controller/             # TrainingController (FastAPI 연동)
│   └── dto/
├── generation/                  # 이미지 생성
│   ├── entity/                 # GenerationHistory
│   ├── repository/
│   ├── service/                # GenerationService
│   ├── controller/             # GenerationController (FastAPI 연동)
│   └── dto/
├── admin/                       # 관리자 기능
│   ├── controller/             # AdminController (유저 관리, 통계)
│   ├── service/                # AdminService
│   └── dto/
└── util/                        # 공통 유틸리티
```

---

## 시작하기

### 요구사항

- Java 17 이상
- Gradle 8.14 이상
- MySQL 8.0 이상 (또는 Docker)
- Redis (또는 Docker)

### 로컬 환경에서 빌드

```bash
# 프로젝트 클론
git clone <repository-url>
cd WSD_Lora_community

# 환경변수 설정
cp .env.example .env
# .env 파일을 열어 실제 값으로 수정
```

### 실행 방법

#### 방법 1: Gradle로 직접 실행
```bash
# 빌드
./gradlew clean build -x test

# 로컬 실행
./gradlew bootRun
```

#### 방법 2: Docker Compose로 실행 (권장)
```bash
# 빌드
./gradlew clean build -x test

# Docker Compose 실행 (MySQL + Redis + Spring Boot)
docker-compose up -d

# 로그 확인
docker-compose logs -f

# 종료
docker-compose down
```

서버는 `http://localhost:5000`에서 실행됩니다.

### 헬스 체크

```bash
GET http://localhost:5000/
```

**응답 예시:**
```json
{
  "success": true,
  "message": "LoRA 모델 공유 플랫폼 API가 정상적으로 실행 중입니다.",
  "data": {
    "status": "UP",
    "service": "WSD_Lora_community"
  }
}
```

---

## 환경변수 설정

`.env.example` 파일을 참고하여 `.env` 파일을 생성하세요.

### 필수 환경변수

| 변수명 | 설명 | 예시 |
|--------|------|------|
| `DB_HOST` | MySQL 호스트 | `localhost` |
| `DB_PORT` | MySQL 포트 | `3306` |
| `DB_NAME` | 데이터베이스명 | `loradb` |
| `DB_USERNAME` | MySQL 유저명 | `root` |
| `DB_PASSWORD` | MySQL 비밀번호 | `password` |
| `REDIS_HOST` | Redis 호스트 | `localhost` |
| `REDIS_PORT` | Redis 포트 | `6379` |
| `REDIS_PASSWORD` | Redis 비밀번호 (선택) | (공백 가능) |
| `REDIS_SSL_ENABLED` | Redis SSL 사용 여부 | `false` |
| `JWT_SECRET` | JWT 서명 키 (256bit 이상) | `your-secret-key-min-256-bits` |
| `GOOGLE_CLIENT_ID` | Google OAuth2 클라이언트 ID | `123456...apps.googleusercontent.com` |
| `GOOGLE_CLIENT_SECRET` | Google OAuth2 클라이언트 시크릿 | `GOCSPX-...` |
| `FIREBASE_PROJECT_ID` | Firebase 프로젝트 ID | `my-lora-auth` |
| `FIREBASE_PRIVATE_KEY_ID` | Firebase Private Key ID | `9f88093f...` |
| `FIREBASE_PRIVATE_KEY` | Firebase Private Key (Base64) | `LS0tLS1...` |
| `FIREBASE_CLIENT_EMAIL` | Firebase 클라이언트 이메일 | `firebase-adminsdk-...@....iam.gserviceaccount.com` |
| `FIREBASE_CLIENT_ID` | Firebase 클라이언트 ID | `116885161361666385411` |
| `AWS_ACCESS_KEY_ID` | MinIO Access Key | `admin` |
| `AWS_SECRET_ACCESS_KEY` | MinIO Secret Key | `your-password` |
| `AWS_S3_ENDPOINT` | MinIO 내부 엔드포인트 | `http://minio:9000` |
| `AWS_S3_PUBLIC_ENDPOINT` | MinIO 외부 URL (브라우저용) | `https://storage.rheon.kr` |
| `AWS_S3_REGION` | S3 리전 (MinIO는 임의값) | `us-east-1` |
| `AWS_S3_TRAINING_BUCKET` | 학습 데이터 버킷 | `lora-training-data` |
| `AWS_S3_MODELS_BUCKET` | 모델 버킷 | `lora-models` |
| `AWS_S3_GENERATED_BUCKET` | 생성 이미지 버킷 | `lora-generated-images` |
| `FASTAPI_URL` | FastAPI 서버 URL (Modal) | `https://...modal.run` |
| `FRONTEND_URL` | 프론트엔드 URL | `https://blueming.rheon.kr` |
| `CALLBACK_URL` | 백엔드 콜백 URL | `https://api-blueming.rheon.kr` |
| `CORS_ORIGINS` | CORS 허용 Origin | `https://blueming.rheon.kr` |

---

## 배포 주소

### Production (현재 - Self-hosted 미니 PC)
- **Frontend**: https://blueming.rheon.kr
- **Backend API**: https://api-blueming.rheon.kr
- **Swagger UI**: https://api-blueming.rheon.kr/swagger-ui.html
- **Health Check**: https://api-blueming.rheon.kr/actuator/health
- **MinIO Storage**: https://storage.rheon.kr
- **MinIO Console**: https://minio.rheon.kr

### 과거 배포 주소 (AWS + JCloud)
- **Frontend (AWS CloudFront)**: https://d2f4r8lrfwl0ez.cloudfront.net _(종료)_
- **Backend (AWS CloudFront)**: https://d3ka730j70ocy8.cloudfront.net _(종료)_
- **Backend (JCloud)**: http://113.198.66.68:18232 _(종료)_

---

## 인증 방식

### JWT 기반 Stateless 인증

#### 1. Google OAuth2 로그인
```
1. 사용자가 "Google 로그인" 클릭
2. 프론트엔드가 /api/auth/google 호출
3. Spring Security가 Google OAuth2 인증 페이지로 리다이렉트
4. 사용자 인증 후 Google이 /login/oauth2/code/google로 콜백
5. OAuth2SuccessHandler가 JWT 토큰 생성
6. 프론트엔드로 리다이렉트 (URL Fragment에 토큰 포함)
7. 프론트엔드가 토큰을 LocalStorage에 저장
```

#### 2. Firebase 이메일/비밀번호 로그인
```
1. 사용자가 이메일/비밀번호 입력
2. 프론트엔드가 Firebase SDK로 로그인
3. Firebase ID Token 발급
4. POST /api/auth/firebase/login에 ID Token 전송
5. 백엔드가 Firebase Admin SDK로 ID Token 검증
6. JWT 토큰 발급 및 응답
7. 프론트엔드가 JWT 토큰 저장
```

#### 3. JWT 토큰 사용
```
1. 모든 인증 필요 API 요청에 Authorization 헤더 포함
   - Authorization: Bearer {accessToken}
2. Access Token 만료 시 (401 에러)
   - POST /api/auth/refresh에 Refresh Token 전송
   - 새로운 Access Token 발급
3. Refresh Token도 만료 시
   - 재로그인 필요
```

#### 토큰 정보
- **Access Token**: 1시간 유효
- **Refresh Token**: 7일 유효

### Stateless 설계의 장점

- JWT 기반 인증으로 서버 메모리 세션 불필요
- Refresh Token만 DB 저장
- 멀티 인스턴스 환경에서 수평 확장 가능
- 로드밸런서 없이도 인스턴스 간 세션 공유

---

## 역할 및 권한

### 역할 (Role)

| 역할 | 설명 | 권한 |
|------|------|------|
| `USER` | 일반 사용자 | 모델 조회, 생성, 수정(본인), 삭제(본인), 학습, 이미지 생성, 댓글 |
| `TEST` | 테스트 계정 | USER 권한 + 테스트 엔드포인트 접근 |
| `ADMIN` | 관리자 | 모든 권한 + 유저 관리 + 통계 조회 + 모든 모델 삭제 |

### 권한 매트릭스

| API | USER | TEST | ADMIN |
|-----|------|------|-------|
| 모델 조회 | ✅ | ✅ | ✅ |
| 모델 생성 | ✅ | ✅ | ✅ |
| 모델 수정 (본인) | ✅ | ✅ | ✅ |
| 모델 삭제 (본인) | ✅ | ✅ | ✅ |
| 모델 삭제 (타인) | ❌ | ❌ | ✅ |
| 학습 작업 생성 | ✅ | ✅ | ✅ |
| 이미지 생성 | ✅ | ✅ | ✅ |
| 댓글 작성 | ✅ | ✅ | ✅ |
| 유저 관리 | ❌ | ❌ | ✅ |
| 통계 조회 | ❌ | ❌ | ✅ |
| 테스트 엔드포인트 | ❌ | ✅ | ✅ |

---

## 테스트 로그인

### `/test` 엔드포인트로 테스트 로그인 가능

```bash
POST http://localhost:5000/test
```

**응답 예시:**
```json
{
  "success": true,
  "message": "테스트 엔드포인트 - 인증 없이 접근 가능",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "id": 1,
      "email": "test@example.com",
      "role": "TEST"
    }
  }
}
```

이 토큰을 사용하여 인증이 필요한 API를 테스트할 수 있습니다.

---

## 주요 엔드포인트

### 인증

- `GET /oauth2/authorization/google` - Google OAuth2 로그인
- `POST /api/auth/firebase/register` - Firebase 회원가입
- `POST /api/auth/firebase/login` - Firebase 로그인
- `POST /api/auth/refresh` - 액세스 토큰 갱신
- `POST /api/auth/logout` - 로그아웃
- `GET /api/auth/me` - 현재 유저 정보 조회

### 유저

- `GET /api/users/me` - 내 프로필 조회
- `PUT /api/users/me` - 내 프로필 수정
- `GET /api/users/{userId}` - 유저 프로필 조회
- `GET /api/users/search` - 유저 검색

### LoRA 모델

- `POST /api/models` - 모델 생성
- `GET /api/models` - 공개 모델 목록 조회
- `GET /api/models/{modelId}` - 모델 상세 조회
- `PUT /api/models/{modelId}` - 모델 수정
- `DELETE /api/models/{modelId}` - 모델 삭제
- `GET /api/models/search` - 모델 검색
- `POST /api/models/{modelId}/samples` - 샘플 이미지 추가
- `POST /api/models/{modelId}/prompts` - 프롬프트 추가

### 커뮤니티

- `POST /api/models/{modelId}/comments` - 댓글 작성
- `GET /api/models/{modelId}/comments` - 댓글 목록 조회
- `POST /api/models/{modelId}/like` - 모델 좋아요 토글
- `POST /api/models/{modelId}/favorite` - 즐겨찾기 토글
- `POST /api/models/{modelId}/comments/{commentId}/like` - 댓글 좋아요 토글

### 태그

- `GET /api/tags` - 전체 태그 조회
- `GET /api/tags/popular` - 인기 태그 조회
- `GET /api/tags/category/{category}` - 카테고리별 태그 조회
- `POST /api/tags/models/{modelId}` - 모델에 태그 추가

### 학습 (Modal GPU)

- `POST /api/training/upload-urls` - 학습 이미지 업로드 URL 생성
- `POST /api/training/models/{modelId}` - 학습 작업 생성
- `POST /api/training/jobs/{jobId}/start` - 학습 시작 (Modal GPU A10G)
- `GET /api/training/jobs/{jobId}` - 학습 작업 조회
- `GET /api/training/my` - 내 학습 작업 목록
- `POST /api/training/callback` - Modal 학습 완료 콜백
- `GET /api/training/stream` - 실시간 진행률 스트리밍 (SSE)
- `GET /api/training/fastapi/status` - Modal 학습 상태 조회

### 이미지 생성 (Modal GPU)

- `POST /api/generate` - 이미지 생성 요청 (Modal GPU T4)
- `POST /api/generate/history` - Modal 생성 완료 콜백
- `GET /api/generate/history` - 생성 기록 조회
- `POST /api/generate/history/{historyId}/sample` - 샘플로 등록
- `GET /api/generate/stream` - 실시간 진행률 스트리밍 (SSE)
- `GET /api/generate/fastapi/status` - Modal 생성 상태 조회

### 검색

- `GET /api/search` - 통합 검색 (모델 + 유저)
- `GET /api/search/models` - 모델 검색
- `GET /api/search/models/tags` - 태그로 모델 필터링
- `GET /api/search/models/popular` - 인기 모델 조회
- `GET /api/search/users` - 유저 검색

### 관리자 전용 (ADMIN)

- `GET /api/admin/users` - 전체 유저 목록 (페이지네이션)
- `GET /api/admin/users/{id}` - 유저 상세 조회
- `DELETE /api/admin/users/{id}` - 유저 삭제
- `PATCH /api/admin/users/{id}/role` - 유저 권한 변경
- `GET /api/admin/jobs/training` - 전체 학습 작업 목록
- `GET /api/admin/jobs/generation` - 전체 생성 작업 목록
- `GET /api/admin/statistics/users` - 일별 유저 통계
- `GET /api/admin/statistics/models` - 일별 모델 통계
- `GET /api/admin/statistics/training` - 일별 학습 통계
- `GET /api/admin/statistics/generation` - 일별 생성 통계

**총 엔드포인트 수**: 50개 이상 ✅

---

## 핵심 기능 상세

### 1. LoRA 모델 학습 (Modal Serverless GPU)

**플로우:**
```
1. 프론트엔드 → POST /api/training/upload-urls
   { "fileNames": ["img1.jpg", "img2.jpg", ...] }

2. Spring Boot → S3 Presigned URLs 생성
   응답: { "uploadUrls": [...], "downloadUrls": [...] }

3. 프론트엔드 → S3에 직접 업로드 (각 uploadUrl로 PUT)

4. 프론트엔드 → POST /api/training/jobs/{jobId}/start
   {
     "totalEpochs": 10,
     "modelName": "my_model",
     "trainingImageUrls": ["s3-url-1", "s3-url-2", ...],
     "callbackBaseUrl": "https://your-server.com"
   }

5. Spring Boot → Modal API (GPU A10G)
   POST https://dldydwo9--lora-training-inference-fastapi-app.modal.run/train

6. Modal → S3에서 이미지 다운로드 → LoRA 학습 → 모델 S3 업로드

7. Modal → Spring Boot 콜백
   POST /api/training/callback
   {
     "userId": "123",
     "modelName": "my_model",
     "s3Key": "models/123/my_model.safetensors",
     "fileSize": 142857600,
     "status": "SUCCESS"
   }

8. Spring Boot → DB 저장 (모델 상태 COMPLETED로 변경)
```

**주요 기능:**
- Modal Serverless GPU (A10G) 사용
- S3 기반 이미지 업로드/다운로드
- 비동기 처리 (콜백 기반)
- 실시간 진행률 모니터링 (SSE)
- 자동 모델 상태 업데이트

### 2. AI 이미지 생성 (Modal Serverless GPU)

**플로우:**
```
1. 프론트엔드 → POST /api/generate
   {
     "modelId": 1,
     "prompt": "anime girl with blue hair",
     "numImages": 4,
     "steps": 40,
     "guidanceScale": 7.5,
     "seed": 12345
   }

2. Spring Boot → LoRA 모델의 S3 Presigned URL 생성

3. Spring Boot → Modal API (GPU T4)
   POST https://dldydwo9--lora-training-inference-fastapi-app.modal.run/generate

4. Modal → S3에서 LoRA 모델 다운로드 → 이미지 생성 → S3 업로드

5. Modal → Spring Boot 콜백
   POST /api/generate/history
   {
     "userId": "123",
     "modelId": 1,
     "imageS3Keys": ["user-123/img1.png", "user-123/img2.png", ...],
     "status": "SUCCESS"
   }

6. Spring Boot → DB 저장 (GenerationHistory + GeneratedImage)
```

**주요 기능:**
- Modal Serverless GPU (T4) 사용
- S3 기반 모델/이미지 저장
- 프롬프트 기반 이미지 생성
- 생성 파라미터 커스터마이징
- 실시간 진행률 추적 (SSE)
- 생성 이미지 자동 저장 및 샘플 등록

### 3. 커뮤니티 기능

- 모델 좋아요/즐겨찾기
- 댓글 및 대댓글
- 인기 모델 랭킹

### 4. 검색 및 필터링

- 키워드 검색 (제목, 설명)
- 다중 태그 필터링
- 정렬 (최신순, 인기순)
- 유저별 모델 조회

---

## 데이터베이스 스키마

### 주요 테이블

| 테이블 | 설명 |
|--------|------|
| `users` | 유저 정보 (OAuth2 연동) |
| `lora_models` | LoRA 모델 메타데이터 |
| `model_samples` | 샘플 이미지 |
| `model_prompts` | 프롬프트 예시 |
| `tags` | 태그 (스타일, 캐릭터, 장르) |
| `model_tags` | 모델-태그 매핑 |
| `training_jobs` | 학습 작업 (진행률 추적) |
| `generation_history` | 이미지 생성 기록 |
| `comments` | 댓글 및 대댓글 |
| `comment_likes` | 댓글 좋아요 |
| `model_likes` | 모델 좋아요 |
| `model_favorites` | 모델 즐겨찾기 |
| `refresh_tokens` | 리프레시 토큰 |

---

## HTTP Status Code

| Status Code | 사용 사례 |
|-------------|----------|
| **200 OK** | 조회 성공, 업데이트 성공 |
| **201 Created** | 생성 성공 (모델, 댓글, 학습 작업 등) |
| **204 No Content** | 삭제 성공 |
| **400 Bad Request** | 잘못된 요청 (validation 실패) |
| **401 Unauthorized** | 인증 실패 (토큰 없음/만료) |
| **403 Forbidden** | 권한 없음 (ADMIN 전용 API에 USER 접근) |
| **404 Not Found** | 리소스 없음 |
| **409 Conflict** | 중복 리소스 (이미 좋아요한 모델 등) |
| **422 Unprocessable Entity** | 비즈니스 로직 검증 실패 |
| **429 Too Many Requests** | Rate Limit 초과 |
| **500 Internal Server Error** | 서버 에러 |
| **503 Service Unavailable** | 외부 서비스 (FastAPI, S3) 일시 불가 |

**사용된 Status Code**: 12종 ✅

---

## 성능 및 보안

### 보안

#### 1. 인증 및 권한
- **JWT 기반 Stateless 인증**: Access Token (1시간) + Refresh Token (7일) ✅
- **역할 기반 접근 제어 (RBAC)**: USER, TEST, ADMIN 역할 분리 ✅
- **Spring Security**: Method-level 권한 검증 (`@PreAuthorize`)
- **OAuth2**: Google 소셜 로그인 ✅
- **Firebase Authentication**: 이메일/비밀번호 인증 + ID Token 검증 ✅
- **토큰 만료 및 위조 처리**: JWT 서명 검증, 만료 시간 체크 ✅

#### 2. 비밀번호 보안
- **BCrypt**: 비밀번호 해싱 (Firebase에서 자동 처리) ✅

#### 3. CORS
- **Origin 제한**: 프론트엔드 URL만 허용 ✅
- **Credentials 허용**: 쿠키 기반 인증 지원

#### 4. Rate Limiting
- **IP 기반 요청 제한**: 1분당 최대 100개 요청 ✅
- **429 Too Many Requests**: 제한 초과 시 에러 응답 ✅
- **프록시 지원**: X-Forwarded-For, X-Real-IP 헤더 처리 ✅

#### 5. Input Validation
- **@Valid**: DTO 레벨 검증 ✅
- **통일된 에러 응답**: GlobalExceptionHandler ✅

#### 6. 민감정보 보호
- **환경변수**: JWT Secret, DB 비밀번호, OAuth2 Secret 등
- **.gitignore**: `.env` 제외

### 성능

#### 1. 데이터베이스
- **인덱스**: userId, modelId, createdAt 등
- **페이지네이션**: 모든 목록 조회 API ✅

#### 2. 캐싱
- **Redis**: 학습/생성 작업 상태, Refresh Token ✅

#### 3. 비동기 처리
- **FastAPI 연동**: WebClient (비동기)
- **SSE**: 실시간 진행률 전송

#### 4. 파일 저장
- **MinIO**: 이미지, 모델 파일 저장 (S3 호환, Docker Container) ✅

#### 5. 로깅
- **레벨 제어**: WARN (root), DEBUG (application) ✅

---

## Docker 배포

### Dockerfile.runtime (CI/CD용)

CI/CD에서 빌드된 JAR를 복사해 실행하는 경량 이미지입니다. 미니 PC에서 컴파일하지 않아 배포 시 CPU/RAM 부담이 없습니다.

```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN apk add --no-cache curl
RUN addgroup -S spring && adduser -S spring -G spring
COPY application.jar app.jar
RUN chown spring:spring app.jar
USER spring:spring
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-Xmx768m", "-Xms256m", "-jar", "app.jar"]
```

### docker-compose.yml (미니 PC)

```yaml
version: '3.8'

services:
  app:
    build:
      context: ./backend
      dockerfile: Dockerfile.runtime
    container_name: lora-app
    restart: always
    env_file: ./backend/.env
    ports:
      - "8080:8080"
    depends_on:
      - db
      - redis
    deploy:
      resources:
        limits:
          memory: 1.5G
    networks:
      - blueming-net

  db:
    image: mysql:8.0
    container_name: mysql
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: root_password
      MYSQL_DATABASE: blooming
    volumes:
      - ./mysql_data:/var/lib/mysql
    networks:
      - blueming-net

  redis:
    image: redis:7-alpine
    container_name: redis
    restart: always
    volumes:
      - ./redis_data:/data
    command: redis-server --appendonly yes
    networks:
      - blueming-net

  minio:
    image: minio/minio
    container_name: minio
    restart: always
    environment:
      MINIO_ROOT_USER: admin
      MINIO_ROOT_PASSWORD: your-password
      MINIO_SERVER_URL: https://storage.rheon.kr
      MINIO_BROWSER_REDIRECT_URL: https://minio.rheon.kr
    ports:
      - "9000:9000"
      - "9001:9001"
    volumes:
      - ./minio_data:/data
    command: server /data --console-address ":9001"
    networks:
      - blueming-net

networks:
  blueming-net:
    external: true
```

### 로그 확인
```bash
# 전체 로그
docker compose logs -f

# 앱만
docker compose logs -f app
```

---

## CI/CD

### GitHub Actions → 미니 PC SSH 배포

`.github/workflows/deploy.yml`:
- **트리거**: `main` 브랜치 push ✅
- **배포 환경**: Self-hosted 미니 PC (SSH) ✅

#### 배포 흐름

```
[git push main]
      │
      ▼
[GitHub Actions Runner] (ubuntu-latest)
      │
      ├─ 1. Checkout + JDK 17 + Gradle 캐시
      │
      ├─ 2. Build
      │      ./gradlew build -x test
      │      → build/libs/application.jar
      │
      ├─ 3. Upload artifact (GitHub)
      │
      └─ 4. Deploy job
             │
             ├─ SSH 키 설정 (secrets.PC_SSH_KEY)
             │
             ├─ SCP 전송
             │    application.jar → ~/blueming/backend/
             │    Dockerfile.runtime → ~/blueming/backend/
             │
             └─ SSH 명령 실행
                  cd ~/blueming
                  docker compose up -d --build app --no-deps
                  ✅ 배포 완료
```

#### 필요한 GitHub Secrets

| Secret | 설명 |
|--------|------|
| `PC_SSH_KEY` | 미니 PC SSH 개인키 |
| `PC_SSH_HOST` | 미니 PC 외부 IP (`61.99.168.118`) |
| `PC_SSH_USER` | SSH 사용자명 (`rheon`) |

#### 특징
- **컴파일 없음**: 미니 PC에서 빌드하지 않음 (RAM 절약)
- **Dockerfile.runtime**: JRE만 포함한 경량 이미지 (eclipse-temurin:17-jre-alpine)
- **JVM 메모리 제한**: `-Xmx768m -Xms256m` (총 RAM 8GB 중 최대 1.5G)
- **헬스체크**: `/actuator/health` 90초 기다린 후 3회 재시도

#### 과거 CI/CD (AWS + JCloud 병렬 배포)
```
[git push main]
      │
      ├─ AWS Elastic Beanstalk 배포
      │    → https://d3ka730j70ocy8.cloudfront.net
      │
      └─ JCloud SSH 배포 (병렬)
           → http://113.198.66.68:18232
```

---

## 한계와 개선 계획

### 현재 한계
1. **메모리 기반 Rate Limiting**: ConcurrentHashMap 사용 (멀티 인스턴스 환경에서 제한적)
2. **Soft Delete**: 일부 엔티티만 적용
3. **테스트 커버리지**: 단위 테스트 확대 필요

### 개선 계획
1. **Redis 기반 분산 Rate Limiter**: 멀티 인스턴스 환경에서 공유 가능
2. **Elasticsearch**: 모델 검색 성능 개선
3. **WebSocket**: SSE 대신 양방향 통신

---