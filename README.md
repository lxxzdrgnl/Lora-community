# LoRA 모델 공유 플랫폼 - Backend (Spring Boot)

> 만화/웹툰 캐릭터 LoRA 모델 학습, 생성, 공유를 위한 백엔드 API 서버

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## 📌 프로젝트 개요

이 프로젝트는 만화/웹툰 캐릭터를 학습시킨 LoRA 모델을 공유하고, 이미지를 생성할 수 있는 커뮤니티 플랫폼의 백엔드 서버입니다.

### 시스템 구성

```
Vue.js (Frontend) ↔ Spring Boot 3 (Backend) ↔ FastAPI (LoRA Training/Generation)
                                    ↕
                              H2 / AWS RDS (Database)
```

---

## 🚀 현재 구현 상태

### ✅ Phase 1: 초기 설정 완료

- [x] **프로젝트 구조 생성**
  - Spring Boot 3.5.7 + Java 17
  - Gradle 8.14.3 빌드 도구
  - H2 인메모리 데이터베이스 (개발용)

- [x] **의존성 설정**
  - Spring Web, Data JPA, Security
  - OAuth2 Client (Google)
  - JWT 인증 (JJWT)
  - Swagger/OpenAPI 3 (SpringDoc)
  - WebFlux (SSE 지원)

- [x] **설정 파일**
  - `application.yml`: H2 DB, JPA, OAuth2, JWT, CORS 설정
  - Swagger: JWT 인증 포함된 API 문서 자동 생성
  - CORS: Vue.js 프론트엔드 연동 준비

### ✅ Phase 2: 엔티티 및 데이터베이스

- [x] **공통 엔티티**
  - `BaseEntity`: 생성일시/수정일시 자동 관리 (JPA Auditing)

- [x] **유저 관리**
  - `User`: OAuth2 Google 로그인 정보
  - `RefreshToken`: JWT 리프레시 토큰 저장

- [x] **LoRA 모델 관리**
  - `LoraModel`: 모델 정보, 학습 설정, 공개/비공개
  - `ModelSample`: 샘플 이미지
  - `ModelPrompt`: 프롬프트 예시
  - `Tag`, `ModelTag`: 태그 시스템

- [x] **학습 및 생성**
  - `TrainingJob`: 학습 작업 진행 상태 추적
  - `GenerationHistory`: 이미지 생성 기록

- [x] **커뮤니티**
  - `Comment`, `CommentLike`: 댓글 및 좋아요
  - `ModelLike`, `ModelFavorite`: 모델 좋아요/즐겨찾기

### ✅ Phase 3: Repository 레이어

- [x] **13개 Repository 인터페이스 작성**
  - UserRepository, LoraModelRepository
  - ModelSampleRepository, ModelPromptRepository
  - TagRepository, ModelTagRepository
  - TrainingJobRepository, GenerationHistoryRepository
  - ModelLikeRepository, ModelFavoriteRepository
  - CommentRepository, CommentLikeRepository
  - RefreshTokenRepository

- [x] **주요 쿼리 메서드**
  - 페이징 처리 (`Pageable`)
  - 검색 기능 (제목, 설명, 태그)
  - 정렬 (인기순, 최신순, 좋아요순)

### ✅ Phase 4: Security 및 인증

- [x] **JWT 인증**
  - `JwtTokenProvider`: Access/Refresh Token 생성/검증
  - `JwtAuthenticationFilter`: 토큰 기반 인증 필터
  - Stateless 세션 관리 (멀티 인스턴스 고려)

- [x] **OAuth2 Google 로그인**
  - `OAuth2SuccessHandler`: 로그인 성공 시 JWT 발급
  - 신규 유저 자동 생성
  - 닉네임 중복 방지

- [x] **Spring Security 설정**
  - SecurityConfig: 인증/인가 규칙
  - WebConfig: CORS 설정
  - 공개 엔드포인트 vs 인증 필요 엔드포인트 분리

### ✅ Phase 5: 예외 처리 및 공통 응답

- [x] **공통 DTO**
  - `ApiResponse<T>`: 성공/실패 공통 응답 형식
  - `PageResponse<T>`: 페이징 처리된 응답
  - `ErrorResponse`: 에러 응답 (Validation 포함)

- [x] **예외 처리**
  - `ErrorCode`: 40+ 에러 코드 정의
  - `CustomException`: 커스텀 예외 클래스
  - `GlobalExceptionHandler`: 전역 예외 핸들러

### ✅ Phase 6: Swagger 문서화

- [x] **Swagger UI 설정**
  - 접속: `http://localhost:8080/swagger-ui.html`
  - JWT 인증 통합 (Bearer Token)
  - API 자동 문서화

---

## 🛠 기술 스택

### Backend
- **Framework**: Spring Boot 3.5.7
- **Language**: Java 17
- **Build Tool**: Gradle 8.14.3
- **Database**: H2 (개발), AWS RDS MySQL (프로덕션 예정)
- **ORM**: Spring Data JPA (Hibernate)

### Security
- **Authentication**: OAuth2 (Google) + JWT
- **Authorization**: Spring Security
- **Session**: Stateless (멀티 인스턴스 환경)

### API Documentation
- **Swagger**: SpringDoc OpenAPI 3

### 기타
- **SSE**: WebFlux (실시간 진행률 추적)
- **File Upload**: Multipart (최대 50MB)
- **Logging**: SLF4J + Logback

---

## 📁 프로젝트 구조

```
src/main/java/rheon/wsd_lora_community/
├── WsdLoraCommunityApplication.java
├── config/
│   ├── JpaConfig.java             # JPA Auditing 활성화
│   ├── SecurityConfig.java        # Spring Security 설정
│   ├── SwaggerConfig.java         # Swagger/OpenAPI 설정
│   └── WebConfig.java             # CORS, 정적 리소스
├── entity/
│   ├── common/
│   │   └── BaseEntity.java        # 공통 엔티티 (생성일, 수정일)
│   ├── user/
│   │   ├── User.java              # 유저
│   │   └── RefreshToken.java     # 리프레시 토큰
│   ├── model/
│   │   ├── LoraModel.java         # LoRA 모델
│   │   ├── ModelSample.java       # 샘플 이미지
│   │   ├── ModelPrompt.java       # 프롬프트 예시
│   │   ├── Tag.java               # 태그
│   │   └── ModelTag.java          # 모델-태그 연결
│   ├── training/
│   │   └── TrainingJob.java      # 학습 작업
│   ├── generation/
│   │   └── GenerationHistory.java # 생성 기록
│   └── community/
│       ├── Comment.java           # 댓글
│       ├── CommentLike.java       # 댓글 좋아요
│       ├── ModelLike.java         # 모델 좋아요
│       └── ModelFavorite.java     # 즐겨찾기
├── repository/
│   ├── UserRepository.java
│   ├── LoraModelRepository.java
│   ├── ... (13개 Repository)
├── service/                        # 🚧 다음 구현 예정
├── controller/                     # 🚧 다음 구현 예정
│   └── HealthCheckController.java # 헬스 체크
├── security/
│   ├── JwtTokenProvider.java      # JWT 생성/검증
│   ├── JwtAuthenticationFilter.java
│   ├── CustomUserDetailsService.java
│   └── OAuth2SuccessHandler.java
├── dto/
│   └── common/
│       ├── ApiResponse.java       # 공통 응답
│       ├── PageResponse.java      # 페이징 응답
│       └── ErrorResponse.java     # 에러 응답
├── exception/
│   ├── ErrorCode.java             # 에러 코드 정의
│   ├── CustomException.java       # 커스텀 예외
│   └── GlobalExceptionHandler.java
└── util/                           # 🚧 다음 구현 예정
```

---

## 🚀 실행 방법

### 1. 필수 요구사항

- Java 17
- Gradle 8.14+
- (선택) IntelliJ IDEA 또는 Eclipse

### 2. 프로젝트 클론

```bash
git clone <repository-url>
cd WSD_Lora_community
```

### 3. Google OAuth2 클라이언트 ID 설정 (선택)

OAuth2 로그인을 테스트하려면 Google Cloud Console에서 클라이언트 ID 발급:

```yaml
# application.yml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: YOUR_CLIENT_ID
            client-secret: YOUR_CLIENT_SECRET
```

### 4. 빌드 및 실행

```bash
# 빌드
./gradlew clean build

# 실행
./gradlew bootRun
```

서버가 실행되면:
- API 서버: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- H2 Console: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:loradb`
  - Username: `sa`
  - Password: (공백)

---

## 📡 API 엔드포인트

### 헬스 체크

```bash
GET /
```

**Response:**
```json
{
  "success": true,
  "message": "LoRA 모델 공유 플랫폼 API가 정상적으로 실행 중입니다.",
  "data": {
    "status": "UP",
    "service": "WSD_Lora_community",
    "version": "1.0.0"
  },
  "timestamp": "2025-11-13T00:00:00"
}
```

### 추가 API (구현 예정)

상세한 API 문서는 Swagger UI에서 확인 가능:
- `/api/auth/**` - 인증 (OAuth2, JWT)
- `/api/users/**` - 유저 관리
- `/api/models/**` - LoRA 모델 CRUD
- `/api/training/**` - 학습 작업
- `/api/generate/**` - 이미지 생성
- `/api/tags/**` - 태그
- `/api/search/**` - 검색

---

## 🔒 인증 방식

### JWT 기반 Stateless 인증

1. **Google OAuth2 로그인**
   ```
   GET /oauth2/authorization/google
   ```

2. **토큰 발급**
   - Access Token: 1시간 유효
   - Refresh Token: 7일 유효

3. **API 호출 시 헤더에 토큰 포함**
   ```
   Authorization: Bearer {accessToken}
   ```

### 멀티 인스턴스 고려사항

- Stateless JWT 사용 (서버 메모리에 세션 저장 안 함)
- Refresh Token은 DB에 저장
- CORS 설정으로 프론트엔드 연동
- 파일 저장은 S3 사용 예정 (현재는 로컬)

---

## 📊 데이터베이스 ERD

### 주요 테이블

- **users**: 유저 정보 (OAuth2)
- **lora_models**: LoRA 모델 메타데이터
- **model_samples**: 샘플 이미지
- **model_prompts**: 프롬프트 예시
- **tags**, **model_tags**: 태그 시스템
- **training_jobs**: 학습 작업 진행 상태
- **generation_history**: 이미지 생성 기록
- **comments**, **comment_likes**: 댓글 및 좋아요
- **model_likes**, **model_favorites**: 모델 좋아요/즐겨찾기

---

## 🚧 다음 구현 예정 (Remaining Tasks)

### Phase 7: Service 레이어
- [ ] AuthService (로그인, 토큰 갱신, 로그아웃)
- [ ] UserService (프로필 조회/수정)
- [ ] LoraModelService (모델 CRUD, 검색, 필터링)
- [ ] TrainingService (FastAPI 연동, 학습 진행률 추적)
- [ ] GenerationService (이미지 생성, 실시간 진행률)
- [ ] CommentService (댓글 CRUD, 대댓글)
- [ ] LikeService, FavoriteService (좋아요/즐겨찾기 토글)
- [ ] TagService (태그 관리, 인기 태그)

### Phase 8: Controller 레이어
- [ ] AuthController
- [ ] UserController
- [ ] LoraModelController
- [ ] TrainingController (SSE 포함)
- [ ] GenerationController (SSE 포함)
- [ ] CommentController
- [ ] TagController
- [ ] SearchController

### Phase 9: FastAPI 연동
- [ ] FastApiClient (RestTemplate/WebClient)
- [ ] SSE 스트리밍 처리
- [ ] 파일 업로드 처리

### Phase 10: 테스트 및 배포
- [ ] 단위 테스트 (Service)
- [ ] 통합 테스트 (Controller)
- [ ] H2 → AWS RDS MySQL 전환
- [ ] Docker 컨테이너화
- [ ] CI/CD 파이프라인

---

## 📝 개발 가이드

### 새로운 API 추가 시

1. **DTO 작성** (`dto/request`, `dto/response`)
2. **Service 메서드 작성** (비즈니스 로직)
3. **Controller 작성** (REST API 엔드포인트)
4. **Swagger 문서화** (`@Operation`, `@Tag`)

### 에러 처리

```java
throw new CustomException(ErrorCode.USER_NOT_FOUND);
```

### 페이징 처리

```java
Page<LoraModel> models = loraModelRepository.findAll(
    PageRequest.of(page, size, Sort.by("createdAt").descending())
);
return PageResponse.of(models);
```

---

## 🤝 기여

이 프로젝트는 학교 프로젝트로 개발되었습니다.

---

## 📄 라이센스

MIT License

---

## 🔗 관련 링크

- [CLAUDE.md](./CLAUDE.md) - 전체 개발 계획 및 TODO
- [Spring Boot Docs](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Security OAuth2](https://spring.io/guides/tutorials/spring-boot-oauth2/)
- [SpringDoc OpenAPI](https://springdoc.org/)

---

**Last Updated**: 2025-11-13
**Version**: 1.0.0-SNAPSHOT
**Author**: Rheon
