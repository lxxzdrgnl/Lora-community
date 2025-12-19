# 데이터베이스 마이그레이션 가이드

## 개요

이 프로젝트는 **Flyway**를 사용하여 데이터베이스 스키마 버전 관리를 수행합니다.

## Flyway란?

- **버전 관리 도구**: 데이터베이스 스키마를 Git처럼 버전 관리
- **자동 마이그레이션**: 애플리케이션 시작 시 자동으로 마이그레이션 실행
- **이력 관리**: `flyway_schema_history` 테이블에 마이그레이션 이력 저장

---

## 프로젝트 설정

### 1. 의존성 (build.gradle)

```gradle
// Flyway (DB 마이그레이션)
implementation 'org.flywaydb:flyway-core'
implementation 'org.flywaydb:flyway-mysql'
```

### 2. 설정 파일 (application.yml)

```yaml
spring:
  flyway:
    enabled: true  # Flyway 활성화
    baseline-on-migrate: true  # 기존 DB에 적용 가능
    baseline-version: 0
    locations: classpath:db/migration
    validate-on-migrate: true
```

**로컬 개발 환경 (application-local.yml)**:
```yaml
spring:
  flyway:
    enabled: false  # 로컬에서는 JPA ddl-auto: update 사용
```

**프로덕션 환경 (application-prod.yml)**:
```yaml
spring:
  flyway:
    enabled: true  # 프로덕션에서는 Flyway로 스키마 관리
```

---

## 마이그레이션 파일 구조

```
src/main/resources/db/migration/
├── V1__init.sql              # 초기 스키마 생성
├── V2__sample_data.sql       # 샘플 데이터 (개발용)
├── V3__add_column_xxx.sql    # 향후 스키마 변경 예시
└── R__refresh_views.sql      # 반복 실행 마이그레이션 (선택)
```

### 파일 네이밍 규칙

| 접두사 | 설명 | 예시 |
|--------|------|------|
| `V{버전}__{설명}.sql` | 버전 마이그레이션 (한 번만 실행) | `V1__init.sql` |
| `R__{설명}.sql` | 반복 마이그레이션 (매번 실행) | `R__refresh_views.sql` |

**버전 규칙**:
- `V1`, `V2`, `V3` (정수)
- `V1.1`, `V1.2` (소수점)
- `V2023_01_01` (날짜)

**주의사항**:
- 파일명에 `__` (언더스코어 2개) 필수
- 공백 사용 금지
- 실행된 마이그레이션 파일은 절대 수정하지 마세요 (체크섬 검증 실패)

---

## 현재 마이그레이션 파일

### V1__init.sql (초기 스키마)

13개 테이블 생성:
1. `users` - 유저
2. `refresh_tokens` - 리프레시 토큰
3. `lora_models` - LoRA 모델
4. `training_jobs` - 학습 작업
5. `tags` - 태그
6. `model_tags` - 모델-태그
7. `generation_history` - 생성 기록
8. `generated_images` - 생성된 이미지
9. `model_samples` - 모델 샘플
10. `model_prompts` - 모델 프롬프트
11. `comments` - 댓글
12. `comment_likes` - 댓글 좋아요
13. `model_likes` - 모델 좋아요

### V2__sample_data.sql (샘플 데이터)

개발/테스트용 데이터:
- 3명의 테스트 유저 (`testuser1`, `testuser2`, `admin`)
- 16개의 기본 태그 (anime, manga, realistic 등)

---

## 사용 방법

### 1. 새로운 MySQL 데이터베이스에 적용

#### RDS MySQL 사용 시

1. **RDS 인스턴스 생성** (AWS Console)
   - Engine: MySQL 8.0
   - Database name: `loradb`

2. **환경변수 설정**
   ```bash
   export DB_HOST=your-rds-endpoint.rds.amazonaws.com
   export DB_PORT=3306
   export DB_NAME=loradb
   export DB_USERNAME=admin
   export DB_PASSWORD=your-password
   export SPRING_PROFILES_ACTIVE=prod
   ```

3. **애플리케이션 실행**
   ```bash
   ./gradlew bootRun
   ```

4. **자동 마이그레이션 실행**
   - Flyway가 자동으로 `V1__init.sql`, `V2__sample_data.sql` 실행
   - 로그 확인:
     ```
     Flyway Community Edition 9.x.x by Redgate
     Database: jdbc:mysql://xxx.rds.amazonaws.com:3306/loradb
     Successfully validated 2 migrations
     Creating Schema History table `loradb`.`flyway_schema_history` ...
     Current version of schema `loradb`: << Empty Schema >>
     Migrating schema `loradb` to version "1 - init"
     Migrating schema `loradb` to version "2 - sample data"
     Successfully applied 2 migrations to schema `loradb`
     ```

#### 로컬 MySQL (Docker) 사용 시

1. **Docker로 MySQL 실행**
   ```bash
   docker run -d \
     --name lora-mysql \
     -e MYSQL_ROOT_PASSWORD=password \
     -e MYSQL_DATABASE=loradb \
     -p 3306:3306 \
     mysql:8.0
   ```

2. **application-local.yml 수정**
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/loradb
       username: root
       password: password
     flyway:
       enabled: true  # Flyway 활성화
   ```

3. **로컬 프로필로 실행**
   ```bash
   SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
   ```

### 2. 기존 데이터베이스에 적용 (마이그레이션)

기존 H2/MySQL에서 데이터가 있는 경우:

1. **데이터 백업 (중요!)**
   ```bash
   mysqldump -u root -p loradb > backup_$(date +%Y%m%d).sql
   ```

2. **Flyway baseline 설정**
   ```yaml
   spring:
     flyway:
       baseline-on-migrate: true
       baseline-version: 0
   ```

3. **애플리케이션 실행**
   - Flyway가 기존 테이블을 인식하고 버전 1부터 마이그레이션 적용

### 3. 샘플 데이터 제외하기 (프로덕션)

프로덕션에서 샘플 데이터를 원하지 않는 경우:

**Option 1: V2 파일 삭제**
```bash
rm src/main/resources/db/migration/V2__sample_data.sql
```

**Option 2: 실행 후 수동 삭제**
```sql
DELETE FROM users WHERE email LIKE '%@example.com';
DELETE FROM tags WHERE usage_count = 0;
```

---

## 새로운 마이그레이션 추가

### 예시 1: 컬럼 추가

**V3__add_user_bio.sql**:
```sql
-- 유저 프로필에 bio 필드 추가
ALTER TABLE users ADD COLUMN bio VARCHAR(500) AFTER nickname;
```

### 예시 2: 인덱스 추가

**V4__add_indexes.sql**:
```sql
-- 성능 최적화를 위한 인덱스 추가
CREATE INDEX idx_lora_model_like_count ON lora_models(like_count DESC);
CREATE INDEX idx_comments_created_at ON comments(created_at DESC);
```

### 예시 3: 테이블 추가

**V5__add_notifications.sql**:
```sql
-- 알림 테이블 추가
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

## Flyway 명령어

### Gradle 플러그인 추가 (선택)

`build.gradle`에 추가:
```gradle
plugins {
    id 'org.flywaydb.flyway' version '10.4.1'
}

flyway {
    url = 'jdbc:mysql://localhost:3306/loradb'
    user = 'root'
    password = 'password'
}
```

### 수동 명령어

```bash
# 마이그레이션 실행
./gradlew flywayMigrate

# 마이그레이션 정보 확인
./gradlew flywayInfo

# 마이그레이션 검증
./gradlew flywayValidate

# 전체 초기화 (주의: 모든 데이터 삭제)
./gradlew flywayClean

# 베이스라인 설정
./gradlew flywayBaseline
```

---

## 마이그레이션 이력 확인

### flyway_schema_history 테이블 조회

```sql
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
```

**결과 예시**:
| installed_rank | version | description | type | script | checksum | installed_on | success |
|----------------|---------|-------------|------|--------|----------|--------------|---------|
| 1 | 1 | init | SQL | V1__init.sql | 123456789 | 2025-01-20 10:00:00 | 1 |
| 2 | 2 | sample data | SQL | V2__sample_data.sql | 987654321 | 2025-01-20 10:00:01 | 1 |

---

## 문제 해결

### 1. 체크섬 불일치 오류

**오류 메시지**:
```
Migration checksum mismatch for migration version 1
```

**원인**: 실행된 마이그레이션 파일을 수정했을 때 발생

**해결책**:
1. 파일을 원래대로 복구
2. 또는 새로운 마이그레이션 파일 생성 (권장)
3. 강제 수정 (주의):
   ```sql
   UPDATE flyway_schema_history SET checksum = NULL WHERE version = '1';
   ```

### 2. 마이그레이션 실패 후 재시도

**오류 메시지**:
```
Migration V3__xxx.sql failed
```

**해결책**:
1. 실패한 마이그레이션 파일 수정
2. 이력 테이블에서 실패 기록 삭제:
   ```sql
   DELETE FROM flyway_schema_history WHERE version = '3' AND success = 0;
   ```
3. 애플리케이션 재시작

### 3. Flyway 완전 초기화

**주의**: 모든 데이터가 삭제됩니다!

```bash
# 1. 모든 테이블 삭제
./gradlew flywayClean

# 2. 마이그레이션 재실행
./gradlew flywayMigrate
```

또는 수동:
```sql
DROP DATABASE loradb;
CREATE DATABASE loradb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

---

## 환경별 설정

### 로컬 개발 (application-local.yml)

```yaml
spring:
  flyway:
    enabled: false  # JPA ddl-auto: update 사용 (빠른 개발)
  jpa:
    hibernate:
      ddl-auto: update
```

### 스테이징/프로덕션 (application-prod.yml)

```yaml
spring:
  flyway:
    enabled: true  # Flyway로 스키마 관리
    baseline-on-migrate: true
    clean-disabled: true  # 실수 방지
  jpa:
    hibernate:
      ddl-auto: validate  # 스키마 검증만
```

---

## 베스트 프랙티스

1. **마이그레이션은 한 번만 실행됩니다**
   - 실행된 파일은 절대 수정하지 마세요
   - 수정이 필요하면 새로운 마이그레이션 파일 생성

2. **트랜잭션 단위**
   - 하나의 마이그레이션 파일은 하나의 트랜잭션
   - 실패 시 자동 롤백 (MySQL InnoDB)

3. **롤백 전략**
   - Flyway는 자동 롤백을 지원하지 않음
   - 롤백이 필요하면 새로운 마이그레이션 파일로 되돌리기
   - 예: `V4__rollback_v3.sql`

4. **백업**
   - 프로덕션 마이그레이션 전에 항상 백업
   - 자동 백업 스크립트 사용 권장

5. **버전 관리**
   - 마이그레이션 파일도 Git에 커밋
   - 팀원들과 동기화 필수

---

## 참고 자료

- [Flyway 공식 문서](https://flywaydb.org/documentation/)
- [Spring Boot Flyway 가이드](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization.migration-tool.flyway)
- [Flyway vs Liquibase 비교](https://www.baeldung.com/liquibase-vs-flyway)

---

## 요약

✅ **H2 → RDS MySQL 전환 완료**
- Flyway로 스키마 버전 관리
- 초기 스키마 (V1__init.sql)
- 샘플 데이터 (V2__sample_data.sql)

✅ **환경별 설정**
- 로컬: JPA ddl-auto (Flyway 비활성화)
- 프로덕션: Flyway (ddl-auto: validate)

✅ **향후 스키마 변경**
- 새로운 `V3__xxx.sql` 파일 생성
- 절대 기존 파일 수정 금지

---

**문의사항이 있으면 팀 리더에게 연락하세요!**
