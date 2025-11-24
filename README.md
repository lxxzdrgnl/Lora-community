# LoRA 모델 공유 플랫폼

> 만화/웹툰 캐릭터 LoRA 모델 학습, 생성, 공유를 위한 커뮤니티 플랫폼

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![Java](https://img.shields.io/badge/Java-17-007396?style=flat-square&logo=openjdk&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-8.14-02303A?style=flat-square&logo=gradle&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-OAuth2%20%2B%20JWT-6DB33F?style=flat-square&logo=springsecurity&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger-API%20Docs-85EA2D?style=flat-square&logo=swagger&logoColor=black)
![AWS Elastic Beanstalk](https://img.shields.io/badge/AWS%20Elastic%20Beanstalk-Deployed-232F3E?style=flat-square&logo=aws&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)
![Status](https://img.shields.io/badge/Status-Active-success?style=flat-square)

---

## 소개

만화/웹툰 캐릭터에 특화된 LoRA 모델을 학습하고, AI 이미지를 생성하며, 커뮤니티와 공유할 수 있는 통합 플랫폼입니다.

### 주요 기능

- **LoRA 모델 학습**: FastAPI 기반 분산 학습 시스템
- **AI 이미지 생성**: 실시간 진행률 모니터링
- **커뮤니티**: 댓글, 좋아요, 즐겨찾기, 인기 랭킹
- **스마트 검색**: 태그 필터링, 키워드 검색
- **간편 로그인**: Google OAuth2 인증

### 주요 링크
- **프론트엔드 배포 링크**: http://blueming-front.s3-website.ap-northeast-2.amazonaws.com/
- **백엔드 API 기본 URL**: http://blueming-ai-env.eba-gdfew9bx.ap-northeast-2.elasticbeanstalk.com/
- **백엔드 API 문서 (Swagger UI)**: http://blueming-ai-env.eba-gdfew9bx.ap-northeast-2.elasticbeanstalk.com//index.html
- **프론트엔드 Github**: https://github.com/lxxzdrgnl/LoRA-Platform-Front
- **AI서버 Github** : https://github.com/lxxzdrgnl/Lora-training-api
### 시스템 아키텍처

```
┌─────────────┐      ┌───────────────────────────┐      ┌─────────────────┐
│   Vue.js    │ <--> │ AWS Elastic Beanstalk     │ <--> │    FastAPI      │
│  (Frontend) │      │ (Spring Boot 3 Backend)   │      │  (AI Service)   │
└─────────────┘      └───────────┬───────────────┘      └─────────────────┘
                                 │
                                 v
                        ┌─────────────────┐
                        │   AWS RDS (DB)  │
                        └─────────────────┘
```
---

## 기술 스택

### Backend
- **Framework**: Spring Boot 3.5.7
- **Language**: Java 17
- **Build Tool**: Gradle 8.14.3
- **ORM**: Spring Data JPA

### Security & Auth
- **Authentication**: OAuth2 (Google) + JWT
- **Authorization**: Spring Security
- **Session Management**: Stateless (멀티 인스턴스 지원)

### Database
- **Development**: H2 (In-memory)
- **Production**: MySQL 8.0+ on AWS RDS

### API & Communication
- **API Documentation**: SpringDoc OpenAPI 3 (Swagger UI)
- **Real-time Communication**: Server-Sent Events (SSE)
- **Async HTTP Client**: Spring WebFlux WebClient

### AI Service Integration
- **Framework**: FastAPI (Modal Serverless)
- **GPU**: Modal A10G (Training), T4 (Generation)
- **Communication**: Reactive Streams (Mono, Flux)
- **Progress Tracking**: SSE Streaming
- **Storage**: AWS S3 (Models, Images, Datasets)

---

## 프로젝트 구조

```
src/main/java/rheon/wsd_lora_community/
├── WsdLoraCommunityApplication.java
├── global/                      # 공통 기능
│   ├── config/                 # Security, JPA, Swagger, CORS
│   ├── security/               # JWT, OAuth2, Filter
│   ├── client/                 # FastAPI HTTP Client
│   ├── controller/             # 통합 검색 API
│   ├── exception/              # 전역 예외 처리
│   └── dto/                    # 공통 DTO
├── user/                        # 유저 관리
│   ├── entity/                 # User, RefreshToken
│   ├── repository/
│   ├── service/                # 인증, 프로필 관리
│   ├── controller/
│   └── dto/
├── model/                       # LoRA 모델
│   ├── entity/                 # LoraModel, ModelSample, Tag
│   ├── repository/
│   ├── service/                # 모델, 샘플, 프롬프트, 태그 관리
│   ├── controller/
│   └── dto/
├── community/                   # 커뮤니티
│   ├── entity/                 # Comment, Like, Favorite
│   ├── repository/
│   ├── service/                # 댓글, 좋아요, 즐겨찾기
│   ├── controller/
│   └── dto/
├── training/                    # 학습 관리
│   ├── entity/                 # TrainingJob
│   ├── repository/
│   ├── service/                # 학습 작업 관리
│   ├── controller/             # FastAPI 연동
│   └── dto/
└── generation/                  # 이미지 생성
    ├── entity/                 # GenerationHistory
    ├── repository/
    ├── service/                # 생성 기록 관리
    ├── controller/             # FastAPI 연동
    └── dto/
```

---

## 시작하기

### 요구사항

- Java 17 이상
- Gradle 8.14 이상

### 로컬 환경에서 빌드

```bash
# 프로젝트 클론
git clone <repository-url>
cd WSD_Lora_community

# 빌드
./gradlew clean build -x test

# 로컬 실행
./gradlew bootRun
```

---
### 헬스 체크

```bash
GET /
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

### 주요 엔드포인트

#### 인증
- `GET /oauth2/authorization/google` - Google OAuth2 로그인
- `POST /api/auth/refresh` - 액세스 토큰 갱신
- `POST /api/auth/logout` - 로그아웃
- `GET /api/auth/me` - 현재 유저 정보 조회

#### 유저
- `GET /api/users/me` - 내 프로필 조회
- `PUT /api/users/me` - 내 프로필 수정
- `GET /api/users/{userId}` - 유저 프로필 조회
- `GET /api/users/search` - 유저 검색

#### LoRA 모델
- `POST /api/models` - 모델 생성
- `GET /api/models` - 공개 모델 목록 조회
- `GET /api/models/{modelId}` - 모델 상세 조회
- `PUT /api/models/{modelId}` - 모델 수정
- `DELETE /api/models/{modelId}` - 모델 삭제
- `GET /api/models/search` - 모델 검색
- `POST /api/models/{modelId}/samples` - 샘플 이미지 추가
- `POST /api/models/{modelId}/prompts` - 프롬프트 추가

#### 커뮤니티
- `POST /api/models/{modelId}/comments` - 댓글 작성
- `GET /api/models/{modelId}/comments` - 댓글 목록 조회
- `POST /api/models/{modelId}/like` - 모델 좋아요 토글
- `POST /api/models/{modelId}/favorite` - 즐겨찾기 토글
- `POST /api/models/{modelId}/comments/{commentId}/like` - 댓글 좋아요 토글

#### 태그
- `GET /api/tags` - 전체 태그 조회
- `GET /api/tags/popular` - 인기 태그 조회
- `GET /api/tags/category/{category}` - 카테고리별 태그 조회
- `POST /api/tags/models/{modelId}` - 모델에 태그 추가

#### 학습 (Modal GPU)
- `POST /api/training/upload-urls` - 학습 이미지 업로드 URL 생성
- `POST /api/training/models/{modelId}` - 학습 작업 생성
- `POST /api/training/jobs/{jobId}/start` - 학습 시작 (Modal GPU A10G)
- `GET /api/training/jobs/{jobId}` - 학습 작업 조회
- `GET /api/training/my` - 내 학습 작업 목록
- `POST /api/training/callback` - Modal 학습 완료 콜백
- `GET /api/training/stream` - 실시간 진행률 스트리밍 (SSE)
- `GET /api/training/fastapi/status` - Modal 학습 상태 조회

#### 이미지 생성 (Modal GPU)
- `POST /api/generate` - 이미지 생성 요청 (Modal GPU T4)
- `POST /api/generate/history` - Modal 생성 완료 콜백
- `GET /api/generate/history` - 생성 기록 조회
- `POST /api/generate/history/{historyId}/sample` - 샘플로 등록
- `GET /api/generate/stream` - 실시간 진행률 스트리밍 (SSE)
- `GET /api/generate/fastapi/status` - Modal 생성 상태 조회

#### 검색
- `GET /api/search` - 통합 검색 (모델 + 유저)
- `GET /api/search/models` - 모델 검색
- `GET /api/search/models/tags` - 태그로 모델 필터링
- `GET /api/search/models/popular` - 인기 모델 조회
- `GET /api/search/users` - 유저 검색

---

## 인증 방식

### JWT 기반 Stateless 인증

1. **Google OAuth2 로그인**
   ```
   GET /oauth2/authorization/google
   ```

2. **토큰 발급**
   - Access Token: 1시간 유효
   - Refresh Token: 7일 유효

3. **API 호출**
   ```
   Authorization: Bearer {accessToken}
   ```

4. **토큰 갱신**
   ```
   POST /api/auth/refresh
   Content-Type: application/json

   {
     "refreshToken": "{refreshToken}"
   }
   ```

### Stateless 설계의 장점

- JWT 기반 인증으로 서버 메모리 세션 불필요
- Refresh Token만 DB 저장
- 멀티 인스턴스 환경에서 수평 확장 가능
- 로드밸런서 없이도 인스턴스 간 세션 공유

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

## 프로덕션 환경 설정

### 환경 변수 설정

실행 환경에 따라 다음 환경 변수를 설정해야 합니다.

```properties
# Database (RDS or other MySQL)
spring.datasource.url=jdbc:mysql://<your-db-host>:<your-db-port>/<your-db-name>
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# JWT Secret
jwt.secret=${JWT_SECRET}

# OAuth2 Google
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}

# Modal API URL
fastapi.base-url=${FASTAPI_URL:https://dldydwo9--lora-training-inference-fastapi-app.modal.run}

# AWS S3
aws.s3.region=${AWS_S3_REGION:ap-southeast-2}
aws.s3.buckets.training-data=${AWS_S3_TRAINING_BUCKET:lora-training-data-bucket}
aws.s3.buckets.models=${AWS_S3_MODELS_BUCKET:lora-models-bucket}
aws.s3.buckets.generated-images=${AWS_S3_GENERATED_BUCKET:lora-generated-image-bucket}
aws.credentials.access-key=${AWS_ACCESS_KEY_ID}
aws.credentials.secret-key=${AWS_SECRET_ACCESS_KEY}

# Modal Settings
modal.enabled=${MODAL_ENABLED:true}
modal.app-url=${MODAL_APP_URL:https://dldydwo9--lora-training-inference-fastapi-app.modal.run}
```