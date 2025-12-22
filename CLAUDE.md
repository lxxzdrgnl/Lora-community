# LoRA 모델 공유 플랫폼 개발 가이드

> **프로젝트명**: WSD_Lora_community
> **현재 진행률**: Phase 8 완료 - FastAPI 즉시 콜백 + SSE 실시간 전송 완료 ✅

---

## ⚠️ 개발 규칙 (최우선)

**Claude Code는 다음 작업을 절대 수행하지 않습니다:**
1. ❌ **빌드 명령 실행 금지**: `./gradlew build`, `./gradlew clean build` 등
2. ❌ **실행 명령 실행 금지**: `./gradlew bootRun` 등
3. ❌ **Git 명령 실행 금지**: `git add`, `git commit`, `git push` 등

**이유**: 사용자가 직접 빌드/실행/커밋을 관리합니다.

**Claude Code의 역할**: 코드 작성, 수정, 분석, 문제 해결만 수행합니다.

---

## ⚡ 현재 상태

### ✅ 완료된 작업

#### 1. Package-by-Feature 구조로 전환 완료
```
src/main/java/rheon/wsd_lora_community/
├── WsdLoraCommunityApplication.java
├── global/                    # 공통 기능
│   ├── config/               # 설정 (Security, JPA, Swagger, Web, CORS)
│   ├── security/             # JWT, OAuth2, Filter
│   ├── exception/            # 전역 예외 처리
│   ├── client/              # ✅ FastApiClient (HTTP 클라이언트, SSE 스트림)
│   ├── controller/          # ✅ SearchController
│   └── dto/                  # 공통 DTO (ApiResponse, PageResponse, ErrorResponse, BaseEntity)
├── user/                     # 유저 도메인
│   ├── entity/              # User, RefreshToken
│   ├── repository/          # UserRepository, RefreshTokenRepository
│   ├── service/             # ✅ UserService, AuthService
│   ├── controller/          # ✅ AuthController, UserController
│   └── dto/                 # ✅ UserResponse, UserUpdateRequest
├── model/                    # LoRA 모델 도메인
│   ├── entity/              # LoraModel, ModelSample, ModelPrompt, Tag, ModelTag
│   ├── repository/          # 5개 Repository
│   ├── service/             # ✅ LoraModelService, SampleService, PromptService, TagService
│   ├── controller/          # ✅ LoraModelController, TagController
│   └── dto/                 # ✅ 10개 DTO (Request 5개, Response 5개)
├── community/                # 커뮤니티 도메인
│   ├── entity/              # Comment, CommentLike, ModelLike, ModelFavorite
│   ├── repository/          # 4개 Repository
│   ├── service/             # ✅ CommentService, LikeService, FavoriteService
│   ├── controller/          # ✅ CommentController
│   └── dto/                 # ✅ CommentCreateRequest, CommentResponse
├── training/                 # 학습 도메인
│   ├── entity/              # TrainingJob
│   ├── repository/          # TrainingJobRepository
│   ├── service/             # ✅ TrainingService
│   ├── controller/          # ✅ TrainingController
│   └── dto/                 # ✅ TrainingJobResponse
└── generation/               # 생성 도메인
    ├── entity/              # GenerationHistory
    ├── repository/          # GenerationHistoryRepository
    ├── service/             # ✅ GenerationService
    ├── controller/          # ✅ GenerationController
    └── dto/                 # ✅ GenerateImageRequest, GenerationHistoryResponse
```

#### 2. 작성 완료된 파일
- **Entity**: 14개 (모두 완료)
- **Repository**: 13개 (모두 완료)
- **Config**: 5개 (완료)
- **Security**: 4개 (완료)
- **Exception**: 3개 (완료)
- **DTO**: 20개 (Request 8개, Response 9개, Common 3개)
- **Service**: 11개 완료
  - ✅ `user/service/UserService.java` - 프로필 CRUD, 검색
  - ✅ `user/service/AuthService.java` - 토큰 갱신, 로그아웃
  - ✅ `model/service/LoraModelService.java` - 모델 CRUD, 검색, 필터링
  - ✅ `model/service/SampleService.java` - 샘플 이미지 관리
  - ✅ `model/service/PromptService.java` - 프롬프트 CRUD
  - ✅ `model/service/TagService.java` - 태그 관리
  - ✅ `community/service/CommentService.java` - 댓글 CRUD
  - ✅ `community/service/LikeService.java` - 좋아요 토글
  - ✅ `community/service/FavoriteService.java` - 즐겨찾기 토글
  - ✅ `training/service/TrainingService.java` - 학습 작업 관리
  - ✅ `generation/service/GenerationService.java` - 이미지 생성 관리
- **Controller**: 8개 완료
  - ✅ `user/controller/AuthController.java` - `/api/auth` (토큰 갱신, 로그아웃)
  - ✅ `user/controller/UserController.java` - `/api/users` (프로필 관리)
  - ✅ `model/controller/LoraModelController.java` - `/api/models` (모델 CRUD, 샘플, 프롬프트)
  - ✅ `model/controller/TagController.java` - `/api/tags` (태그 관리)
  - ✅ `community/controller/CommentController.java` - `/api/models/{id}/comments` (댓글, 좋아요, 즐겨찾기)
  - ✅ `training/controller/TrainingController.java` - `/api/training` (학습 작업)
  - ✅ `generation/controller/GenerationController.java` - `/api/generate` (이미지 생성)
  - ✅ `global/controller/SearchController.java` - `/api/search` (통합 검색)

#### 3. Phase 1: Repository 메서드 추가 완료 ✅
- **추가된 메서드**:
  - `LoraModelRepository`: findByIsPublicTrueAndStatusOrderByCreatedAtDesc, findByIsPublicTrueAndStatusOrderByLikeCountDesc
  - `ModelPromptRepository`: findByModelIdOrderByDisplayOrder
  - `ModelTagRepository`: findByModelId
  - `TagRepository`: findByNameIn
  - `ModelLikeRepository`: existsByModelIdAndUserId
  - `ModelFavoriteRepository`: existsByModelIdAndUserId

#### 4. Phase 2: Service 작성 완료 ✅
- **Status**: 빌드 성공 ✅
- **작성된 Service**:
  - `SampleService` - 샘플 이미지 CRUD, 대표 이미지 설정, 표시 순서 관리
  - `PromptService` - 프롬프트 예시 CRUD, 표시 순서 관리
  - `TagService` - 태그 생성/조회, 모델-태그 연결, 인기 태그, 카테고리별 조회
  - `CommentService` - 댓글/대댓글 CRUD, 좋아요 토글, 페이징 조회
  - `LikeService` - 모델 좋아요 토글, 좋아요 여부 확인
  - `FavoriteService` - 모델 즐겨찾기 토글, 즐겨찾기 목록 조회
  - `TrainingService` - 학습 작업 생성/시작/진행률 업데이트/완료/실패 처리
  - `GenerationService` - 이미지 생성 기록 저장/조회, 샘플 등록/해제
- **추가된 ErrorCode**:
  - RESOURCE_NOT_FOUND (COMMON_007)
  - DUPLICATE_RESOURCE (COMMON_008)
- **추가된 Entity 메서드**:
  - `LoraModel`: updateStatus(), updateModelFileUrl()

#### 5. Phase 3: Controller 작성 완료 ✅
- **Status**: 빌드 성공 ✅
- **작성된 Controller**:
  - `AuthController` - 토큰 갱신, 로그아웃, 현재 유저 정보 조회
  - `UserController` - 프로필 조회/수정, 유저 검색
  - `LoraModelController` - 모델 CRUD, 검색, 태그 필터링, 샘플/프롬프트 관리
  - `TagController` - 태그 조회, 인기 태그, 카테고리별 조회, 모델-태그 연결
  - `CommentController` - 댓글 CRUD, 댓글 좋아요, 모델 좋아요/즐겨찾기
  - `TrainingController` - 학습 작업 생성/시작/진행률 업데이트/완료/실패 처리
  - `GenerationController` - 이미지 생성 요청, 생성 기록 조회/관리
  - `SearchController` - 통합 검색, 모델/유저 검색, 태그 필터링

#### 6. Phase 4: FastAPI 클라이언트 작성 완료 ✅
- **Status**: 빌드 성공 ✅
- **작성된 파일**:
  - `global/client/FastApiClient.java` - FastAPI 서버 통신 클라이언트
- **주요 기능**:
  - **학습 API**: startTraining(), getTrainingStatus(), streamTrainingStatus() (SSE)
  - **이미지 생성 API**: startImageGeneration(), getGenerationStatus(), streamGenerationStatus() (SSE)
  - **생성 이미지 조회**: getGeneratedImageUrls()
  - **서버 상태 확인**: checkServerHealth()
- **기술 스택**:
  - Spring WebFlux WebClient (비동기 HTTP 클라이언트)
  - Server-Sent Events (SSE) 스트리밍 지원
  - Reactive Streams (Mono, Flux)

#### 7. Phase 5: Controller와 FastAPI 연동 완료 ✅
- **Status**: 빌드 성공 ✅
- **연동 완료**:
  - `TrainingController` - FastAPIClient로 학습 시작 요청 전송 (비동기)
  - `GenerationController` - FastAPIClient로 이미지 생성 요청 전송 (비동기)
  - `GenerationService` - validateGenerationRequest()가 모델 경로 반환하도록 수정
- **추가된 엔드포인트**:
  - `GET /api/training/stream` - 학습 진행률 SSE 스트리밍
  - `GET /api/training/fastapi/status` - FastAPI 학습 상태 조회
  - `GET /api/generate/stream` - 이미지 생성 진행률 SSE 스트리밍
  - `GET /api/generate/fastapi/status` - FastAPI 생성 상태 조회
  - `GET /api/generate/fastapi/images` - 생성된 이미지 URL 목록 조회
- **주요 특징**:
  - 비동기 처리: .subscribe()를 사용한 non-blocking 방식
  - 에러 처리: FastAPI 요청 실패 시 TrainingService.failTraining() 자동 호출
  - 실시간 모니터링: SSE를 통한 진행률 실시간 스트리밍

#### 8. Phase 6: Google OAuth 통합 및 프론트엔드 연동 완료 ✅
- **Status**: 빌드 성공 ✅
- **OAuth2 설정**:
  - Google Cloud Console에서 OAuth2 Client ID 생성
  - `application-local.yml`에 클라이언트 정보 저장 (.gitignore에 추가)
  - `OAuth2SuccessHandler` 수정: 프론트엔드로 JWT 토큰 전달 (URL Fragment 방식)
  - Redirect URI: `http://localhost:8080/login/oauth2/code/google`
  - Frontend Callback: `http://localhost:5173/auth/callback`
- **인증 흐름**:
  1. 사용자가 "Login with Google" 클릭 → `/api/auth/google` 호출
  2. Spring Security가 Google OAuth2 인증 페이지로 리다이렉트
  3. 사용자 인증 후 Google이 `/login/oauth2/code/google`로 콜백
  4. `OAuth2SuccessHandler`가 JWT 토큰 생성 후 프론트엔드로 리다이렉트
  5. 프론트엔드가 URL Fragment에서 토큰 추출 후 LocalStorage 저장
  6. 이후 모든 API 요청에 `Authorization: Bearer {token}` 헤더 포함
- **Dual Authentication 지원**:
  - `UserController.getUserIdFromAuthentication()` 메서드 추가
  - OAuth2User (Google 로그인) 및 UserDetails (JWT 인증) 모두 지원
  - Principal 타입에 따라 자동으로 User ID 추출
- **CORS 설정**:
  - `SecurityConfig`에 `.cors(cors -> cors.configure(http))` 추가
  - 프론트엔드 (`http://localhost:5173`)에서 백엔드 API 호출 허용
- **보안 설정**:
  - JWT Secret 키 환경변수화
  - Google Client Secret 환경변수화
  - `application-local.yml` .gitignore 추가 (민감정보 보호)
  - Frontend URL 설정 (`frontend.url`)

#### 9. Phase 7: FastAPI 콜백 처리 개선 완료 ✅
- **Status**: 빌드 성공 ✅
- **문제 해결**:
  - `/api/training/callback` 엔드포인트를 `permitAll()` 목록에 추가
  - FastAPI가 jobId 없이 SUCCESS/FAIL 콜백을 보낼 때 userId로 진행 중인 작업 자동 검색
  - `handleTrainingSuccess`, `failTraining`의 상태 체크 완화 (이미 완료/실패한 경우 처리)
- **개선 사항**:
  - 명확한 로그 출력 (⚠️ 경고, ✅ 성공, ❌ 에러)
  - SUCCESS/FAIL 콜백 에러 처리 강화 (try-catch 추가)
  - 중복 완료/실패 방지 로직 추가

#### 10. Phase 8: 실시간 진행률 전송 및 즉시 콜백 완료 ✅
- **Status**: 빌드 성공 ✅
- **FastAPI 수정** (`/home/rheon/Desktop/Study/lora/main.py`):
  - `GenerateRequest`에 `model_id`, `history_id`, `lora_scale`, `callback_url` 필드 추가
  - `run_generation_task()` 함수 수정:
    - Redis 클라이언트를 통해 **실시간 진행률 전송** (Pub/Sub)
    - 생성 완료 즉시 **HTTP 콜백을 Spring Boot로 전송** (큐 없이 즉시 전송)
    - 실패 시에도 콜백 전송
  - 로그: `✅ Success callback sent: status=200` 출력
- **Spring Boot 수정**:
  - `JobCallbackService.java`에 **SSE 전송 로직 추가**:
    - `handleGenerationProgress()`: 진행률 SSE 전송 (`generation_progress` 이벤트)
    - `handleGenerationSuccess()`: 완료 SSE 전송 (이미지 URL 포함)
    - `handleGenerationFailure()`: 실패 SSE 전송
    - `handleTrainingProgress()`: 학습 진행률 SSE 전송 (`training_progress` 이벤트)
    - `handleTrainingSuccess()`, `handleTrainingFailure()`: 학습 완료/실패 SSE 전송
  - `SseEmitterService.sendToUser()` 호출하여 **실시간 프론트엔드 알림**
  - `JwtAuthenticationFilter.java` 수정: URL 파라미터 `?token=...`로도 JWT 인증 가능 (SSE 용)
- **Redis 하이브리드 방식**:
  1. FastAPI → Redis Pub/Sub (실시간) + 캐싱 (TTL 1시간)
  2. FastAPI → Spring Boot HTTP 콜백 (DB 업데이트)
  3. Spring Boot → 프론트엔드 SSE 전송 (실시간 UI 업데이트)
- **이제 프론트엔드에서 진행률을 실시간으로 받을 수 있습니다!** 🎉

---

#### 11. Phase 9: 프론트엔드 History 폴링 및 인증 개선 ✅
- **Status**: 프론트엔드 수정 완료 ✅
- **Profile.vue 수정** (`/home/rheon/Desktop/Study/LoRA-Platform-Front/src/views/Profile.vue`):
  - **폴링 로직 추가**: 진행 중인 작업(`GENERATING`, `TRAINING`)이 있으면 3초마다 히스토리 리프레시
  - `startPolling()`: 폴링 시작, History 탭에서만 동작
  - `stopPolling()`: 진행 중인 작업이 없으면 자동 중지
  - `closeGenerateModal()`: 생성 완료 후 히스토리 리프레시 및 폴링 시작
- **GenerationController.java 수정**:
  - `streamGenerationProgress()`: Authentication null 체크 추가
  - 로그인하지 않으면 `UNAUTHORIZED` 에러 반환
- **JWT 토큰 필수**:
  - SSE는 userId로 메시지를 필터링하므로 JWT 토큰 필수
  - 프론트엔드에서 `?token=...` URL 파라미터로 전달

---

## 🚧 다음 작업 (우선순위 순)

### Phase 10: 테스트 및 배포 준비
1. FastAPI 서버 실행 후 통합 테스트
2. Swagger UI 문서 검증
3. 프로덕션 환경 설정 (MySQL, S3, 환경변수)
4. Docker Compose 구성
5. CI/CD 파이프라인 구축

---

## 🔧 실행 방법

### 빌드
```bash
cd /home/rheon/Desktop/Semester/3-2/WSD/WSD_Lora_community
./gradlew clean build -x test
```

### 실행
```bash
./gradlew bootRun
```

### 접속
- **API 서버**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2 Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:loradb`
  - Username: `sa`
  - Password: (공백)

---

## 📝 주요 변경 사항

### Package-by-Feature 구조로 전환한 이유
- 도메인별 응집도 향상
- 도메인 독립적 개발 가능
- 코드 탐색 및 유지보수 용이

### DTO 타입 변경
- `guidanceScale`, `learningRate`: `Double` → `BigDecimal` (정확한 소수점 계산)

### 추가된 ErrorCode
- `REFRESH_TOKEN_NOT_FOUND` (AUTH_005)
- `REFRESH_TOKEN_EXPIRED` (AUTH_006)
- `NICKNAME_ALREADY_EXISTS` (USER_004)

### 추가된 Entity 메서드
**LoraModel**:
- `updateTitle()`, `updateDescription()`, `updateCharacterName()`, `updateStyle()`, `updateIsPublic()`

---

## 📚 기술 스택

### Backend
- **Spring Boot**: 3.5.7
- **Java**: 17
- **Build Tool**: Gradle 8.x
- **Database**: H2 (dev), MySQL 8.0+ (prod)
- **ORM**: Spring Data JPA
- **Authentication**: Spring Security + OAuth2 (Google) + JWT
- **API Docs**: SpringDoc OpenAPI 3

### Frontend (예정)
- Vue.js 3 + Vuetify/Element Plus

### AI Service (예정)
- FastAPI + Modal (멀티인스턴스)

---

## 🎯 핵심 기능

1. **유저 관리**: OAuth2 Google 로그인, 프로필 관리
2. **모델 관리**: LoRA 모델 CRUD, 검색, 태그 필터링
3. **커뮤니티**: 좋아요, 즐겨찾기, 댓글 (대댓글 지원)
4. **학습**: FastAPI 연동, 실시간 진행률 (SSE)
5. **생성**: 이미지 생성, 생성 기록 관리

---

## 📌 개발 가이드

### Service 작성 시 주의사항
1. `@Transactional(readOnly = true)` 클래스 레벨 적용
2. Write 메서드에만 `@Transactional` 적용
3. `CustomException` + `ErrorCode` 사용
4. Entity 메서드를 통한 상태 변경 (Setter 사용 금지)

### Controller 작성 시 주의사항
1. `ApiResponse<T>` 래퍼로 응답 통일
2. `@PreAuthorize("isAuthenticated()")` 인증 필요 엔드포인트
3. Swagger 어노테이션 (`@Tag`, `@Operation`) 추가
4. `@Valid` 요청 검증

### Repository 작성 시 주의사항
1. Spring Data JPA 메서드 네이밍 규칙 준수
2. 복잡한 쿼리는 `@Query` 사용
3. Soft delete 조건 (deletedAt IS NULL) 추가

---

## 🔗 주요 참고 자료

- [Spring Boot Docs](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Security OAuth2](https://spring.io/guides/tutorials/spring-boot-oauth2/)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)

---

**Author**: Claude Code Assistant
**Project**: WSD_Lora_community


todo
옵션 1: 나머지 Controller도 AuthenticationUtil 사용

다음 Controller들에도 AuthenticationUtil을 적용하면 좋습니다:
- UserController
- AuthController
- LoraModelController
- TagController
- SearchController
- CommentController
- UploadController

옵션 2: Import 정리

불필요한 import 제거:
- TrainingController: GenerationProgressHandler, GpuResourceManager, TrainingJob, HashMap 등
- GenerationController: GenerationProgressHandler, GpuResourceManager, HashMap 등