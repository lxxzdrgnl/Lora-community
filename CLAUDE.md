# LoRA 모델 공유 플랫폼 개발 가이드

> **프로젝트명**: WSD_Lora_community
> **목표**: 만화/웹툰 캐릭터 LoRA 모델 학습, 공유, 생성을 위한 커뮤니티 플랫폼
> **마지막 업데이트**: 2025-11-12

---

## 📋 목차

1. [프로젝트 개요](#-프로젝트-개요)
2. [개발 환경](#-개발-환경)
3. [시스템 아키텍처](#-시스템-아키텍처)
4. [DB 스키마](#-db-스키마)
5. [API 설계](#-api-설계)
6. [TODO 리스트](#-todo-리스트)
7. [프로젝트 구조](#-프로젝트-구조)
8. [개발 단계](#-개발-단계)
9. [FastAPI 연동](#-fastapi-연동)

---

## 🎯 프로젝트 개요

### 주요 기능

#### 1️⃣ 유저 계정
- OAuth2.0 (Google) 로그인
- 프로필 관리 (닉네임, 생성한 모델 수, 활동 기록)

#### 2️⃣ 모델 생성
- 학습용 데이터 업로드 (만화/웹툰 스크린샷)
- LoRA 자동 학습 (FastAPI 연동)
  - 자동 전처리: 텍스트 제거, 캐릭터 크롭, 512x512 리사이즈
  - 학습 진행률 실시간 추적 (SSE)
- 생성 결과 확인 및 샘플 등록

#### 3️⃣ 모델 정보 관리
- 모델 설명 (캐릭터/스타일 설명, 학습 이미지 수)
- 프롬프트 예시 (JSON 배열 형태)
- 샘플 이미지 등록 (공개/비공개)

#### 4️⃣ 모델 공유 및 탐색
- 공개 모델 검색/조회
- 태그 기반 필터링
- 커뮤니티 피드백 (좋아요, 즐겨찾기)
- 인기 모델 랭킹

#### 5️⃣ 생성 기록 및 관리
- 모델별 생성 이미지 기록
- 생성 파라미터 저장
- 샘플 이미지로 등록 가능

#### 6️⃣ 태그 및 추천
- 스타일/캐릭터 태그 시스템
- 인기 모델 순위, 좋아요 순 정렬

---

## 🛠 개발 환경

### Backend (Spring Boot)
- **Spring Boot**: 3.5.7
- **Java**: 17
- **Build Tool**: Gradle 8.x
- **Database**: MySQL 8.0+
- **ORM**: Spring Data JPA
- **Authentication**: Spring Security + OAuth2 (Google)
- **API Documentation**: SpringDoc OpenAPI (Swagger)

### Frontend (Vue.js)
- **Framework**: Vue.js 3
- **HTTP Client**: Axios
- **State Management**: Pinia
- **UI Framework**: Vuetify 3 / Element Plus

### AI Service (FastAPI)
- **Framework**: FastAPI
- **Deployment**: Modal (멀티인스턴스)
- **ML Framework**: PyTorch, Diffusers
- **Communication**: REST API + SSE (Server-Sent Events)

### Infrastructure
- **Version Control**: Git
- **API Testing**: Postman / Thunder Client
- **Container**: Docker (선택사항)

---

## 🏗 시스템 아키텍처

```
┌─────────────┐      HTTP/REST      ┌──────────────────┐
│   Vue.js    │ ←─────────────────→ │  Spring Boot 3   │
│  Frontend   │                      │    Backend       │
└─────────────┘                      └────────┬─────────┘
                                              │
                                              │ HTTP/REST
                                              │ + SSE
                                              ↓
                                     ┌──────────────────┐
                                     │   FastAPI        │
                                     │   (Modal)        │
                                     │  - LoRA Training │
                                     │  - Image Gen     │
                                     └──────────────────┘
```

### 데이터 흐름

1. **모델 학습 플로우**
   ```
   Vue.js → Spring Boot → FastAPI (학습 시작)
                       ← SSE (진행률 실시간 푸시)
                       ← REST (완료 알림 + 모델 정보)
   ```

2. **이미지 생성 플로우**
   ```
   Vue.js → Spring Boot → FastAPI (생성 시작)
                       ← SSE (step별 진행률)
                       ← REST (이미지 URL 반환)
   ```

---

## 🗄 DB 스키마

### ERD 개요

```
users ──┬── lora_models ──┬── model_samples
        │                 ├── model_prompts
        │                 ├── model_tags
        │                 ├── model_likes
        │                 └── model_favorites
        │
        ├── training_jobs
        ├── generation_history
        ├── comments ──── comment_likes
        └── refresh_tokens
```

### 상세 스키마

#### 1. users (유저)
```sql
CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  email VARCHAR(255) UNIQUE NOT NULL,
  name VARCHAR(100) NOT NULL,
  nickname VARCHAR(50) UNIQUE NOT NULL,
  profile_image_url VARCHAR(500),
  oauth_provider ENUM('GOOGLE') NOT NULL,
  oauth_provider_id VARCHAR(255) UNIQUE NOT NULL,
  role ENUM('USER', 'ADMIN') DEFAULT 'USER' NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
  deleted_at DATETIME,
  INDEX idx_email (email),
  INDEX idx_oauth (oauth_provider, oauth_provider_id)
);
```

#### 2. lora_models (LoRA 모델)
```sql
CREATE TABLE lora_models (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  title VARCHAR(200) NOT NULL,
  description TEXT,
  character_name VARCHAR(100),
  style VARCHAR(100),
  training_images_count INT DEFAULT 0,
  epochs INT,
  learning_rate DECIMAL(10, 8),
  lora_rank INT,
  model_path VARCHAR(500) NOT NULL,
  base_model VARCHAR(200) DEFAULT 'stablediffusionapi/anything-v5',
  is_public BOOLEAN DEFAULT FALSE NOT NULL,
  status ENUM('TRAINING', 'COMPLETED', 'FAILED') DEFAULT 'TRAINING' NOT NULL,
  view_count INT DEFAULT 0,
  like_count INT DEFAULT 0,
  favorite_count INT DEFAULT 0,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
  deleted_at DATETIME,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_user (user_id),
  INDEX idx_public_status (is_public, status),
  INDEX idx_created (created_at DESC)
);
```

#### 3. model_samples (모델 샘플 이미지)
```sql
CREATE TABLE model_samples (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  model_id BIGINT NOT NULL,
  image_url VARCHAR(500) NOT NULL,
  prompt TEXT,
  negative_prompt TEXT,
  steps INT,
  guidance_scale DECIMAL(4, 2),
  seed BIGINT,
  display_order INT DEFAULT 0,
  is_primary BOOLEAN DEFAULT FALSE,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
  FOREIGN KEY (model_id) REFERENCES lora_models(id) ON DELETE CASCADE,
  INDEX idx_model (model_id),
  INDEX idx_primary (model_id, is_primary)
);
```

#### 4. model_prompts (프롬프트 예시)
```sql
CREATE TABLE model_prompts (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  model_id BIGINT NOT NULL,
  title VARCHAR(100) NOT NULL,
  prompt TEXT NOT NULL,
  negative_prompt TEXT,
  description TEXT,
  display_order INT DEFAULT 0,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
  FOREIGN KEY (model_id) REFERENCES lora_models(id) ON DELETE CASCADE,
  INDEX idx_model (model_id)
);
```

#### 5. model_tags (모델 태그)
```sql
CREATE TABLE tags (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) UNIQUE NOT NULL,
  category ENUM('STYLE', 'CHARACTER', 'GENRE', 'OTHER') DEFAULT 'OTHER',
  usage_count INT DEFAULT 0,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
  INDEX idx_name (name),
  INDEX idx_category (category)
);

CREATE TABLE model_tags (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  model_id BIGINT NOT NULL,
  tag_id BIGINT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
  FOREIGN KEY (model_id) REFERENCES lora_models(id) ON DELETE CASCADE,
  FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE,
  UNIQUE KEY unique_model_tag (model_id, tag_id),
  INDEX idx_model (model_id),
  INDEX idx_tag (tag_id)
);
```

#### 6. model_likes (모델 좋아요)
```sql
CREATE TABLE model_likes (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  model_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
  FOREIGN KEY (model_id) REFERENCES lora_models(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  UNIQUE KEY unique_model_like (model_id, user_id),
  INDEX idx_model (model_id),
  INDEX idx_user (user_id)
);
```

#### 7. model_favorites (즐겨찾기)
```sql
CREATE TABLE model_favorites (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  model_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
  FOREIGN KEY (model_id) REFERENCES lora_models(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  UNIQUE KEY unique_model_favorite (model_id, user_id),
  INDEX idx_model (model_id),
  INDEX idx_user (user_id)
);
```

#### 8. training_jobs (학습 작업)
```sql
CREATE TABLE training_jobs (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  model_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  status ENUM('PENDING', 'PREPROCESSING', 'TRAINING', 'SUCCESS', 'FAILED') DEFAULT 'PENDING' NOT NULL,
  current_epoch INT DEFAULT 0,
  total_epochs INT,
  phase VARCHAR(50),
  error_message TEXT,
  started_at DATETIME,
  completed_at DATETIME,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
  FOREIGN KEY (model_id) REFERENCES lora_models(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_model (model_id),
  INDEX idx_user (user_id),
  INDEX idx_status (status)
);
```

#### 9. generation_history (생성 기록)
```sql
CREATE TABLE generation_history (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  model_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  prompt TEXT NOT NULL,
  negative_prompt TEXT,
  steps INT,
  guidance_scale DECIMAL(4, 2),
  seed BIGINT,
  image_url VARCHAR(500) NOT NULL,
  is_sample BOOLEAN DEFAULT FALSE,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
  FOREIGN KEY (model_id) REFERENCES lora_models(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_model (model_id),
  INDEX idx_user (user_id),
  INDEX idx_created (created_at DESC)
);
```

#### 10. comments (댓글)
```sql
CREATE TABLE comments (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  model_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  parent_comment_id BIGINT,
  content TEXT NOT NULL,
  like_count INT DEFAULT 0,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
  deleted_at DATETIME,
  FOREIGN KEY (model_id) REFERENCES lora_models(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (parent_comment_id) REFERENCES comments(id) ON DELETE CASCADE,
  INDEX idx_model (model_id),
  INDEX idx_user (user_id),
  INDEX idx_parent (parent_comment_id)
);
```

#### 11. comment_likes (댓글 좋아요)
```sql
CREATE TABLE comment_likes (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  comment_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
  FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  UNIQUE KEY unique_comment_like (comment_id, user_id),
  INDEX idx_comment (comment_id),
  INDEX idx_user (user_id)
);
```

#### 12. refresh_tokens (리프레시 토큰)
```sql
CREATE TABLE refresh_tokens (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  token VARCHAR(500) UNIQUE NOT NULL,
  expires_at DATETIME NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_user (user_id),
  INDEX idx_token (token)
);
```

---

## 🔌 API 설계

### Base URL
- **Backend**: `http://localhost:8080/api`
- **FastAPI**: `http://localhost:8000`

### 1. 인증 API (`/api/auth`)

| Method | Endpoint | Description | Request | Response |
|--------|----------|-------------|---------|----------|
| GET | `/auth/google` | Google OAuth 로그인 | - | Redirect to Google |
| GET | `/auth/google/callback` | Google OAuth 콜백 | `code` | `{ accessToken, refreshToken, user }` |
| POST | `/auth/refresh` | 토큰 갱신 | `{ refreshToken }` | `{ accessToken }` |
| POST | `/auth/logout` | 로그아웃 | - | `{ message }` |

### 2. 유저 API (`/api/users`)

| Method | Endpoint | Description | Request | Response |
|--------|----------|-------------|---------|----------|
| GET | `/users/me` | 내 프로필 조회 | - | `User` |
| PATCH | `/users/me` | 프로필 수정 | `{ nickname, profileImage }` | `User` |
| GET | `/users/{userId}` | 유저 프로필 조회 | - | `User` |
| GET | `/users/{userId}/models` | 유저 모델 목록 | `page, size` | `Page<LoraModel>` |

### 3. LoRA 모델 API (`/api/models`)

| Method | Endpoint | Description | Request | Response |
|--------|----------|-------------|---------|----------|
| POST | `/models` | 모델 생성 (학습 시작) | `{ title, description, images[], config }` | `{ modelId, jobId }` |
| GET | `/models` | 모델 목록 조회 | `page, size, sort, isPublic, tags[]` | `Page<LoraModel>` |
| GET | `/models/{modelId}` | 모델 상세 조회 | - | `LoraModelDetail` |
| PATCH | `/models/{modelId}` | 모델 정보 수정 | `{ title, description, isPublic }` | `LoraModel` |
| DELETE | `/models/{modelId}` | 모델 삭제 | - | `{ message }` |
| POST | `/models/{modelId}/like` | 좋아요 토글 | - | `{ liked, likeCount }` |
| POST | `/models/{modelId}/favorite` | 즐겨찾기 토글 | - | `{ favorited, favoriteCount }` |
| GET | `/models/{modelId}/samples` | 샘플 이미지 목록 | - | `List<ModelSample>` |
| POST | `/models/{modelId}/samples` | 샘플 이미지 추가 | `{ imageUrl, prompt, ... }` | `ModelSample` |
| DELETE | `/models/{modelId}/samples/{sampleId}` | 샘플 이미지 삭제 | - | `{ message }` |

### 4. 프롬프트 API (`/api/models/{modelId}/prompts`)

| Method | Endpoint | Description | Request | Response |
|--------|----------|-------------|---------|----------|
| GET | `/models/{modelId}/prompts` | 프롬프트 목록 | - | `List<ModelPrompt>` |
| POST | `/models/{modelId}/prompts` | 프롬프트 추가 | `{ title, prompt, negativePrompt, description }` | `ModelPrompt` |
| PATCH | `/models/{modelId}/prompts/{promptId}` | 프롬프트 수정 | `{ title, prompt, ... }` | `ModelPrompt` |
| DELETE | `/models/{modelId}/prompts/{promptId}` | 프롬프트 삭제 | - | `{ message }` |

### 5. 학습 API (`/api/training`)

| Method | Endpoint | Description | Request | Response |
|--------|----------|-------------|---------|----------|
| GET | `/training/jobs/{jobId}` | 학습 작업 상태 조회 | - | `TrainingJob` |
| GET | `/training/jobs/{jobId}/stream` | 학습 진행률 스트림 (SSE) | - | `Server-Sent Events` |
| POST | `/training/jobs/{jobId}/cancel` | 학습 취소 | - | `{ message }` |

### 6. 이미지 생성 API (`/api/generate`)

| Method | Endpoint | Description | Request | Response |
|--------|----------|-------------|---------|----------|
| POST | `/generate` | 이미지 생성 | `{ modelId, prompt, negativePrompt, steps, guidanceScale, numImages }` | `{ jobId }` |
| GET | `/generate/{jobId}/status` | 생성 상태 조회 | - | `{ status, progress, imageUrls[] }` |
| GET | `/generate/{jobId}/stream` | 생성 진행률 스트림 (SSE) | - | `Server-Sent Events` |
| GET | `/generate/history` | 내 생성 기록 | `page, size, modelId` | `Page<GenerationHistory>` |

### 7. 태그 API (`/api/tags`)

| Method | Endpoint | Description | Request | Response |
|--------|----------|-------------|---------|----------|
| GET | `/tags` | 태그 목록 조회 | `category, search` | `List<Tag>` |
| GET | `/tags/popular` | 인기 태그 | `limit` | `List<Tag>` |

### 8. 댓글 API (`/api/models/{modelId}/comments`)

| Method | Endpoint | Description | Request | Response |
|--------|----------|-------------|---------|----------|
| GET | `/models/{modelId}/comments` | 댓글 목록 | `page, size` | `Page<Comment>` |
| POST | `/models/{modelId}/comments` | 댓글 작성 | `{ content, parentCommentId }` | `Comment` |
| PATCH | `/comments/{commentId}` | 댓글 수정 | `{ content }` | `Comment` |
| DELETE | `/comments/{commentId}` | 댓글 삭제 | - | `{ message }` |
| POST | `/comments/{commentId}/like` | 댓글 좋아요 토글 | - | `{ liked, likeCount }` |

### 9. 검색 API (`/api/search`)

| Method | Endpoint | Description | Request | Response |
|--------|----------|-------------|---------|----------|
| GET | `/search/models` | 모델 검색 | `q, tags[], sort, page, size` | `Page<LoraModel>` |
| GET | `/search/users` | 유저 검색 | `q, page, size` | `Page<User>` |

---

## ✅ TODO 리스트

### Phase 1: 프로젝트 초기 설정 (1-2일)

- [ ] **개발 환경 구축**
  - [ ] MySQL 설치 및 데이터베이스 생성
  - [ ] build.gradle 의존성 추가
    - Spring Web
    - Spring Data JPA
    - Spring Security
    - OAuth2 Client
    - MySQL Connector
    - Lombok
    - Validation
    - SpringDoc OpenAPI
  - [ ] application.yml 설정
    - 데이터베이스 연결
    - JPA 설정
    - OAuth2 클라이언트 설정 (Google)
    - 파일 업로드 설정
    - CORS 설정

- [ ] **프로젝트 구조 생성**
  - [ ] 패키지 구조 생성
    - entity, dto, repository, service, controller
    - config, security, exception, util
  - [ ] 공통 응답 DTO 작성
  - [ ] 전역 예외 핸들러 작성
  - [ ] Swagger 설정

### Phase 2: 인증 및 유저 관리 (2-3일)

- [ ] **OAuth2 Google 로그인**
  - [ ] User 엔티티 작성
  - [ ] RefreshToken 엔티티 작성
  - [ ] Spring Security + OAuth2 설정
  - [ ] JWT 토큰 발급/검증 유틸 작성
  - [ ] AuthController 작성
    - Google 로그인 콜백
    - 토큰 갱신
    - 로그아웃

- [ ] **유저 관리**
  - [ ] UserService 작성
  - [ ] UserController 작성
    - 프로필 조회
    - 프로필 수정
    - 유저별 모델 조회

### Phase 3: LoRA 모델 관리 (3-4일)

- [ ] **모델 엔티티 및 기본 CRUD**
  - [ ] LoraModel 엔티티 작성
  - [ ] ModelSample 엔티티 작성
  - [ ] ModelPrompt 엔티티 작성
  - [ ] Tag, ModelTag 엔티티 작성
  - [ ] Repository 작성 (JPA)
  - [ ] ModelService 작성
  - [ ] ModelController 작성
    - 모델 생성
    - 모델 목록 조회 (페이징, 정렬, 필터)
    - 모델 상세 조회
    - 모델 수정/삭제

- [ ] **샘플 이미지 관리**
  - [ ] SampleService 작성
  - [ ] SampleController 작성
  - [ ] 파일 업로드 처리 (로컬 또는 S3)

- [ ] **프롬프트 관리**
  - [ ] PromptService 작성
  - [ ] PromptController 작성

### Phase 4: 좋아요, 즐겨찾기, 댓글 (2-3일)

- [ ] **좋아요/즐겨찾기**
  - [ ] ModelLike 엔티티 작성
  - [ ] ModelFavorite 엔티티 작성
  - [ ] 좋아요/즐겨찾기 토글 로직
  - [ ] 좋아요/즐겨찾기 수 캐싱 (lora_models 테이블)

- [ ] **댓글 시스템**
  - [ ] Comment 엔티티 작성
  - [ ] CommentLike 엔티티 작성
  - [ ] CommentService 작성
  - [ ] CommentController 작성
  - [ ] 대댓글 지원

### Phase 5: FastAPI 연동 - 학습 (3-4일)

- [ ] **학습 작업 관리**
  - [ ] TrainingJob 엔티티 작성
  - [ ] TrainingJobRepository 작성
  - [ ] TrainingService 작성
    - FastAPI `/train` 엔드포인트 호출
    - 학습 작업 생성 및 상태 저장
    - 학습 완료 시 모델 정보 업데이트

- [ ] **학습 진행률 추적**
  - [ ] SSE 컨트롤러 작성 (`/api/training/jobs/{jobId}/stream`)
  - [ ] FastAPI `/train/stream` 연동
  - [ ] 실시간 진행률 브로드캐스트

- [ ] **파일 업로드 처리**
  - [ ] 이미지 업로드 API
  - [ ] FastAPI로 이미지 전송 (Multipart)

### Phase 6: FastAPI 연동 - 이미지 생성 (2-3일)

- [ ] **이미지 생성**
  - [ ] GenerationHistory 엔티티 작성
  - [ ] GenerateService 작성
    - FastAPI `/generate` 호출
    - 생성 기록 저장

- [ ] **생성 진행률 추적**
  - [ ] SSE 컨트롤러 작성 (`/api/generate/{jobId}/stream`)
  - [ ] FastAPI `/generate/stream` 연동
  - [ ] step별 진행률 브로드캐스트

- [ ] **생성 기록 관리**
  - [ ] 생성 기록 조회 API
  - [ ] 샘플로 등록 기능

### Phase 7: 검색 및 태그 (2일)

- [ ] **태그 시스템**
  - [ ] TagService 작성
  - [ ] TagController 작성
  - [ ] 인기 태그 조회

- [ ] **검색 기능**
  - [ ] 모델 검색 (제목, 설명, 태그)
  - [ ] 유저 검색
  - [ ] 정렬 옵션 (인기순, 최신순, 좋아요순)

### Phase 8: 테스트 및 최적화 (2-3일)

- [ ] **단위 테스트**
  - [ ] Service 레이어 테스트
  - [ ] Controller 테스트

- [ ] **성능 최적화**
  - [ ] N+1 쿼리 해결 (Fetch Join)
  - [ ] 인덱스 최적화
  - [ ] 캐싱 적용 (Redis - 선택)

- [ ] **API 문서화**
  - [ ] Swagger 문서 검토
  - [ ] API 예시 추가

### Phase 9: 배포 준비 (선택사항)

- [ ] **Docker 설정**
  - [ ] Dockerfile 작성
  - [ ] docker-compose.yml 작성

- [ ] **환경 분리**
  - [ ] application-dev.yml
  - [ ] application-prod.yml

---

## 📁 프로젝트 구조

```
WSD_Lora_community/
├── src/
│   ├── main/
│   │   ├── java/rheon/wsd_lora_community/
│   │   │   ├── WsdLoraCommunityApplication.java
│   │   │   ├── config/                  # 설정 클래스
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── OAuth2Config.java
│   │   │   │   ├── SwaggerConfig.java
│   │   │   │   ├── WebConfig.java (CORS)
│   │   │   │   └── JpaConfig.java
│   │   │   ├── entity/                  # JPA 엔티티
│   │   │   │   ├── User.java
│   │   │   │   ├── LoraModel.java
│   │   │   │   ├── ModelSample.java
│   │   │   │   ├── ModelPrompt.java
│   │   │   │   ├── Tag.java
│   │   │   │   ├── ModelTag.java
│   │   │   │   ├── ModelLike.java
│   │   │   │   ├── ModelFavorite.java
│   │   │   │   ├── TrainingJob.java
│   │   │   │   ├── GenerationHistory.java
│   │   │   │   ├── Comment.java
│   │   │   │   ├── CommentLike.java
│   │   │   │   └── RefreshToken.java
│   │   │   ├── dto/                     # DTO (요청/응답)
│   │   │   │   ├── request/
│   │   │   │   │   ├── ModelCreateRequest.java
│   │   │   │   │   ├── ModelUpdateRequest.java
│   │   │   │   │   ├── GenerateImageRequest.java
│   │   │   │   │   ├── CommentCreateRequest.java
│   │   │   │   │   └── ...
│   │   │   │   ├── response/
│   │   │   │   │   ├── ApiResponse.java
│   │   │   │   │   ├── UserResponse.java
│   │   │   │   │   ├── LoraModelResponse.java
│   │   │   │   │   ├── TrainingJobResponse.java
│   │   │   │   │   └── ...
│   │   │   │   └── common/
│   │   │   │       ├── PageResponse.java
│   │   │   │       └── ErrorResponse.java
│   │   │   ├── repository/              # JPA Repository
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── LoraModelRepository.java
│   │   │   │   ├── ModelSampleRepository.java
│   │   │   │   ├── ModelPromptRepository.java
│   │   │   │   ├── TagRepository.java
│   │   │   │   ├── ModelTagRepository.java
│   │   │   │   ├── ModelLikeRepository.java
│   │   │   │   ├── ModelFavoriteRepository.java
│   │   │   │   ├── TrainingJobRepository.java
│   │   │   │   ├── GenerationHistoryRepository.java
│   │   │   │   ├── CommentRepository.java
│   │   │   │   ├── CommentLikeRepository.java
│   │   │   │   └── RefreshTokenRepository.java
│   │   │   ├── service/                 # 비즈니스 로직
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── UserService.java
│   │   │   │   ├── LoraModelService.java
│   │   │   │   ├── SampleService.java
│   │   │   │   ├── PromptService.java
│   │   │   │   ├── TrainingService.java
│   │   │   │   ├── GenerationService.java
│   │   │   │   ├── TagService.java
│   │   │   │   ├── CommentService.java
│   │   │   │   ├── LikeService.java
│   │   │   │   ├── FavoriteService.java
│   │   │   │   └── FastApiClient.java (외부 API 호출)
│   │   │   ├── controller/              # REST API 컨트롤러
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── UserController.java
│   │   │   │   ├── LoraModelController.java
│   │   │   │   ├── TrainingController.java
│   │   │   │   ├── GenerationController.java
│   │   │   │   ├── TagController.java
│   │   │   │   ├── CommentController.java
│   │   │   │   └── SearchController.java
│   │   │   ├── security/                # 보안 관련
│   │   │   │   ├── JwtTokenProvider.java
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   ├── OAuth2SuccessHandler.java
│   │   │   │   └── CustomUserDetailsService.java
│   │   │   ├── exception/               # 예외 처리
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── CustomException.java
│   │   │   │   ├── ErrorCode.java
│   │   │   │   └── ...
│   │   │   └── util/                    # 유틸리티
│   │   │       ├── FileUtil.java
│   │   │       ├── ResponseUtil.java
│   │   │       └── ...
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── static/                  # 정적 파일 (이미지 등)
│   └── test/
│       └── java/rheon/wsd_lora_community/
│           ├── service/
│           └── controller/
├── build.gradle
├── settings.gradle
├── CLAUDE.md                            # 이 문서
└── README.md
```

---

## 🚀 개발 단계

### 1️⃣ 초기 설정 및 인증 (Week 1)
- 프로젝트 초기 설정
- OAuth2 Google 로그인 구현
- JWT 토큰 발급/검증
- 유저 프로필 관리

### 2️⃣ 모델 관리 기능 (Week 2)
- LoRA 모델 CRUD
- 샘플 이미지 관리
- 프롬프트 관리
- 태그 시스템

### 3️⃣ FastAPI 연동 (Week 3)
- 학습 작업 연동
- 이미지 생성 연동
- SSE 실시간 진행률
- 파일 업로드/다운로드

### 4️⃣ 커뮤니티 기능 (Week 4)
- 좋아요/즐겨찾기
- 댓글 시스템
- 검색 기능
- 랭킹 시스템

### 5️⃣ 테스트 및 배포 (Week 5)
- 단위 테스트 작성
- 성능 최적화
- API 문서화
- 배포 준비

---

## 🔗 FastAPI 연동

### 학습 API 호출 플로우

```
1. Spring Boot → FastAPI POST /train
   Request: {
     raw_dataset_path: "dataset/",
     output_dir: "my_lora_model",
     skip_preprocessing: false
   }

2. Spring Boot ← FastAPI 200 OK
   Response: {
     message: "Training started in the background..."
   }

3. Spring Boot → FastAPI GET /train/stream (SSE)
   실시간 진행률 수신:
   {
     status: "TRAINING",
     progress: { current_epoch: 50, total_epochs: 250 },
     message: "학습 진행 중... (50/250 에포크 완료)"
   }

4. 학습 완료 시:
   {
     status: "SUCCESS",
     message: "학습이 성공적으로 완료되었습니다."
   }
```

### 이미지 생성 API 호출 플로우

```
1. Spring Boot → FastAPI POST /generate
   Request: {
     prompt: "1girl, black hair, long hair",
     lora_path: "my_lora_model/checkpoint-250",
     num_images: 2,
     steps: 40,
     guidance_scale: 7.5
   }

2. Spring Boot ← FastAPI 200 OK
   Response: {
     message: "Image generation started..."
   }

3. Spring Boot → FastAPI GET /generate/stream (SSE)
   실시간 진행률 수신:
   {
     status: "GENERATING",
     progress: { current_image: 1, total_images: 2, current_step: 20, total_steps: 40 },
     message: "이미지 1/2 생성 중... (step 20/40)"
   }

4. 생성 완료 시:
   {
     status: "SUCCESS",
     message: "이미지 생성 완료 (2개)",
     image_urls: [
       "http://localhost:8000/static/outputs/generated_1.png",
       "http://localhost:8000/static/outputs/generated_2.png"
     ]
   }
```

### FastApiClient 구현 예시

```java
@Service
public class FastApiClient {
    private final RestTemplate restTemplate;
    private final String fastApiUrl = "http://localhost:8000";

    public void startTraining(TrainingRequest request) {
        String url = fastApiUrl + "/train";
        restTemplate.postForObject(url, request, Map.class);
    }

    public Flux<TrainingStatus> streamTrainingStatus(Long jobId) {
        String url = fastApiUrl + "/train/stream";
        // SSE 스트림 처리 (WebFlux 사용)
        return webClient.get()
            .uri(url)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .retrieve()
            .bodyToFlux(TrainingStatus.class);
    }

    public void startGeneration(GenerationRequest request) {
        String url = fastApiUrl + "/generate";
        restTemplate.postForObject(url, request, Map.class);
    }

    public Flux<GenerationStatus> streamGenerationStatus(Long jobId) {
        String url = fastApiUrl + "/generate/stream";
        return webClient.get()
            .uri(url)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .retrieve()
            .bodyToFlux(GenerationStatus.class);
    }
}
```

---

## 📝 개발 시 주의사항

### 1. 보안
- [ ] JWT 시크릿 키를 환경변수로 관리
- [ ] OAuth2 클라이언트 시크릿 보안 유지
- [ ] 파일 업로드 시 확장자/크기 검증
- [ ] CORS 설정 정확히 구성

### 2. 성능
- [ ] N+1 쿼리 방지 (Fetch Join 활용)
- [ ] 페이징 처리 필수
- [ ] 대용량 이미지 처리 최적화
- [ ] 좋아요/조회수 캐싱

### 3. 에러 핸들링
- [ ] 전역 예외 핸들러 구현
- [ ] FastAPI 연동 실패 시 재시도 로직
- [ ] 사용자 친화적 에러 메시지

### 4. 테스트
- [ ] 각 Service 메서드 단위 테스트
- [ ] Controller API 통합 테스트
- [ ] Mock을 활용한 외부 API 테스트

---

## 🔧 필요한 의존성 (build.gradle)

```gradle
dependencies {
    // Spring Boot Starters
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-oauth2-client'
    implementation 'org.springframework.boot:spring-boot-starter-validation'

    // WebFlux (SSE 처리)
    implementation 'org.springframework.boot:spring-boot-starter-webflux'

    // Database
    runtimeOnly 'com.mysql:mysql-connector-j'

    // JWT
    implementation 'io.jsonwebtoken:jjwt-api:0.11.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.11.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.11.5'

    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    // API Documentation
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0'

    // File Upload
    implementation 'commons-io:commons-io:2.11.0'

    // Test
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'
}
```

---

## 📌 참고 자료

### Spring Boot
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Security OAuth2](https://spring.io/guides/tutorials/spring-boot-oauth2/)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)

### FastAPI 연동
- [RestTemplate Guide](https://spring.io/guides/gs/consuming-rest/)
- [WebClient (WebFlux)](https://docs.spring.io/spring-framework/reference/web/webflux-webclient.html)
- [Server-Sent Events](https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events)

### 도구
- [Postman](https://www.postman.com/)
- [Swagger UI](https://swagger.io/tools/swagger-ui/)
- [MySQL Workbench](https://www.mysql.com/products/workbench/)

---

## 💡 다음 단계

1. **MySQL 데이터베이스 생성**
   ```sql
   CREATE DATABASE lora_community CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. **Google OAuth2 클라이언트 ID 발급**
   - [Google Cloud Console](https://console.cloud.google.com/)에서 프로젝트 생성
   - OAuth2 클라이언트 ID 발급
   - Redirect URI: `http://localhost:8080/login/oauth2/code/google`

3. **application.yml 설정**
   - 데이터베이스 연결 정보
   - OAuth2 클라이언트 정보
   - JWT 시크릿 키
   - FastAPI URL

4. **의존성 추가 및 빌드**
   ```bash
   ./gradlew build
   ```

5. **개발 시작!**
   - Phase 1부터 순차적으로 진행
   - 각 단계 완료 시 TODO 체크
   - Git commit 주기적으로

---

**Last Updated**: 2025-11-12
**Author**: Claude Code Assistant
**Project**: WSD_Lora_community
