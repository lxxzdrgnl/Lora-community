# LoRA 모델 공유 플랫폼 개발 가이드

> **프로젝트명**: WSD_Lora_community
> **현재 진행률**: Phase 2 완료 - 모든 Service 작성 완료 ✅

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
│   └── dto/                  # 공통 DTO (ApiResponse, PageResponse, ErrorResponse, BaseEntity)
├── user/                     # 유저 도메인
│   ├── entity/              # User, RefreshToken
│   ├── repository/          # UserRepository, RefreshTokenRepository
│   ├── service/             # ✅ UserService, AuthService
│   ├── controller/          # ❌ (미작성)
│   └── dto/                 # ✅ UserResponse, UserUpdateRequest
├── model/                    # LoRA 모델 도메인
│   ├── entity/              # LoraModel, ModelSample, ModelPrompt, Tag, ModelTag
│   ├── repository/          # 5개 Repository (일부 메서드 추가 필요)
│   ├── service/             # ✅ LoraModelService (작성 완료)
│   ├── controller/          # ❌ (미작성)
│   └── dto/                 # ✅ 10개 DTO (Request 5개, Response 5개)
├── community/                # 커뮤니티 도메인
│   ├── entity/              # Comment, CommentLike, ModelLike, ModelFavorite
│   ├── repository/          # 4개 Repository
│   ├── service/             # ❌ (미작성)
│   ├── controller/          # ❌ (미작성)
│   └── dto/                 # ✅ CommentCreateRequest, CommentResponse
├── training/                 # 학습 도메인
│   ├── entity/              # TrainingJob
│   ├── repository/          # TrainingJobRepository
│   ├── service/             # ❌ (미작성)
│   ├── controller/          # ❌ (미작성)
│   └── dto/                 # ✅ TrainingJobResponse
└── generation/               # 생성 도메인
    ├── entity/              # GenerationHistory
    ├── repository/          # GenerationHistoryRepository
    ├── service/             # ❌ (미작성)
    ├── controller/          # ❌ (미작성)
    └── dto/                 # ✅ GenerateImageRequest, GenerationHistoryResponse
```

#### 2. 작성 완료된 파일
- **Entity**: 14개 (모두 완료)
- **Repository**: 13개 (기본 완료, 일부 메서드 추가 필요)
- **Config**: 5개 (완료)
- **Security**: 4개 (완료)
- **Exception**: 3개 (완료, ErrorCode 2개 추가됨)
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

---

## 🚧 다음 작업 (우선순위 순)

### Phase 3: Controller 작성 (3-4시간)
1. `user/controller/AuthController.java` - `/api/auth`
2. `user/controller/UserController.java` - `/api/users`
3. `model/controller/LoraModelController.java` - `/api/models`
4. `model/controller/TagController.java` - `/api/tags`
5. `community/controller/CommentController.java` - `/api/models/{id}/comments`
6. `training/controller/TrainingController.java` - `/api/training`
7. `generation/controller/GenerationController.java` - `/api/generate`
8. `global/controller/SearchController.java` - `/api/search`

### Phase 4: FastAPI 클라이언트 (2-3시간)
- `global/client/FastApiClient.java` - HTTP 클라이언트
- SSE 스트리밍 구현

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
