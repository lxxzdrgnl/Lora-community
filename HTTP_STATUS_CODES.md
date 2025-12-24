# HTTP Status Code 사용 현황

## 실제 사용 중인 HTTP Status Code (13종)

### 2xx Success
1. **200 OK** - 일반 성공 응답
   - GET 요청 (조회)
   - PUT/PATCH 요청 (수정)
   - 대부분의 POST 요청

2. **201 CREATED** - 리소스 생성 성공
   - `POST /api/models` - 모델 생성
   - 기타 리소스 생성 엔드포인트

3. **204 NO CONTENT** - 삭제 성공 (응답 본문 없음)
   - `DELETE /api/models/{modelId}` - 모델 삭제
   - 기타 DELETE 요청

### 4xx Client Errors
4. **400 BAD REQUEST** - 잘못된 요청
   - `GlobalExceptionHandler.handleMethodArgumentNotValidException()` - Validation 실패
   - `GlobalExceptionHandler.handleBindException()` - Binding 실패
   - `TrainingController`, `GenerationController` - 수동 검증 실패

5. **401 UNAUTHORIZED** - 인증 필요
   - `ErrorCode.UNAUTHORIZED` - 인증되지 않은 요청
   - `ErrorCode.INVALID_TOKEN` - 잘못된 토큰
   - `ErrorCode.EXPIRED_TOKEN` - 만료된 토큰
   - `ErrorCode.INVALID_REFRESH_TOKEN` - 잘못된 리프레시 토큰
   - `ErrorCode.REFRESH_TOKEN_EXPIRED` - 리프레시 토큰 만료

6. **403 FORBIDDEN** - 권한 없음
   - `ErrorCode.HANDLE_ACCESS_DENIED` - 접근 거부
   - `ErrorCode.FORBIDDEN` - 권한 없음
   - `ErrorCode.MODEL_NOT_OWNER` - 모델 소유자만 수정/삭제 가능
   - `ErrorCode.MODEL_NOT_PUBLIC` - 비공개 모델
   - `ErrorCode.COMMENT_NOT_OWNER` - 댓글 작성자만 수정/삭제 가능
   - `GlobalExceptionHandler.handleAccessDeniedException()`

7. **404 NOT FOUND** - 리소스를 찾을 수 없음
   - `ErrorCode.RESOURCE_NOT_FOUND` - 리소스 없음
   - `ErrorCode.USER_NOT_FOUND` - 사용자 없음
   - `ErrorCode.MODEL_NOT_FOUND` - 모델 없음
   - `ErrorCode.TRAINING_JOB_NOT_FOUND` - 학습 작업 없음
   - `ErrorCode.GENERATION_NOT_FOUND` - 생성 기록 없음
   - `ErrorCode.COMMENT_NOT_FOUND` - 댓글 없음
   - `ErrorCode.TAG_NOT_FOUND` - 태그 없음
   - `ErrorCode.SAMPLE_NOT_FOUND` - 샘플 없음
   - `ErrorCode.PROMPT_NOT_FOUND` - 프롬프트 없음
   - `ErrorCode.REFRESH_TOKEN_NOT_FOUND` - 리프레시 토큰 없음
   - `ErrorCode.PARENT_COMMENT_NOT_FOUND` - 부모 댓글 없음

8. **405 METHOD NOT ALLOWED** - 지원하지 않는 HTTP 메서드
   - `ErrorCode.METHOD_NOT_ALLOWED`
   - `GlobalExceptionHandler.handleHttpRequestMethodNotSupportedException()`

9. **409 CONFLICT** - 중복 리소스
   - `ErrorCode.DUPLICATE_RESOURCE` - 이미 존재하는 리소스
   - `ErrorCode.DUPLICATE_EMAIL` - 이미 존재하는 이메일
   - `ErrorCode.DUPLICATE_NICKNAME` - 이미 존재하는 닉네임
   - `ErrorCode.DUPLICATE_TAG` - 이미 존재하는 태그
   - `ErrorCode.MODEL_TRAINING_IN_PROGRESS` - 모델 학습 중
   - `ErrorCode.TRAINING_ALREADY_IN_PROGRESS` - 이미 학습 진행 중

10. **422 UNPROCESSABLE_ENTITY** - 처리할 수 없는 요청
    - `ErrorCode.UNPROCESSABLE_ENTITY`
    - 비즈니스 로직 검증 실패

11. **429 TOO MANY REQUESTS** - Rate Limit 초과
    - `RateLimitFilter` - IP당 1분에 100개 요청 제한

### 5xx Server Errors
12. **500 INTERNAL SERVER ERROR** - 서버 내부 오류
    - `ErrorCode.INTERNAL_SERVER_ERROR`
    - `ErrorCode.FILE_UPLOAD_FAILED` - 파일 업로드 실패
    - `ErrorCode.TRAINING_FAILED` - 학습 실패
    - `ErrorCode.GENERATION_FAILED` - 이미지 생성 실패
    - `ErrorCode.FASTAPI_REQUEST_FAILED` - FastAPI 요청 실패
    - `GlobalExceptionHandler.handleException()`
    - `TrainingController`, `GenerationController` - 처리되지 않은 예외

13. **503 SERVICE UNAVAILABLE** - 서비스 이용 불가
    - `ErrorCode.FASTAPI_CONNECTION_ERROR` - FastAPI 서버 연결 실패

---

## Swagger 문서화 완료 엔드포인트

### LoraModelController
- ✅ `POST /api/models` - 201, 400, 401, 500
- ✅ `GET /api/models` - 200, 500
- ✅ `DELETE /api/models/{modelId}` - 204, 401, 403, 404, 500

---

## 에러 응답 형식 (표준)

```json
{
  "timestamp": "2025-03-05T12:00:00",
  "path": "/api/models/123",
  "status": 404,
  "code": "MODEL_001",
  "message": "모델을 찾을 수 없습니다.",
  "details": {
    "modelId": 123
  },
  "errors": [
    {
      "field": "title",
      "value": "",
      "reason": "제목은 필수입니다."
    }
  ]
}
```

### 필드 설명
- `timestamp`: 에러 발생 시간 (ISO 8601)
- `path`: 요청 경로
- `status`: HTTP 상태 코드 (숫자)
- `code`: 에러 코드 (문자열, ErrorCode enum)
- `message`: 에러 메시지 (사용자에게 표시)
- `details`: 추가 상세 정보 (선택적)
- `errors`: Validation 에러 목록 (선택적)

---

## Swagger UI 접근

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

---

## 권장 사항

### POST 엔드포인트 (리소스 생성)
```java
@PostMapping
@ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "생성 성공"),
    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
    @ApiResponse(responseCode = "401", description = "인증 필요"),
    @ApiResponse(responseCode = "409", description = "중복 리소스"),
    @ApiResponse(responseCode = "500", description = "서버 오류")
})
public ResponseEntity<ApiResponse<T>> create(...) {
    return ResponseEntity.status(HttpStatus.CREATED).body(...);
}
```

### DELETE 엔드포인트
```java
@DeleteMapping("/{id}")
@ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "삭제 성공"),
    @ApiResponse(responseCode = "401", description = "인증 필요"),
    @ApiResponse(responseCode = "403", description = "권한 없음"),
    @ApiResponse(responseCode = "404", description = "리소스 없음"),
    @ApiResponse(responseCode = "500", description = "서버 오류")
})
public ResponseEntity<Void> delete(...) {
    return ResponseEntity.noContent().build();
}
```

### GET 엔드포인트
```java
@GetMapping
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "조회 성공"),
    @ApiResponse(responseCode = "404", description = "리소스 없음"),
    @ApiResponse(responseCode = "500", description = "서버 오류")
})
public ResponseEntity<ApiResponse<T>> get(...) {
    return ResponseEntity.ok(...);
}
```
