-- =====================================================
-- WSD LoRA Community - Initial Schema Migration
-- Version: V1
-- Description: 초기 데이터베이스 스키마 생성
-- Author: System
-- Created: 2025-01-20
-- =====================================================

-- 1. users 테이블 (유저)
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    nickname VARCHAR(10) NOT NULL UNIQUE,
    profile_image_url VARCHAR(500),
    oauth_provider VARCHAR(20) NOT NULL,
    oauth_provider_id VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    deleted_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_oauth (oauth_provider, oauth_provider_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. refresh_tokens 테이블 (리프레시 토큰)
CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(500) NOT NULL UNIQUE,
    expires_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_refresh_token_user (user_id),
    INDEX idx_refresh_token_token (token),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. lora_models 테이블 (LoRA 모델)
CREATE TABLE lora_models (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    training_job_id BIGINT,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    s3_key VARCHAR(500),
    file_size BIGINT,
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    view_count INT NOT NULL DEFAULT 0,
    like_count INT NOT NULL DEFAULT 0,
    deleted_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_lora_model_user (user_id),
    INDEX idx_lora_model_public_status (is_public, deleted_at),
    INDEX idx_lora_model_created (created_at),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. training_jobs 테이블 (학습 작업)
CREATE TABLE training_jobs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    model_id BIGINT,
    user_id BIGINT NOT NULL,
    model_name VARCHAR(200) NOT NULL,
    model_description TEXT,
    training_images_count INT,
    epochs INT,
    learning_rate DOUBLE,
    lora_rank INT,
    base_model VARCHAR(200),
    trigger_word VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    current_epoch INT NOT NULL DEFAULT 0,
    total_epochs INT,
    phase VARCHAR(50),
    error_message TEXT,
    started_at DATETIME,
    completed_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_training_model (model_id),
    INDEX idx_training_user (user_id),
    INDEX idx_training_status (status),
    FOREIGN KEY (model_id) REFERENCES lora_models(id) ON DELETE SET NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. tags 테이블 (태그)
CREATE TABLE tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    category VARCHAR(20) NOT NULL DEFAULT 'OTHER',
    usage_count INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. model_tags 테이블 (모델-태그 연결)
CREATE TABLE model_tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    model_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_model_tag_model (model_id),
    INDEX idx_model_tag_tag (tag_id),
    UNIQUE KEY unique_model_tag (model_id, tag_id),
    FOREIGN KEY (model_id) REFERENCES lora_models(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. generation_history 테이블 (이미지 생성 기록)
CREATE TABLE generation_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    model_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    prompt TEXT NOT NULL,
    negative_prompt TEXT,
    steps INT,
    guidance_scale DECIMAL(4, 2),
    seed BIGINT,
    lora_scale DECIMAL(4, 2),
    num_images INT NOT NULL DEFAULT 1,
    status VARCHAR(20) NOT NULL DEFAULT 'GENERATING',
    current_step INT,
    total_steps INT,
    error_message TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_generation_model (model_id),
    INDEX idx_generation_user (user_id),
    INDEX idx_generation_created (created_at),
    FOREIGN KEY (model_id) REFERENCES lora_models(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. generated_images 테이블 (생성된 이미지)
CREATE TABLE generated_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    generation_history_id BIGINT NOT NULL,
    s3_url VARCHAR(500) NOT NULL,
    s3_key VARCHAR(255) NOT NULL,
    file_size BIGINT,
    display_order INT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_generated_images_history (generation_history_id),
    FOREIGN KEY (generation_history_id) REFERENCES generation_history(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. model_samples 테이블 (모델 샘플 이미지)
CREATE TABLE model_samples (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    model_id BIGINT NOT NULL,
    generated_image_id BIGINT NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_sample_model (model_id),
    INDEX idx_sample_model_order (model_id, display_order),
    FOREIGN KEY (model_id) REFERENCES lora_models(id) ON DELETE CASCADE,
    FOREIGN KEY (generated_image_id) REFERENCES generated_images(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. model_prompts 테이블 (모델 프롬프트 예시)
CREATE TABLE model_prompts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    model_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    prompt TEXT NOT NULL,
    negative_prompt TEXT,
    description TEXT,
    display_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_prompt_model (model_id),
    FOREIGN KEY (model_id) REFERENCES lora_models(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. comments 테이블 (댓글)
CREATE TABLE comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    model_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    like_count INT NOT NULL DEFAULT 0,
    deleted_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_comment_model (model_id),
    INDEX idx_comment_user (user_id),
    FOREIGN KEY (model_id) REFERENCES lora_models(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. comment_likes 테이블 (댓글 좋아요)
CREATE TABLE comment_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    comment_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_comment_like_comment (comment_id),
    INDEX idx_comment_like_user (user_id),
    UNIQUE KEY unique_comment_like (comment_id, user_id),
    FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. model_likes 테이블 (모델 좋아요)
CREATE TABLE model_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    model_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_model_like_model (model_id),
    INDEX idx_model_like_user (user_id),
    UNIQUE KEY unique_model_like (model_id, user_id),
    FOREIGN KEY (model_id) REFERENCES lora_models(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 초기 스키마 생성 완료
-- 총 13개 테이블:
-- 1. users (유저)
-- 2. refresh_tokens (리프레시 토큰)
-- 3. lora_models (LoRA 모델)
-- 4. training_jobs (학습 작업)
-- 5. tags (태그)
-- 6. model_tags (모델-태그)
-- 7. generation_history (생성 기록)
-- 8. generated_images (생성된 이미지)
-- 9. model_samples (모델 샘플)
-- 10. model_prompts (모델 프롬프트)
-- 11. comments (댓글)
-- 12. comment_likes (댓글 좋아요)
-- 13. model_likes (모델 좋아요)
-- =====================================================
