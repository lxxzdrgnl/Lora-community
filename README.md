# LoRA 모델 공유 플랫폼

> 만화/웹툰 캐릭터 LoRA 모델 학습, 생성, 공유를 위한 커뮤니티 플랫폼

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![Java](https://img.shields.io/badge/Java-17-007396?style=flat-square&logo=openjdk&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-8.14-02303A?style=flat-square&logo=gradle&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7.0-DC382D?style=flat-square&logo=redis&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-OAuth2%20%2B%20JWT-6DB33F?style=flat-square&logo=springsecurity&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger-API%20Docs-85EA2D?style=flat-square&logo=swagger&logoColor=black)
![AWS Elastic Beanstalk](https://img.shields.io/badge/AWS%20Elastic%20Beanstalk-Deployed-232F3E?style=flat-square&logo=aws&logoColor=white)
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
- **Frontend (AWS CloudFront)**: https://d2f4r8lrfwl0ez.cloudfront.net
- **Frontend (JCloud)**: https://113.198.66.68:17196
- **Backend (AWS CloudFront)**: https://d3ka730j70ocy8.cloudfront.net
- **Backend (JCloud)**: https://113.198.66.68:17232
- **Swagger UI (AWS)**: https://d3ka730j70ocy8.cloudfront.net/swagger-ui.html
- **Swagger UI (JCloud)**: https://113.198.66.68:17232/swagger-ui.html
- **Health Check (AWS)**: https://d3ka730j70ocy8.cloudfront.net/actuator/health
- **Health Check (JCloud)**: https://113.198.66.68:17232/actuator/health

### Repository Links
- **Backend Github**: https://github.com/lxxzdrgnl/Lora-community
- **AI Server Github**: https://github.com/lxxzdrgnl/Lora-training-api

---

## 시스템 아키텍처

```
┌─────────────┐      ┌───────────────────────────┐      ┌─────────────────┐
│   Vue.js    │ <--> │ AWS Elastic Beanstalk     │ <--> │    FastAPI      │
│  (Frontend) │      │ (Spring Boot 3 Backend)   │      │  (AI Service)   │
└─────────────┘      └───────────┬───────────────┘      └─────────────────┘
                                 │
                    ┌────────────┴────────────┐
                    │                         │
                    v                         v
            ┌─────────────────┐      ┌─────────────────┐
            │   AWS RDS       │      │   AWS S3        │
            │   (MySQL)       │      │   (Storage)     │
            └─────────────────┘      └─────────────────┘
                    │
                    v
            ┌─────────────────┐
            │   Redis         │
            │   (Upstash)     │
            └─────────────────┘
```

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
- **Production**: MySQL 8.0+ on AWS RDS ✅
- **Cache & Session**: Redis (Upstash) ✅

### API & Communication
- **API Documentation**: SpringDoc OpenAPI 3 (Swagger UI) ✅
- **Real-time Communication**: Server-Sent Events (SSE)
- **Async HTTP Client**: Spring WebFlux WebClient

### AI Service Integration
- **Framework**: FastAPI (Modal Serverless)
- **GPU**: Modal A10G (Training), T4 (Generation)
- **Communication**: Reactive Streams (Mono, Flux)
- **Progress Tracking**: SSE Streaming
- **Storage**: AWS S3 (Models, Images, Datasets) ✅

### Frontend
- **Framework**: Vue.js 3 + TypeScript ✅
- **State Management**: Pinia
- **UI Library**: Vuetify
- **Deployment**: AWS CloudFront ✅

### DevOps
- **CI/CD**: GitHub Actions ✅
- **Container**: Docker + Docker Compose ✅
- **Deployment**: AWS Elastic Beanstalk ✅
- **Monitoring**: AWS CloudWatch

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
| `AWS_ACCESS_KEY_ID` | AWS Access Key | `AKIA...` |
| `AWS_SECRET_ACCESS_KEY` | AWS Secret Key | `...` |
| `AWS_S3_REGION` | AWS S3 리전 | `ap-northeast-2` |
| `AWS_S3_TRAINING_BUCKET` | S3 학습 데이터 버킷 | `lora-training-data-bucket` |
| `AWS_S3_MODELS_BUCKET` | S3 모델 버킷 | `lora-models-bucket` |
| `AWS_S3_GENERATED_BUCKET` | S3 생성 이미지 버킷 | `lora-generated-image-bucket` |
| `FASTAPI_URL` | FastAPI 서버 URL | `https://...modal.run` |
| `FRONTEND_URL` | 프론트엔드 URL | `http://localhost:5173` |
| `CALLBACK_URL` | 백엔드 콜백 URL | `http://localhost:5000` |
| `CORS_ORIGINS` | CORS 허용 Origin | `http://localhost:5173` |

---

## 배포 주소

### Production
- **Frontend (AWS CloudFront)**: https://d2f4r8lrfwl0ez.cloudfront.net
- **Frontend (JCloud)**: https://113.198.66.68:17196
- **Backend (AWS CloudFront)**: https://d3ka730j70ocy8.cloudfront.net
- **Backend (JCloud)**: https://113.198.66.68:17232
- **Swagger UI (AWS)**: https://d3ka730j70ocy8.cloudfront.net/swagger-ui.html
- **Swagger UI (JCloud)**: https://113.198.66.68:17232/swagger-ui.html
- **Health Check (AWS)**: https://d3ka730j70ocy8.cloudfront.net/actuator/health
- **Health Check (JCloud)**: https://113.198.66.68:17232/actuator/health

### Database (AWS RDS)
- **호스트**: `blueming-database.cr8gyu4cwsxv.ap-northeast-2.rds.amazonaws.com`
- **포트**: `3306`
- **데이터베이스**: `loradb`
- **유저명**: `admin` (읽기 전용 권장)

⚠️ **주의**: 테스트용 DB 접근 시 읽기 전용으로만 사용하세요.

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
- **AWS S3**: 이미지, 모델 파일 저장 ✅

#### 5. 로깅
- **레벨 제어**: WARN (root), DEBUG (application) ✅

---

## Docker 배포

### Dockerfile
```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 5000
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### docker-compose.yml
```yaml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "5000:5000"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_HOST=mysql
      - REDIS_HOST=redis
    depends_on:
      - mysql
      - redis

  mysql:
    image: mysql:8.0
    environment:
      MYSQL_DATABASE: loradb
      MYSQL_ROOT_PASSWORD: password
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data

volumes:
  mysql-data:
  redis-data:
```

### 실행
```bash
# 빌드
./gradlew clean build -x test

# Docker Compose 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f

# 종료
docker-compose down
```

---

## CI/CD

### GitHub Actions를 통한 멀티 배포

`.github/workflows/deploy.yml`:
- **트리거**: `main` 브랜치 push ✅
- **배포 환경**: AWS Elastic Beanstalk + JCloud (병렬 배포) ✅

#### 배포 단계
1. **빌드 준비**
   - Java 17 설치
   - Gradle 캐싱
   - 환경 변수 설정 (AWS, JCloud)

2. **빌드**
   - `./gradlew clean build -x test`
   - JAR 파일 생성

3. **AWS Elastic Beanstalk 배포**
   - AWS 자격 증명 구성
   - Elastic Beanstalk 환경에 배포
   - 배포 URL: https://d3ka730j70ocy8.cloudfront.net

4. **JCloud 배포 (병렬)**
   - SSH를 통한 JCloud 서버 접속
   - Docker 이미지 빌드 및 배포
   - 배포 URL: https://113.198.66.68:17232

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