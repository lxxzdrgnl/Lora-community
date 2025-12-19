# AWS Elastic Beanstalk 배포 가이드

## 개요

이 프로젝트는 **AWS Elastic Beanstalk**를 사용하여 자동 배포됩니다.

---

## 📋 현재 배포 환경

### 플랫폼 정보
- **서비스**: AWS Elastic Beanstalk
- **리전**: ap-northeast-2 (서울)
- **애플리케이션**: Blueming-AI
- **환경**: Blueming-AI-env-1
- **플랫폼**: Corretto 17 (Java)
- **웹 서버**: Nginx (프록시)

### 인스턴스 스펙
- **인스턴스 타입**: t3.small 권장
- **메모리**: 2GB RAM
- **Heap 메모리**: 512MB (max), 256MB (min)
- **Metaspace**: 128MB
- **Swap 메모리**: 2GB (자동 설정)

### Spring 설정
- **Spring Profile**: `prod` (자동 설정)
- **포트**: 5000
- **데이터베이스**: RDS MySQL 8.0

---

## 🚀 자동 배포 (GitHub Actions)

### 트리거 조건
- `main` 브랜치에 push할 때 자동 배포

### 배포 프로세스
1. GitHub Actions가 자동 실행
2. JDK 17 설정
3. Gradle 빌드 (`./gradlew build -x test`)
4. JAR 파일 이름 통일 (`application.jar`)
5. Elastic Beanstalk에 자동 배포
6. 환경 변수 적용
7. 애플리케이션 재시작

### GitHub Secrets 설정 필요
GitHub Repository → Settings → Secrets and variables → Actions에서 추가:

```
AWS_ACCESS_KEY_ID=AKIA...
AWS_SECRET_ACCESS_KEY=...
```

---

## 🗄️ RDS MySQL 설정

### 1. RDS 인스턴스 생성

**AWS Console → RDS → Create database**

#### 기본 설정
- **Engine**: MySQL 8.0
- **Template**: Free tier (테스트) 또는 Production
- **DB instance identifier**: `lora-community-db`
- **Master username**: `admin`
- **Master password**: 강력한 비밀번호 설정 (기록 필수!)

#### 인스턴스 설정
- **DB instance class**:
  - Free tier: `db.t3.micro`
  - Production: `db.t3.small` 이상
- **Storage**: 20 GB (General Purpose SSD)
- **Multi-AZ**: Production에서는 활성화 권장

#### 연결 설정
- **VPC**: Default VPC (Elastic Beanstalk과 같은 VPC)
- **Public access**: No (보안상 권장)
- **VPC security group**: 새로 생성
  - **Inbound rule**:
    - Type: MySQL/Aurora (3306)
    - Source: Elastic Beanstalk 보안 그룹
- **Availability Zone**: ap-northeast-2a (Elastic Beanstalk와 같은 AZ)

#### 추가 설정
- **Database name**: `loradb`
- **Port**: 3306
- **Backup retention**: 7일 (프로덕션)
- **Encryption**: 활성화 권장

### 2. RDS 엔드포인트 확인

RDS 인스턴스 생성 후 엔드포인트 복사:
```
lora-community-db.c9akciq32.ap-northeast-2.rds.amazonaws.com
```

---

## ⚙️ Elastic Beanstalk 환경 변수 설정

### AWS Console에서 설정

**Elastic Beanstalk → Environments → Blueming-AI-env-1 → Configuration → Software**

"Environment properties" 섹션에 다음 환경 변수 추가:

#### 필수 환경 변수

| 이름 | 값 | 설명 |
|------|-----|------|
| `RDS_HOSTNAME` | `lora-community-db.xxx.rds.amazonaws.com` | RDS 엔드포인트 |
| `RDS_PORT` | `3306` | MySQL 포트 |
| `RDS_DB_NAME` | `loradb` | 데이터베이스 이름 |
| `RDS_USERNAME` | `admin` | RDS 마스터 유저 |
| `RDS_PASSWORD` | `your-strong-password` | RDS 비밀번호 |
| `JWT_SECRET` | `wsd-lora-community-jwt-secret-key-...` | JWT 시크릿 키 (256bit+) |
| `GOOGLE_CLIENT_ID` | `990214424232-xxx.apps.googleusercontent.com` | Google OAuth2 클라이언트 ID |
| `GOOGLE_CLIENT_SECRET` | `GOCSPX-xxx` | Google OAuth2 시크릿 |
| `FRONTEND_URL` | `https://your-frontend-domain.com` | 프론트엔드 도메인 |
| `CALLBACK_URL` | `http://blueming-ai-env-1...elasticbeanstalk.com` | 백엔드 콜백 URL |
| `FASTAPI_URL` | `https://dldydwo9--lora-training...modal.run` | FastAPI/Modal URL |

#### S3 버킷 환경 변수

| 이름 | 값 | 설명 |
|------|-----|------|
| `AWS_S3_TRAINING_BUCKET` | `lora-training-data-bucket` | 학습 데이터 버킷 |
| `AWS_S3_MODELS_BUCKET` | `lora-models-bucket` | 모델 파일 버킷 |
| `AWS_S3_GENERATED_BUCKET` | `lora-generated-image-bucket` | 생성 이미지 버킷 |

**주의**: `AWS_ACCESS_KEY_ID`와 `AWS_SECRET_ACCESS_KEY`는 EC2 인스턴스 역할(IAM Role)로 관리하는 것이 더 안전합니다.

### AWS CLI로 설정 (선택)

```bash
aws elasticbeanstalk update-environment \
  --application-name Blueming-AI \
  --environment-name Blueming-AI-env-1 \
  --option-settings \
    Namespace=aws:elasticbeanstalk:application:environment,OptionName=RDS_HOSTNAME,Value=lora-community-db.xxx.rds.amazonaws.com \
    Namespace=aws:elasticbeanstalk:application:environment,OptionName=RDS_PORT,Value=3306 \
    Namespace=aws:elasticbeanstalk:application:environment,OptionName=RDS_DB_NAME,Value=loradb \
    Namespace=aws:elasticbeanstalk:application:environment,OptionName=RDS_USERNAME,Value=admin \
    Namespace=aws:elasticbeanstalk:application:environment,OptionName=RDS_PASSWORD,Value=your-password
```

---

## 🔒 보안 설정

### 1. RDS 보안 그룹 설정

RDS 보안 그룹 Inbound Rules:

| Type | Protocol | Port | Source | Description |
|------|----------|------|--------|-------------|
| MySQL/Aurora | TCP | 3306 | sg-xxxxx (Elastic Beanstalk SG) | Allow EB instances |

### 2. Elastic Beanstalk 보안 그룹

Elastic Beanstalk 보안 그룹 Outbound Rules:

| Type | Protocol | Port | Destination | Description |
|------|----------|------|-------------|-------------|
| MySQL/Aurora | TCP | 3306 | sg-yyyyy (RDS SG) | Allow RDS connection |
| HTTPS | TCP | 443 | 0.0.0.0/0 | Internet access |

### 3. IAM Role 설정 (권장)

S3 접근을 위해 Elastic Beanstalk 인스턴스 역할에 권한 추가:

**Policy**: `AmazonS3FullAccess` 또는 커스텀 정책:
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:GetObject",
        "s3:PutObject",
        "s3:DeleteObject",
        "s3:ListBucket"
      ],
      "Resource": [
        "arn:aws:s3:::lora-training-data-bucket/*",
        "arn:aws:s3:::lora-models-bucket/*",
        "arn:aws:s3:::lora-generated-image-bucket/*"
      ]
    }
  ]
}
```

---

## 📦 S3 버킷 생성

### 필요한 버킷 (3개)

```bash
# 1. 학습 데이터 버킷
aws s3 mb s3://lora-training-data-bucket --region ap-northeast-2

# 2. 모델 파일 버킷
aws s3 mb s3://lora-models-bucket --region ap-northeast-2

# 3. 생성 이미지 버킷
aws s3 mb s3://lora-generated-image-bucket --region ap-northeast-2
```

### CORS 설정 (생성 이미지 버킷)

`lora-generated-image-bucket` → Permissions → CORS:

```json
[
  {
    "AllowedHeaders": ["*"],
    "AllowedMethods": ["GET", "HEAD"],
    "AllowedOrigins": ["https://your-frontend-domain.com"],
    "ExposeHeaders": ["ETag"],
    "MaxAgeSeconds": 3600
  }
]
```

---

## 🔄 데이터베이스 마이그레이션

### 첫 배포 시

애플리케이션 시작 시 **Flyway**가 자동으로 스키마를 생성합니다:

1. Elastic Beanstalk에 환경 변수 설정
2. GitHub Actions로 배포 (main 브랜치 push)
3. 애플리케이션 시작 시 Flyway 실행
4. `V1__init.sql` 실행 → 13개 테이블 생성
5. RDS에 스키마 생성 완료

### 스키마 변경 시

1. `src/main/resources/db/migration/V2__xxx.sql` 파일 생성
2. Git commit & push
3. GitHub Actions가 자동 배포
4. Flyway가 자동으로 마이그레이션 실행

---

## 🧪 테스트

### 로컬에서 프로덕션 설정 테스트

```bash
# 환경 변수 설정
export DB_HOST=your-rds-endpoint.rds.amazonaws.com
export DB_USERNAME=admin
export DB_PASSWORD=your-password
export SPRING_PROFILES_ACTIVE=prod
export JWT_SECRET=your-jwt-secret
export GOOGLE_CLIENT_ID=your-google-client-id
export GOOGLE_CLIENT_SECRET=your-google-client-secret

# 실행
./gradlew bootRun
```

### 배포 후 헬스 체크

```bash
# Elastic Beanstalk 헬스 체크
curl http://blueming-ai-env-1.ap-northeast-2.elasticbeanstalk.com/actuator/health

# 응답 예시
{"status":"UP"}
```

---

## 🐛 문제 해결

### 1. 데이터베이스 연결 실패

**증상**:
```
CommunicationsException: Communications link failure
```

**확인 사항**:
1. RDS 엔드포인트가 올바른지 확인
2. 보안 그룹 Inbound rule 확인 (3306 포트)
3. VPC가 동일한지 확인
4. 환경 변수 `RDS_HOSTNAME`, `RDS_USERNAME`, `RDS_PASSWORD` 확인

**해결책**:
```bash
# 1. RDS 엔드포인트 확인
aws rds describe-db-instances --db-instance-identifier lora-community-db

# 2. 보안 그룹 확인
aws ec2 describe-security-groups --group-ids sg-xxxxx

# 3. 연결 테스트 (Elastic Beanstalk 인스턴스에서)
mysql -h lora-community-db.xxx.rds.amazonaws.com -u admin -p
```

### 2. Flyway 마이그레이션 실패

**증상**:
```
FlywayException: Unable to obtain connection from database
```

**확인**:
1. RDS가 시작되었는지 확인
2. 데이터베이스 이름이 `loradb`인지 확인
3. `RDS_DB_NAME` 환경 변수 확인

**해결책**:
```bash
# RDS 상태 확인
aws rds describe-db-instances --db-instance-identifier lora-community-db \
  --query 'DBInstances[0].DBInstanceStatus'

# 데이터베이스 생성 (없는 경우)
mysql -h xxx.rds.amazonaws.com -u admin -p
CREATE DATABASE loradb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. 메모리 부족 (OutOfMemoryError)

**증상**:
```
java.lang.OutOfMemoryError: Java heap space
```

**해결책**:

1. **인스턴스 타입 업그레이드**:
   - t3.small → t3.medium (4GB RAM)

2. **JVM 옵션 조정** (`.ebextensions/jvm-options.config`):
   ```yaml
   JAVA_TOOL_OPTIONS: "-Xmx1024m -Xms512m -XX:MaxMetaspaceSize=256m"
   ```

3. **Swap 메모리 확인**:
   ```bash
   # Elastic Beanstalk 인스턴스에서
   free -h
   ```

### 4. SSE 타임아웃

**증상**:
```
504 Gateway Timeout (SSE 스트리밍 중단)
```

**확인**:
- `.platform/nginx/conf.d/sse.conf` 파일 존재 여부
- Nginx 타임아웃 설정 (현재 10분)

**해결책**:
```nginx
# 타임아웃 늘리기 (20분)
proxy_read_timeout 1200s;
proxy_connect_timeout 1200s;
proxy_send_timeout 1200s;
```

---

## 📊 모니터링

### CloudWatch 로그

**Elastic Beanstalk → Logs → Request Logs**

로그 위치:
- `/var/log/eb-engine.log` - Elastic Beanstalk 엔진
- `/var/log/nginx/access.log` - Nginx 액세스
- `/var/log/nginx/error.log` - Nginx 에러
- `/var/app/current/application.log` - Spring Boot (stdout)

### 애플리케이션 로그 확인

```bash
# SSH로 인스턴스 접속
eb ssh Blueming-AI-env-1

# Spring Boot 로그
tail -f /var/log/web.stdout.log

# Nginx 에러 로그
tail -f /var/log/nginx/error.log
```

### RDS 모니터링

**RDS → Monitoring → CloudWatch metrics**

중요 메트릭:
- **CPUUtilization**: 80% 이하 유지
- **DatabaseConnections**: 최대 연결 수 확인
- **FreeableMemory**: 여유 메모리 확인
- **WriteLatency / ReadLatency**: 레이턴시 모니터링

---

## 🔄 롤백

### 이전 버전으로 롤백

**Elastic Beanstalk → Application versions**

1. 이전 버전 선택
2. "Deploy" 버튼 클릭
3. 환경 선택 (Blueming-AI-env-1)
4. 배포 확인

### 긴급 롤백 (CLI)

```bash
# 이전 버전으로 즉시 롤백
eb deploy Blueming-AI-env-1 --version github-action-abc1234
```

---

## 📋 체크리스트

### 배포 전 필수 확인사항

- [ ] RDS 인스턴스 생성 완료
- [ ] RDS 보안 그룹 설정 (3306 포트 허용)
- [ ] Elastic Beanstalk 환경 변수 설정 (10개 이상)
- [ ] S3 버킷 생성 (3개)
- [ ] IAM Role 권한 설정 (S3 접근)
- [ ] GitHub Secrets 설정 (AWS 키)
- [ ] Google OAuth2 클라이언트 설정
- [ ] Frontend URL CORS 설정

### 배포 후 확인사항

- [ ] 애플리케이션 헬스 체크 (UP)
- [ ] RDS 연결 성공
- [ ] Flyway 마이그레이션 완료
- [ ] API 엔드포인트 응답 확인
- [ ] OAuth2 로그인 테스트
- [ ] 로그 확인 (에러 없음)

---

## 🆘 지원

### AWS 리소스 정리 (비용 절감)

**사용하지 않을 때**:
```bash
# Elastic Beanstalk 환경 종료 (인스턴스 중지)
eb terminate Blueming-AI-env-1

# RDS 중지 (7일간 자동 중지)
aws rds stop-db-instance --db-instance-identifier lora-community-db

# S3 버킷 비우기
aws s3 rm s3://lora-generated-image-bucket --recursive
```

### 추가 문서
- [AWS Elastic Beanstalk 공식 문서](https://docs.aws.amazon.com/elasticbeanstalk/)
- [RDS MySQL 가이드](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/CHAP_MySQL.html)
- [Flyway 마이그레이션 가이드](DATABASE_MIGRATION.md)

---

**작성일**: 2025-01-20
**작성자**: Claude Code Assistant
**프로젝트**: WSD_Lora_community
