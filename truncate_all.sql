-- 모든 테이블의 데이터 삭제 (테이블 구조는 유지)
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE comment_likes;
TRUNCATE TABLE comments;
TRUNCATE TABLE model_likes;
TRUNCATE TABLE model_tags;
TRUNCATE TABLE model_prompts;
TRUNCATE TABLE model_samples;
TRUNCATE TABLE generated_images;
TRUNCATE TABLE generation_history;
TRUNCATE TABLE training_jobs;
TRUNCATE TABLE lora_models;
TRUNCATE TABLE refresh_tokens;
TRUNCATE TABLE users;
TRUNCATE TABLE tags;

SET FOREIGN_KEY_CHECKS = 1;
