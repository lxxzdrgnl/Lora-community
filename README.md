# LoRA 모델 공유 플랫폼 - Backend

> 만화/웹툰 캐릭터 LoRA 모델 학습, 생성, 공유를 위한 커뮤니티 플랫폼 백엔드 API

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

---

## 📌 프로젝트 개요

만화/웹툰 캐릭터 LoRA 모델을 학습하고 공유하며, AI 이미지를 생성할 수 있는 커뮤니티 플랫폼의 백엔드 서버입니다.

### 시스템 구성
```
Vue.js (Frontend) ↔ Spring Boot 3 (Backend) ↔ FastAPI (AI Service)
                              ↕
                    H2 / MySQL (Database)
```

---

## 🚀 현재 상태

### ✅ 완료된 작업
- ✅ **Package-by-Feature 구조 완성** (도메인별 분리)
- ✅ **Entity 14개 작성** (User, LoraModel, Comment 등)
- ✅ **Repository 13개 작성** (JPA + 커스텀 쿼리)
- ✅ **Security 구현** (JWT + OAuth2 Google)
- ✅ **DTO 20개 작성** (Request/Response)
- ✅ **Service 11개 작성** (모든 도메인 Service 완료)
  - UserService, AuthService, LoraModelService
  - SampleService, PromptService, TagService
  - CommentService, LikeService, FavoriteService
  - TrainingService, GenerationService
- ✅ **전역 예외 처리** (40+ ErrorCode)
- ✅ **Swagger UI** (API 문서 자동 생성)
- ✅ **Phase 2 완료** - 빌드 성공 ✅

### 🚧 다음 작업
- 🚧 **Phase 3: Controller 레이어** (8개 필요)
- 🚧 **Phase 4: FastAPI 클라이언트**

---

## 🛠 기술 스택

### Backend
- **Framework**: Spring Boot 3.5.7
- **Language**: Java 17
- **Build Tool**: Gradle 8.14.3
- **Database**: H2 (dev) / MySQL (prod)
- **ORM**: Spring Data JPA

### Security & Auth
- **Authentication**: OAuth2 (Google) + JWT
- **Authorization**: Spring Security
- **Session**: Stateless (멀티 인스턴스 지원)

### API & Docs
- **API Docs**: SpringDoc OpenAPI 3 (Swagger)
- **Realtime**: Server-Sent Events (SSE)

---

## 📁 프로젝트 구조 (Package-by-Feature)

```
src/main/java/rheon/wsd_lora_community/
├── WsdLoraCommunityApplication.java
├── global/                      # 공통 기능
│   ├── config/                 # Security, JPA, Swagger, CORS
│   ├── security/               # JWT, OAuth2, Filter
│   ├── exception/              # 전역 예외 처리
│   └── dto/                    # 공통 DTO
├── user/                        # 유저 도메인 ✅
│   ├── entity/                 # User, RefreshToken
│   ├── repository/
│   ├── service/                # ✅ UserService, AuthService
│   ├── controller/             # 🚧 예정
│   └── dto/
├── model/                       # LoRA 모델 도메인 ✅
│   ├── entity/                 # LoraModel, ModelSample, Tag 등
│   ├── repository/
│   ├── service/                # ✅ LoraModelService, SampleService, PromptService, TagService
│   ├── controller/             # 🚧 예정
│   └── dto/
├── community/                   # 커뮤니티 도메인 ✅
│   ├── entity/                 # Comment, Like, Favorite
│   ├── repository/
│   ├── service/                # ✅ CommentService, LikeService, FavoriteService
│   ├── controller/             # 🚧 예정
│   └── dto/
├── training/                    # 학습 도메인 ✅
│   ├── entity/                 # TrainingJob
│   ├── repository/
│   ├── service/                # ✅ TrainingService
│   ├── controller/             # 🚧 예정
│   └── dto/
└── generation/                  # 생성 도메인 ✅
    ├── entity/                 # GenerationHistory
    ├── repository/
    ├── service/                # ✅ GenerationService
    ├── controller/             # 🚧 예정
    └── dto/
```

---

## 🚀 실행 방법

### 1. 요구사항
- Java 17+
- Gradle 8.14+

### 2. 빌드 및 실행
```bash
cd /home/rheon/Desktop/Semester/3-2/WSD/WSD_Lora_community

# 빌드
./gradlew clean build -x test

# 실행
./gradlew bootRun
```

### 3. 접속
- **API 서버**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2 Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:loradb`
  - Username: `sa`
  - Password: (공백)

---

## 📡 API 구조

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
    "service": "WSD_Lora_community"
  }
}
```

### API 엔드포인트 (예정)
- `/api/auth/**` - 인증 (OAuth2, JWT)
- `/api/users/**` - 유저 관리
- `/api/models/**` - LoRA 모델 CRUD
- `/api/training/**` - 학습 작업 (SSE)
- `/api/generate/**` - 이미지 생성 (SSE)
- `/api/tags/**` - 태그 관리
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

3. **API 호출**
   ```
   Authorization: Bearer {accessToken}
   ```

### Stateless 설계
- JWT 기반 인증 (서버 메모리 세션 없음)
- Refresh Token만 DB 저장
- 멀티 인스턴스 환경 지원

---

## 🎯 주요 기능

### 1. 유저 관리
- OAuth2 Google 로그인
- 프로필 관리 (닉네임, 프로필 이미지)

### 2. LoRA 모델 관리
- 모델 CRUD (생성, 조회, 수정, 삭제)
- 검색 (제목, 설명, 태그)
- 공개/비공개 설정
- 좋아요/즐겨찾기

### 3. 커뮤니티
- 댓글 및 대댓글
- 좋아요 시스템
- 인기 모델 랭킹

### 4. AI 학습 & 생성
- FastAPI 연동 (예정)
- 실시간 진행률 추적 (SSE)
- 학습 기록 관리

---

## 📊 데이터베이스

### 주요 테이블 (14개)
- `users` - 유저 정보
- `lora_models` - LoRA 모델
- `model_samples` - 샘플 이미지
- `model_prompts` - 프롬프트 예시
- `tags`, `model_tags` - 태그 시스템
- `training_jobs` - 학습 작업
- `generation_history` - 생성 기록
- `comments`, `comment_likes` - 댓글/좋아요
- `model_likes`, `model_favorites` - 모델 좋아요/즐겨찾기
- `refresh_tokens` - 리프레시 토큰

---

## 🚧 다음 작업

### ~~Phase 1: Repository 메서드 추가~~ ✅
- ~~ModelPromptRepository, ModelTagRepository 등~~

### ~~Phase 2: Service 레이어 (8개)~~ ✅ 완료 (2025-01-13)
- ~~SampleService, PromptService, TagService~~
- ~~CommentService, LikeService, FavoriteService~~
- ~~TrainingService, GenerationService~~

### Phase 3: Controller 레이어 (8개) 🚧 다음 작업
1. `user/controller/AuthController.java` - `/api/auth` (로그인, 로그아웃, 토큰 갱신)
2. `user/controller/UserController.java` - `/api/users` (프로필 조회/수정)
3. `model/controller/LoraModelController.java` - `/api/models` (모델 CRUD, 검색)
4. `model/controller/TagController.java` - `/api/tags` (태그 관리)
5. `community/controller/CommentController.java` - `/api/models/{id}/comments` (댓글)
6. `training/controller/TrainingController.java` - `/api/training` (학습 작업)
7. `generation/controller/GenerationController.java` - `/api/generate` (이미지 생성)
8. `global/controller/SearchController.java` - `/api/search` (통합 검색)

### Phase 4: FastAPI 연동
- FastApiClient (RestTemplate/WebClient)
- SSE 스트리밍 처리

---

## 📝 개발 가이드

### Service 작성 시
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExampleService {

    @Transactional
    public void writeMethod() {
        // Write 메서드에만 @Transactional
        throw new CustomException(ErrorCode.EXAMPLE_ERROR);
    }
}
```

### Controller 작성 시
```java
@RestController
@RequestMapping("/api/example")
@RequiredArgsConstructor
@Tag(name = "Example API")
public class ExampleController {

    @GetMapping
    @Operation(summary = "예시 조회")
    public ApiResponse<ExampleResponse> get() {
        return ApiResponse.success(data);
    }
}
```

---

## 🔗 참고 자료

- **상세 개발 문서**: [CLAUDE.md](./CLAUDE.md)
- **Spring Boot Docs**: https://docs.spring.io/spring-boot/
- **Spring Security OAuth2**: https://spring.io/guides/tutorials/spring-boot-oauth2/
- **SpringDoc OpenAPI**: https://springdoc.org/

---

## 📄 라이센스

MIT License

---

**Last Updated**: 2025-01-13
**Version**: 1.0.0-SNAPSHOT
**Status**: In Development
**Progress**: Phase 2 완료 (Service 레이어) → Phase 3 진행 예정 (Controller 레이어)
