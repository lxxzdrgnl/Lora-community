-- 1. 데이터 입력 중 외래키 에러 방지 (순서 무시)
SET FOREIGN_KEY_CHECKS = 0;

-- 2. Users 데이터 (U& 유니코드 -> 한글 복원 완료)
INSERT INTO users (CREATED_AT, DELETED_AT, ID, UPDATED_AT, NICKNAME, NAME, PROFILE_IMAGE_URL, EMAIL, OAUTH_PROVIDER_ID, OAUTH_PROVIDER, ROLE) VALUES
                                                                                                                                                  ('2025-11-25 04:33:49.538308', NULL, 0, '2025-11-25 04:39:12.588406', '호그라이더', '호그라이더', 'https://i.namu.wiki/i/aQOMWPkdAQdPbTFq54MsoZiwOsxCWYwioCRnDuuP6hoihR5DlP3quxzC9hSP3y7H2bFIu6blY-sX7KvP6NsABA.webp', 'dldydwo9@gmail.com', '115796410464082379941', 'GOOGLE', 'USER'),
                                                                                                                                                  ('2025-11-01 00:00:00.000000', NULL, 100, '2025-11-01 00:00:00.000000', 'testUser1', 'Test User', 'https://previews.123rf.com/images/alexraths/alexraths1501/alexraths150100053/35560687-portrait-of-a-screaming-clown-isolated-on-white-background.jpg', 'test@test.com', 'test-oauth-id-100', 'GOOGLE', 'USER'),
                                                                                                                                                  ('2025-11-20 10:00:00.000000', NULL, 101, '2025-11-20 10:00:00.000000', 'AIUser1', 'Fake User 1', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTxNKOirm3pJEReRAFc1VTQ_mmpdKFlqsSGXmmpPHnw24qlM3PekwGy1glAiDA5kQE5Lhc&usqp=CAU', 'fake1@example.com', 'fake-oauth-id-101', 'GOOGLE', 'USER'),
                                                                                                                                                  ('2025-11-21 10:00:00.000000', NULL, 102, '2025-11-21 10:00:00.000000', 'AIUser2', 'Fake User 2', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTK_N21b6QD1emRqHOgWbg3FYU3M5vqtyjKZiCmj5M0jcGkT8LucfMKnvIJ7or0A0NvdBQ&usqp=CAU', 'fake2@example.com', 'fake-oauth-id-102', 'GOOGLE', 'USER');

-- 3. Training Jobs
-- 컬럼 순서: MODEL_NAME, MODEL_DESCRIPTION, TRAINING_IMAGES_COUNT, EPOCHS, LEARNING_RATE, LORA_RANK, BASE_MODEL, TRIGGER_WORD, STATUS, CURRENT_EPOCH, TOTAL_EPOCHS, PHASE, ERROR_MESSAGE, STARTED_AT, COMPLETED_AT, CREATED_AT, UPDATED_AT, ID, MODEL_ID, USER_ID
INSERT INTO training_jobs (MODEL_NAME, MODEL_DESCRIPTION, TRAINING_IMAGES_COUNT, EPOCHS, LEARNING_RATE, LORA_RANK, BASE_MODEL, TRIGGER_WORD, STATUS, CURRENT_EPOCH, TOTAL_EPOCHS, PHASE, ERROR_MESSAGE, STARTED_AT, COMPLETED_AT, CREATED_AT, UPDATED_AT, ID, MODEL_ID, USER_ID) VALUES
    -- 완료된 학습 (모델 1 - Reze)
    ('Reze', '체인소맨 레제 만화버전', 20, 200, 0.0001, 16, 'stablediffusionapi/anything-v5', 'sks', 'SUCCESS', 200, 200, 'model_saved', NULL, '2025-11-10 08:05:30.000000', '2025-11-12 14:30:15.123456', '2025-11-10 08:00:00.000000', '2025-11-12 14:30:15.123456', 1, NULL, 0),
    -- 완료된 학습 (모델 2 - Pochita)
    ('Pochita', '체인소맨 포치타', 30, 100, 0.0001, 16, 'stablediffusionapi/anything-v5', 'pochita', 'SUCCESS', 100, 100, 'model_saved', NULL, '2025-11-15 08:05:30.000000', '2025-11-15 12:30:15.123456', '2025-11-15 08:00:00.000000', '2025-11-15 12:30:15.123456', 2, NULL, 0),
    -- 완료된 학습 (모델 3 - Mouri Kogorou)
    ('Mouri Kogorou', '명탐정 코난 모리 코고로 weight 0.6 ~ 0.7 추천', 20, 150, 0.00008, 32, 'stablediffusionapi/anything-v5', NULL, 'SUCCESS', 150, 150, 'model_saved', NULL, '2025-11-17 08:05:30.000000', '2025-11-17 14:30:15.123456', '2025-11-17 08:00:00.000000', '2025-11-17 14:30:15.123456', 3, NULL, 0),
    -- 완료된 학습 (모델 4 - Naruto Style)
    ('Naruto Style', '나루토 스타일의 캐릭터를 생성합니다.', 180, 121, 0.00012, 24, 'Lykon/AnyLoRA', NULL, 'SUCCESS', 121, 121, 'model_saved', NULL, '2025-11-18 08:05:30.000000', '2025-11-18 15:30:15.123456', '2025-11-18 08:00:00.000000', '2025-11-18 15:30:15.123456', 4, NULL, 0),
    -- 완료된 학습 (모델 5 - Kamado Tanjiro)
    ('Kamado Tanjiro', '귀멸의 칼날 탄지로', 40, 130, 0.00015, 16, 'stablediffusionapi/anything-v5', NULL, 'SUCCESS', 130, 130, 'model_saved', NULL, '2025-11-20 08:05:30.000000', '2025-11-20 13:30:15.123456', '2025-11-20 08:00:00.000000', '2025-11-20 13:30:15.123456', 5, NULL, 0),
    -- 완료된 학습 (모델 6 - Animal Crossing Style)
    ('Animal Crossing Style', '동물의 숲 스타일 이미지를 생성합니다.', 30, 140, 0.0001, 16, 'stablediffusionapi/anything-v5', NULL, 'SUCCESS', 140, 140, 'model_saved', NULL, '2025-11-21 08:05:30.000000', '2025-11-21 14:30:15.123456', '2025-11-21 08:00:00.000000', '2025-11-21 14:30:15.123456', 6, NULL, 0),
    -- 완료된 학습 (모델 7 - Manga Outline Style)
    ('Manga Outline Style', '아웃라인 위주의 흑백 만화 스타일로 애니 캐릭터를 생성합니다.', 40, 150, 0.00015, 16, 'stablediffusionapi/anything-v5', NULL, 'SUCCESS', 150, 150, 'model_saved', NULL, '2025-11-23 08:05:30.000000', '2025-11-23 14:30:15.123456', '2025-11-23 08:00:00.000000', '2025-11-23 14:30:15.123456', 7, NULL, 0),
    -- 완료된 학습 (모델 8 - Zelda Style)
    ('Zelda Style', '젤다 야생의 숨결 스타일의 이미지를 생성합니다.', 40, 150, 0.00015, 16, 'Lykon/AnyLoRA', NULL, 'SUCCESS', 150, 150, 'model_saved', NULL, '2025-11-24 08:05:30.000000', '2025-11-24 14:30:15.123456', '2025-11-24 08:00:00.000000', '2025-11-24 14:30:15.123456', 8, NULL, 0),
    -- 완료된 학습 (모델 9 - Minimalist)
    ('Minimalist', '간결한 표현이 특징인 Lora', 40, 150, 0.00015, 16, 'stablediffusionapi/anything-v5', NULL, 'SUCCESS', 150, 150, 'model_saved', NULL, '2025-11-25 08:05:30.000000', '2025-11-25 14:30:15.123456', '2025-11-25 08:00:00.000000', '2025-11-25 14:30:15.123456', 9, NULL, 0),

    -- 테스트 유저(100)의 학습 작업들
    ('Test LoRA Model 1', 'My first custom LoRA model', 15, 100, 0.0001, 16, 'stablediffusionapi/anything-v5', 'test1', 'SUCCESS', 100, 100, 'model_saved', NULL, '2025-11-26 09:00:00.000000', '2025-11-26 11:30:15.123456', '2025-11-26 09:00:00.000000', '2025-11-26 11:30:15.123456', 10, NULL, 100),
    ('Test Anime Style', 'Anime style training test', 25, 120, 0.00008, 32, 'Lykon/AnyLoRA', 'anime_test', 'SUCCESS', 120, 120, 'model_saved', NULL, '2025-11-27 10:00:00.000000', '2025-11-27 13:20:15.123456', '2025-11-27 10:00:00.000000', '2025-11-27 13:20:15.123456', 11, NULL, 100),
    ('Failed Training Test', 'This training failed for testing', 20, 80, 0.0001, 16, 'stablediffusionapi/anything-v5', NULL, 'FAILED', 45, 80, NULL, 'Out of memory error', '2025-11-28 08:00:00.000000', '2025-11-28 09:30:00.000000', '2025-11-28 08:00:00.000000', '2025-11-28 09:30:00.000000', 12, NULL, 100);
-- 4. Tags
INSERT INTO tags (USAGE_COUNT, CREATED_AT, ID, UPDATED_AT, NAME, CATEGORY) VALUES
    -- STYLE
    (4, '2025-11-25 04:33:49.545265', 1, '2025-11-25 07:59:05.639056', 'anime', 'STYLE'),
    (1, '2025-11-25 04:33:49.545265', 4, '2025-11-25 04:33:49.545265', 'cartoon', 'STYLE'),
    (1, '2025-11-25 07:58:50.524822', 23, '2025-11-25 07:58:50.529814', 'style', 'STYLE'),
    -- CHARACTER
    (1, '2025-11-25 04:33:49.545265', 7, '2025-11-25 07:58:22.91937', 'girl', 'CHARACTER'),
    (1, '2025-11-25 04:33:49.545265', 9, '2025-11-25 04:33:49.545265', 'animal', 'CHARACTER'),
    (1, '2025-11-25 07:57:57.951871', 19, '2025-11-25 07:57:57.969741', 'man', 'CHARACTER'),
    (1, '2025-11-25 07:57:57.951833', 20, '2025-11-25 07:57:57.970467', 'conan', 'CHARACTER'),
    (2, '2025-11-25 07:58:08.703929', 21, '2025-11-25 07:58:22.933247', 'chainsaw', 'CHARACTER'),
    (1, '2025-11-25 07:58:50.536904', 24, '2025-11-25 07:58:50.538863', 'naruto', 'CHARACTER'),
    (1, '2025-11-25 07:59:05.631161', 25, '2025-11-25 07:59:05.645201', 'tanjiro', 'CHARACTER'),
    -- GENRE
    (1, '2025-11-25 07:58:33.211142', 22, '2025-11-25 07:58:33.225385', 'game', 'GENRE'),
    -- OTHER
    (2, '2025-11-25 04:33:49.545265', 16, '2025-11-25 07:58:33.228677', 'cute', 'OTHER');

-- 6. LoRA Models (유니코드 한글 복원)
-- 컬럼 순서: ID, USER_ID, TRAINING_JOB_ID, TITLE, DESCRIPTION, S3_KEY, FILE_SIZE, IS_PUBLIC, VIEW_COUNT, LIKE_COUNT, DELETED_AT, CREATED_AT, UPDATED_AT
INSERT INTO lora_models (ID, USER_ID, TRAINING_JOB_ID, TITLE, DESCRIPTION, S3_KEY, FILE_SIZE, IS_PUBLIC, VIEW_COUNT, LIKE_COUNT, DELETED_AT, CREATED_AT, UPDATED_AT) VALUES
                                                                                                                                                                         (1, 0, 1, 'Reze', '체인소맨 레제 만화버전', '0/Reze.safetensors', 15728640, TRUE, 505451, 14545, NULL, '2025-11-10 04:33:49.55569', '2025-11-25 14:57:27.828744'),
                                                                                                                                                                         (2, 0, 2, 'Pochita', '체인소맨 포치타', '1/Pochita.safetensors', 52428800, TRUE, 330, 45, NULL, '2025-11-15 04:33:49.55569', '2025-11-25 09:36:10.137369'),
                                                                                                                                                                         (3, 0, 3, 'Mouri Kogorou', '명탐정 코난 모리 코고로 weight 0.6 ~ 0.7 추천', '2/Mouri_Kogorou.safetensors', 104857600, TRUE, 594, 92, NULL, '2025-11-17 04:33:49.55569', '2025-11-25 14:58:41.714603'),
                                                                                                                                                                         (4, 0, 4, 'Naruto Style', '나루토 스타일의 캐릭터를 생성합니다.', '3/Naruto_Style.safetensors', 78643200, TRUE, 216, 38, NULL, '2025-11-18 04:33:49.55569', '2025-11-25 14:58:57.144276'),
                                                                                                                                                                         (5, 0, 5, 'Kamado Tanjiro', '귀멸의 칼날 탄지로', '4/Tanjiro.safetensors', 52428800, TRUE, 162, 29, NULL, '2025-11-20 04:33:49.55569', '2025-11-25 14:59:00.659209'),
                                                                                                                                                                         (6, 0, 6, 'Animal Crossing Style', '동물의 숲 스타일 이미지를 생성합니다.', '5/animal_crossing.safetensors', 62914560, TRUE, 331, 51, NULL, '2025-11-21 04:33:49.55569', '2025-11-25 14:58:48.42624'),
                                                                                                                                                                         (7, 0, 7, 'Manga Outline Style', '아웃라인 위주의 흑백 만화 스타일로 애니 캐릭터를 생성합니다.', '6/animeoutline.safetensors', 52428800, TRUE, 162, 29, NULL, '2025-11-23 04:33:49.55569', '2025-11-25 14:59:00.659209'),
                                                                                                                                                                         (8, 0, 8, 'Zelda Style', '젤다 야생의 숨결 스타일의 이미지를 생성합니다.', '7/zelda_style.safetensors', 52428800, TRUE, 162, 29, NULL, '2025-11-24 04:33:49.55569', '2025-11-25 14:59:00.659209'),
                                                                                                                                                                         (9, 0, 9, 'Minimalist', '간결한 표현이 특징인 Lora', '8/anime_minimalist.safetensors', 52428800, TRUE, 162, 29, NULL, '2025-11-25 04:33:49.55569', '2025-11-25 14:59:00.659209');

-- 6-1. Training Jobs의 MODEL_ID 업데이트 (LoRA Model 생성 후 연결)
UPDATE training_jobs SET model_id = 1 WHERE id = 1;
UPDATE training_jobs SET model_id = 2 WHERE id = 2;
UPDATE training_jobs SET model_id = 3 WHERE id = 3;
UPDATE training_jobs SET model_id = 4 WHERE id = 4;
UPDATE training_jobs SET model_id = 5 WHERE id = 5;
UPDATE training_jobs SET model_id = 6 WHERE id = 6;
UPDATE training_jobs SET model_id = 7 WHERE id = 7;
UPDATE training_jobs SET model_id = 8 WHERE id = 8;
UPDATE training_jobs SET model_id = 9 WHERE id = 9;

-- 7. Model Tags
INSERT INTO model_tags (CREATED_AT, ID, MODEL_ID, TAG_ID, UPDATED_AT) VALUES
                                                                          ('2025-11-25 04:33:49.565266', 1, 1, 1, '2025-11-25 04:33:49.565266'),
                                                                          ('2025-11-25 04:33:49.565266', 2, 1, 7, '2025-11-25 04:33:49.565266'),
                                                                          ('2025-11-25 04:33:49.565266', 15, 7, 4, '2025-11-25 04:33:49.565266'),
                                                                          ('2025-11-25 04:33:49.565266', 16, 7, 9, '2025-11-25 04:33:49.565266'),
                                                                          ('2025-11-25 04:33:49.565266', 17, 7, 16, '2025-11-25 04:33:49.565266'),
                                                                          ('2025-11-25 07:57:57.96647', 18, 3, 19, '2025-11-25 07:57:57.96647'),
                                                                          ('2025-11-25 07:57:57.969857', 19, 3, 20, '2025-11-25 07:57:57.969857'),
                                                                          ('2025-11-25 07:58:08.705449', 20, 1, 21, '2025-11-25 07:58:08.705449'),
                                                                          ('2025-11-25 07:58:22.92426', 21, 2, 1, '2025-11-25 07:58:22.92426'),
                                                                          ('2025-11-25 07:58:22.932566', 22, 2, 21, '2025-11-25 07:58:22.932566'),
                                                                          ('2025-11-25 07:58:33.224569', 23, 6, 22, '2025-11-25 07:58:33.224569'),
                                                                          ('2025-11-25 07:58:33.227965', 24, 6, 16, '2025-11-25 07:58:33.227965'),
                                                                          ('2025-11-25 07:58:50.526261', 25, 4, 23, '2025-11-25 07:58:50.526261'),
                                                                          ('2025-11-25 07:58:50.538307', 26, 4, 24, '2025-11-25 07:58:50.538307'),
                                                                          ('2025-11-25 07:58:50.539342', 27, 4, 1, '2025-11-25 07:58:50.539342'),
                                                                          ('2025-11-25 07:59:05.638332', 28, 5, 1, '2025-11-25 07:59:05.638332'),
                                                                          ('2025-11-25 07:59:05.644517', 29, 5, 25, '2025-11-25 07:59:05.644517');

-- 8. Generation History (기존 데이터 + 새 데이터 통합)
INSERT INTO generation_history (CURRENT_STEP, GUIDANCE_SCALE, LORA_SCALE, NUM_IMAGES, STEPS, TOTAL_STEPS, CREATED_AT, ID, MODEL_ID, SEED, UPDATED_AT, USER_ID, STATUS, ERROR_MESSAGE, NEGATIVE_PROMPT, PROMPT) VALUES
    -- ID 1-9: 기존 샘플 데이터 (각 모델별 1개씩)
    (NULL, 6.50, 1.0, 2, 35, NULL, '2025-11-19 02:00:00', 1, 1, 12345, '2025-11-24 02:05:00', 0, 'SUCCESS', NULL, 'ugly, lowres, bad anatomy', 'sks, manga girl, black hair, white shirt'),
    (NULL, 9.00, 1.0, 2, 40, NULL, '2025-11-19 03:00:00', 2, 2, 111222, '2025-11-24 03:05:00', 0, 'SUCCESS', NULL, 'cartoon, low quality', 'pochita, chainsaw, no humans'),
    (NULL, 8.50, 1.0, 2, 35, NULL, '2025-11-19 04:00:00', 3, 3, 777888, '2025-11-24 04:05:00', 0, 'SUCCESS', NULL, 'dark, blurry', '1boy, solo, black eyes, mustache, black hair'),
    (NULL, 8.00, 1.0, 2, 30, NULL, '2025-11-19 05:00:00', 4, 4, 131313, '2025-11-24 05:05:00', 0, 'SUCCESS', NULL, 'blurry, organic', 'manga character, ninja, orange jacket'),
    (NULL, 6.50, 1.0, 2, 22, NULL, '2025-11-19 06:00:00', 5, 5, 151515, '2025-11-24 06:05:00', 0, 'SUCCESS', NULL, 'cute, sharp', 'watercolor, boy, checkered haori'),
    (NULL, 7.00, 1.0, 3, 20, NULL, '2025-11-19 07:00:00', 6, 6, 161616, '2025-11-24 07:05:00', 0, 'SUCCESS', NULL, 'realistic, dark', 'cute animal, cartoon style'),
    (NULL, 7.00, 1.0, 3, 20, NULL, '2025-11-19 08:00:00', 7, 7, 171717, '2025-11-24 08:05:00', 0, 'SUCCESS', NULL, 'EasyNegative, badhandv4', 'masterpiece, best quality, 1girl, solo, lineart, monochrome'),
    (NULL, 9.00, 1.0, 3, 28, NULL, '2025-11-19 09:00:00', 8, 8, 181818, '2025-11-24 09:05:00', 0, 'SUCCESS', NULL, 'painting by bad-artist, watermark, text, blurry', 'botw style, zelda, breath of the wild'),
    (NULL, 7.20, 1.0, 2, 22, NULL, '2025-11-19 10:00:00', 9, 9, 191919, '2025-11-24 10:05:00', 0, 'SUCCESS', NULL, 'worst quality, low quality, realistic', 'anime minimalist, 1girl, solo'),

    -- ID 10-12: Model 1 초기 테스트
    (NULL, 7.50, 1.00, 1, 35, NULL, '2025-11-20 04:33:49.59053', 10, 1, 111111, '2025-11-25 04:33:49.59053', 0, 'SUCCESS', NULL, 'ugly, deformed, noisy, blurry, distorted', 'sks, manga girl, black hair, white shirt'),
    (NULL, 8.00, 1.00, 1, 30, NULL, '2025-11-21 04:33:49.59053', 11, 1, 222222, '2025-11-25 04:33:49.59053', 0, 'SUCCESS', NULL, 'ugly, deformed, noisy, blurry, distorted', 'sks, manga girl, black hair, white shirt'),
    (NULL, 7.80, 1.00, 1, 28, NULL, '2025-11-22 04:33:49.59053', 12, 1, 333333, '2025-11-25 04:33:49.59053', 0, 'SUCCESS', NULL, 'deformed, blurry, bad anatomy', 'sks, manga girl, black hair, white shirt'),

    -- ID 13: Model 6 첫 성공
    (21, 7.00, 1.00, 1, 20, 20, '2025-11-25 04:40:15.998897', 13, 6, NULL, '2025-11-25 04:43:00.146636', 0, 'SUCCESS', NULL, 'text,username,logo,(low quality, worst quality:1.4), bad anatomy', 'masterpiece, best quality, chibi, cute animal'),

    -- ID 14-16: 삭제된 레코드 (FAILED 또는 테스트)
    (NULL, 7.50, 1.00, 1, 20, NULL, '2025-11-25 05:00:00', 14, 6, NULL, '2025-11-25 05:00:00', 0, 'FAILED', 'Training data error', 'text, low quality', 'test prompt'),
    (NULL, 7.50, 1.00, 1, 20, NULL, '2025-11-25 05:05:00', 15, 6, NULL, '2025-11-25 05:05:00', 0, 'FAILED', 'Training data error', 'text, low quality', 'test prompt'),
    (NULL, 7.50, 1.00, 1, 20, NULL, '2025-11-25 05:08:00', 16, 6, NULL, '2025-11-25 05:08:00', 0, 'FAILED', 'Training data error', 'text, low quality', 'test prompt'),

    -- ID 17-26: Model 6 실패 케이스들
    (21, 7.50, 1.00, 1, 20, 20, '2025-11-25 05:10:45.587569', 17, 6, NULL, '2025-11-25 05:11:59.44949', 0, 'FAILED', 'Input type (c10::Half) and bias type (float) should be the same', 'text, username, low quality', 'masterpiece, best quality'),
    (21, 7.50, 1.00, 1, 20, 20, '2025-11-25 05:14:32.873869', 18, 6, NULL, '2025-11-25 05:15:44.496064', 0, 'FAILED', 'Input type (c10::Half) and bias type (float) should be the same', 'text, username, low quality', 'masterpiece, best quality'),
    (19, 7.50, 1.00, 1, 20, 20, '2025-11-25 05:19:05.454201', 19, 6, NULL, '2025-11-25 05:19:12.086869', 0, 'FAILED', 'Input type (c10::Half) and bias type (float) should be the same', 'text, username, low quality', 'masterpiece, best quality'),
    (18, 7.50, 1.00, 1, 20, 20, '2025-11-25 05:20:50.127978', 20, 6, NULL, '2025-11-25 05:20:56.799297', 0, 'FAILED', 'Input type (c10::Half) and bias type (float) should be the same', 'text, username, low quality', 'masterpiece, best quality'),
    (21, 7.50, 1.00, 1, 20, 20, '2025-11-25 05:26:33.60939', 21, 6, NULL, '2025-11-25 05:28:32.573772', 0, 'FAILED', 'Input type (c10::Half) and bias type (float) should be the same', 'text, username, low quality', 'masterpiece, best quality'),
    (21, 7.50, 1.00, 1, 20, 20, '2025-11-25 05:31:11.557386', 22, 6, NULL, '2025-11-25 05:32:23.203915', 0, 'FAILED', 'Input type (c10::Half) and bias type (float) should be the same', 'text, username, low quality', 'masterpiece, best quality'),
    (NULL, 7.50, 1.00, 1, 20, NULL, '2025-11-25 05:35:00', 23, 6, NULL, '2025-11-25 05:35:00', 0, 'FAILED', 'Training error', 'text, low quality', 'test'),
    (NULL, 7.50, 1.00, 1, 20, NULL, '2025-11-25 05:40:00', 24, 6, NULL, '2025-11-25 05:40:00', 0, 'FAILED', 'Training error', 'text, low quality', 'test'),
    (31, 7.50, 1.00, 1, 30, 30, '2025-11-25 05:50:10.081894', 25, 6, NULL, '2025-11-25 05:52:26.177882', 0, 'FAILED', 'permute(sparse_coo): number of dimensions in the tensor input does not match', 'text, username, low quality', 'masterpiece, best quality'),
    (NULL, 7.50, 1.00, 1, 20, NULL, '2025-11-25 05:55:00', 26, 6, NULL, '2025-11-25 05:55:00', 0, 'FAILED', 'Training error', 'text, low quality', 'test'),

    -- ID 27-34: Model 6 성공 케이스들
    (21, 7.50, 1.00, 1, 20, 20, '2025-11-25 05:57:06.222053', 27, 6, NULL, '2025-11-25 06:00:04.624866', 0, 'SUCCESS', NULL, 'text,username,logo,(low quality, worst quality:1.4), bad anatomy', 'masterpiece, best quality, chibi, cute'),
    (21, 7.50, 1.00, 1, 20, 20, '2025-11-25 06:04:21.222919', 28, 6, NULL, '2025-11-25 06:06:17.574119', 0, 'SUCCESS', NULL, 'text,username,logo,(low quality, worst quality:1.4), bad anatomy', 'masterpiece, best quality, chibi, cute'),
    (20, 7.50, 1.00, 1, 20, 20, '2025-11-25 06:09:42.384939', 29, 6, NULL, '2025-11-25 06:12:19.812421', 0, 'SUCCESS', NULL, 'text,username,logo,(low quality, worst quality:1.4), bad anatomy', 'masterpiece, best quality, chibi, cute'),
    (20, 7.50, 1.00, 1, 20, 20, '2025-11-25 06:18:13.672308', 30, 6, NULL, '2025-11-25 06:20:04.495844', 0, 'SUCCESS', NULL, 'text,username,logo,(low quality, worst quality:1.4), bad anatomy', 'masterpiece, best quality, chibi, cute'),
    (20, 7.50, 1.00, 1, 20, 20, '2025-11-25 06:27:25.945889', 31, 6, NULL, '2025-11-25 06:29:16.265169', 0, 'SUCCESS', NULL, 'text,username,logo,(low quality, worst quality:1.4), bad anatomy', 'masterpiece, best quality, chibi, cute'),
    (20, 7.50, 1.00, 1, 20, 20, '2025-11-25 06:31:08.056271', 32, 6, NULL, '2025-11-25 06:33:37.580035', 0, 'SUCCESS', NULL, 'text,username,logo,(low quality, worst quality:1.4), bad anatomy', 'masterpiece, best quality, chibi, cute'),
    (20, 7.50, 1.00, 1, 20, 20, '2025-11-25 06:43:57.387647', 33, 6, NULL, '2025-11-25 06:46:05.344298', 0, 'SUCCESS', NULL, 'text,username,logo,(low quality, worst quality:1.4), bad anatomy', 'masterpiece, best quality, chibi, cute'),
    (20, 7.00, 1.00, 1, 20, 20, '2025-11-25 07:19:41.443614', 34, 6, NULL, '2025-11-25 07:22:58.23772', 0, 'SUCCESS', NULL, 'text,username,logo,(low quality, worst quality:1.4), bad anatomy', 'masterpiece, best quality, chibi, cute'),

    -- ID 35-47: 다양한 모델 테스트
    (31, 7.50, 0.65, 1, 30, 30, '2025-11-25 07:28:41.420707', 35, 3, NULL, '2025-11-25 07:30:33.217834', 0, 'SUCCESS', NULL, 'lowres, bad anatomy, bad hands, text', '1boy,solo, black eyes,mustache,black hair'),
    (30, 7.50, 0.90, 1, 30, 30, '2025-11-25 07:33:15.003823', 36, 4, NULL, '2025-11-25 07:35:15.133305', 0, 'SUCCESS', NULL, 'lowres, bad anatomy, bad hands, text', 'a girl, manga style, naruto'),
    (31, 7.50, 0.65, 1, 30, 30, '2025-11-25 07:43:44.423523', 37, 3, NULL, '2025-11-25 07:45:56.880237', 0, 'SUCCESS', NULL, 'lowres, bad anatomy, bad hands, text', '1boy,solo, black eyes,mustache,black hair'),
    (31, 7.50, 0.90, 1, 30, 30, '2025-11-25 07:47:59.602794', 38, 2, NULL, '2025-11-25 07:49:51.461172', 0, 'SUCCESS', NULL, 'lowres, bad anatomy, bad hands, text', 'pochita, chainsaw, no humans'),
    (31, 7.50, 1.00, 1, 30, 30, '2025-11-25 07:54:16.716238', 39, 5, NULL, '2025-11-25 07:56:06.543725', 0, 'SUCCESS', NULL, 'lowres, bad anatomy, bad hands, text', 'demon slayer, tanjiro, checkered haori'),
    (40, 6.50, 1.20, 1, 40, 40, '2025-11-25 07:59:43.429086', 40, 1, NULL, '2025-11-25 08:01:47.780932', 0, 'SUCCESS', NULL, 'lowres, bad anatomy, bad hands, text', 'sks, a manga girl, black hair, white shirt'),
    (31, 6.00, 2.00, 1, 30, 30, '2025-11-25 08:05:14.879683', 41, 1, NULL, '2025-11-25 08:06:50.459734', 0, 'SUCCESS', NULL, 'lowres, bad anatomy, bad hands, text', 'sks, a manga girl, black hair, white shirt'),
    (31, 6.00, 1.00, 1, 30, 30, '2025-11-25 08:09:27.344109', 42, 1, NULL, '2025-11-25 08:11:08.948651', 0, 'SUCCESS', NULL, 'lowres, bad anatomy, bad hands, text', 'sks, a manga girl, black hair, white shirt'),
    (41, 6.50, 1.00, 1, 40, 40, '2025-11-25 08:11:45.271014', 43, 1, NULL, '2025-11-25 08:13:47.525713', 0, 'SUCCESS', NULL, 'lowres, bad anatomy, bad hands, text', 'sks, a manga girl, black hair, white shirt'),
    (31, 6.00, 1.10, 1, 30, 30, '2025-11-25 08:17:00.202881', 44, 1, NULL, '2025-11-25 08:18:37.892771', 0, 'SUCCESS', NULL, 'lowres, bad anatomy, bad hands, text', 'sks, a manga girl, black hair, white shirt'),
    (31, 6.00, 1.10, 1, 30, 30, '2025-11-25 08:28:07.358186', 45, 1, NULL, '2025-11-25 08:30:26.993585', 0, 'SUCCESS', NULL, 'lowres, bad anatomy, bad hands, text', 'sks, a manga girl, black hair, white shirt'),
    (31, 6.50, 1.10, 1, 30, 30, '2025-11-25 08:38:45.113346', 46, 1, NULL, '2025-11-25 08:41:06.044598', 0, 'SUCCESS', NULL, 'lowres, bad anatomy, bad hands, text', 'sks, a manga girl, black hair, white shirt'),
    (31, 6.50, 1.10, 1, 30, 30, '2025-11-25 09:13:07.933755', 47, 1, NULL, '2025-11-25 09:14:54.010376', 0, 'SUCCESS', NULL, 'lowres, bad anatomy, bad hands, text', 'sks, a manga girl, black hair, white shirt'),

    -- ID 48-49: 최신 생성
    (30, 7.50, 0.90, 2, 30, 30, '2025-11-25 09:16:18.459402', 48, 6, NULL, '2025-11-25 09:18:08.980654', 0, 'SUCCESS', NULL, 'lowres, bad anatomy, bad hands, text', 'chibi, cute animal, cartoon style'),
    (30, 7.50, 1.00, 1, 30, 30, '2025-11-25 09:37:23.372027', 49, 2, NULL, '2025-11-25 09:39:04.982155', 0, 'SUCCESS', NULL, 'lowres, bad anatomy, bad hands, text', 'pochita, chainsaw, no humans'),

    -- ID 50-54: 테스트 유저(100)의 생성 기록
    (NULL, 7.00, 1.00, 2, 25, NULL, '2025-11-26 10:00:00', 50, 1, 54321, '2025-11-26 10:03:00', 100, 'SUCCESS', NULL, 'ugly, lowres, bad anatomy', 'sks, manga girl, test image'),
    (NULL, 7.50, 1.00, 1, 30, NULL, '2025-11-26 11:00:00', 51, 3, 77777, '2025-11-26 11:04:00', 100, 'SUCCESS', NULL, 'dark, blurry', '1boy, solo, black eyes, test character'),
    (NULL, 8.00, 0.90, 3, 28, NULL, '2025-11-26 12:00:00', 52, 6, 88888, '2025-11-26 12:05:00', 100, 'SUCCESS', NULL, 'realistic, dark', 'chibi, cute animal, test'),
    (NULL, 7.20, 1.10, 1, 22, NULL, '2025-11-26 13:00:00', 53, 4, 99999, '2025-11-26 13:03:00', 100, 'SUCCESS', NULL, 'worst quality, low quality', 'naruto style, test ninja'),
    (NULL, 6.50, 1.00, 2, 20, NULL, '2025-11-26 14:00:00', 54, 9, 11111, '2025-11-26 14:02:00', 100, 'SUCCESS', NULL, 'realistic', 'anime minimalist, 1girl, test');

-- 9. Generated Images (기존 + 새 데이터 통합)
INSERT INTO generated_images (DISPLAY_ORDER, CREATED_AT, FILE_SIZE, GENERATION_HISTORY_ID, ID, UPDATED_AT, S3_KEY, S3_URL) VALUES
    -- ID 1-21: 기존 샘플 이미지 (gen_hist 1-9 참조)
    (1, '2025-11-19 02:05:00', 1024000, 1, 1, '2025-11-25 02:05:00', '0/reze.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/reze.png'),
    (2, '2025-11-19 02:05:00', 1024000, 1, 2, '2025-11-25 02:05:00', '0/20251111_181540_3.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/20251111_181540_3.png'),
    (1, '2025-11-19 03:05:00', 1024000, 2, 3, '2025-11-25 03:05:00', '0/pochita_1.jpeg', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/pochita_1.jpeg'),
    (2, '2025-11-19 03:05:00', 1024000, 2, 4, '2025-11-25 03:05:00', '0/pochita_2.jpeg', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/pochita_2.jpeg'),
    (1, '2025-11-19 04:05:00', 1024000, 3, 5, '2025-11-25 04:05:00', '0/2.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/2.png'),
    (2, '2025-11-19 04:05:00', 1024000, 3, 6, '2025-11-25 04:05:00', '0/3.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/3.png'),
    (1, '2025-11-19 05:05:00', 1024000, 4, 7, '2025-11-25 05:05:00', '0/naru_11.webp', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/naru_11.webp'),
    (2, '2025-11-19 05:05:00', 1024000, 4, 8, '2025-11-25 05:05:00', '0/naru_22.webp', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/naru_22.webp'),
    (1, '2025-11-19 06:05:00', 1024000, 5, 9, '2025-11-25 06:05:00', '0/tanji_1.webp', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/tanji_1.webp'),
    (2, '2025-11-19 06:05:00', 1024000, 5, 10, '2025-11-25 06:05:00', '0/tanji_22.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/tanji_22.png'),
    (1, '2025-11-19 07:05:00', 1024000, 6, 11, '2025-11-25 07:05:00', '0/animal_1.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/animal_1.png'),
    (2, '2025-11-19 07:05:00', 1024000, 6, 12, '2025-11-25 07:05:00', '0/animal_2.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/animal_2.png'),
    (3, '2025-11-19 07:05:00', 1024000, 6, 13, '2025-11-25 07:05:00', '0/animal_3.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/animal_3.png'),
    (1, '2025-11-19 08:05:00', 1024000, 7, 14, '2025-11-25 08:05:00', '0/manga_2.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/manga_2.png'),
    (2, '2025-11-19 08:05:00', 1024000, 7, 15, '2025-11-25 08:05:00', '0/manga_1.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/manga_1.png'),
    (3, '2025-11-19 08:05:00', 1024000, 7, 16, '2025-11-25 08:05:00', '0/manga_3.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/manga_3.png'),
    (1, '2025-11-19 09:05:00', 1024000, 8, 17, '2025-11-25 09:05:00', '0/zelda_1.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/zelda_1.png'),
    (2, '2025-11-19 09:05:00', 1024000, 8, 18, '2025-11-25 09:05:00', '0/zelda_2.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/zelda_2.png'),
    (3, '2025-11-19 09:05:00', 1024000, 8, 19, '2025-11-25 09:05:00', '0/zelda_3.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/zelda_3.png'),
    (1, '2025-11-19 10:05:00', 1024000, 9, 20, '2025-11-25 10:05:00', '0/minial_1.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/minial_1.png'),
    (2, '2025-11-19 10:05:00', 1024000, 9, 21, '2025-11-25 10:05:00', '0/minial_2.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/minial_2.png'),

    -- ID 22: gen_hist 10 (Model 1 초기)
    (1, '2025-11-20 04:33:49.591612', 2048576, 10, 22, '2025-11-25 04:33:49.591612', '0/reze.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/reze.png'),

    -- ID 23: gen_hist 13 (Model 6 첫 성공)
    (1, '2025-11-25 04:43:00.079819', NULL, 13, 23, '2025-11-25 04:43:00.079819', 'user-0/20251125_044257_1.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/user-0/20251125_044257_1.png'),

    -- ID 24-31: gen_hist 27-34 (Model 6 성공 케이스들)
    (1, '2025-11-25 06:00:04.617775', NULL, 27, 24, '2025-11-25 06:00:04.617775', 'user-0/20251125_060001_1.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/user-0/20251125_060001_1.png'),
    (1, '2025-11-25 06:06:17.572395', NULL, 28, 25, '2025-11-25 06:06:17.572395', 'user-0/20251125_060614_1.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/user-0/20251125_060614_1.png'),
    (1, '2025-11-25 06:12:19.810484', NULL, 29, 26, '2025-11-25 06:12:19.810484', 'user-0/20251125_061215_1.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/user-0/20251125_061215_1.png'),
    (1, '2025-11-25 06:20:04.493617', NULL, 30, 27, '2025-11-25 06:20:04.493617', 'user-0/20251125_062000_1.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/user-0/20251125_062000_1.png'),
    (1, '2025-11-25 06:29:16.263295', NULL, 31, 28, '2025-11-25 06:29:16.263295', 'user-0/20251125_062912_1.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/user-0/20251125_062912_1.png'),
    (1, '2025-11-25 06:33:37.578199', NULL, 32, 29, '2025-11-25 06:33:37.578199', 'user-0/20251125_063333_1.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/user-0/20251125_063333_1.png'),
    (1, '2025-11-25 06:46:05.342122', NULL, 33, 30, '2025-11-25 06:46:05.342122', 'user-0/20251125_064601_1.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/user-0/20251125_064601_1.png'),
    (1, '2025-11-25 07:22:58.236053', NULL, 34, 31, '2025-11-25 07:22:58.236053', 'user-0/20251125_072253_1.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/user-0/20251125_072253_1.png'),

    -- ID 32-36: gen_hist 35-39 (다양한 모델 테스트)
    (1, '2025-11-25 07:30:33.216266', NULL, 35, 32, '2025-11-25 07:30:33.216266', 'user-0/20251125_073030_1.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/user-0/20251125_073030_1.png'),
    (1, '2025-11-25 07:35:15.131264', NULL, 36, 33, '2025-11-25 07:35:15.131264', 'user-0/20251125_073511_1.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/user-0/20251125_073511_1.png'),
    (1, '2025-11-25 07:45:56.878524', NULL, 37, 34, '2025-11-25 07:45:56.878524', 'user-0/20251125_074553_1.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/user-0/20251125_074553_1.png'),
    (1, '2025-11-25 07:49:51.459664', NULL, 38, 35, '2025-11-25 07:49:51.459664', 'user-0/20251125_074948_1.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/user-0/20251125_074948_1.png'),
    (1, '2025-11-25 07:56:06.542244', NULL, 39, 36, '2025-11-25 07:56:06.542244', 'user-0/20251125_075603_1.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/user-0/20251125_075603_1.png'),

    -- ID 37-44: gen_hist 40-47 (Model 1 다양한 파라미터 테스트)
    (1, '2025-11-25 08:01:47.774956', NULL, 40, 37, '2025-11-25 08:01:47.774956', 'user-0/20251125_080143_1.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/user-0/20251125_080143_1.png'),
    (1, '2025-11-25 08:06:50.458179', NULL, 41, 38, '2025-11-25 08:06:50.458179', 'user-0/20251125_080647_1.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/user-0/20251125_080647_1.png'),
    (1, '2025-11-25 08:11:08.947198', NULL, 42, 39, '2025-11-25 08:11:08.947198', 'user-0/20251125_081106_1.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/user-0/20251125_081106_1.png'),
    (1, '2025-11-25 08:13:47.524268', NULL, 43, 40, '2025-11-25 08:13:47.524268', 'user-0/20251125_081343_1.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/user-0/20251125_081343_1.png'),
    (1, '2025-11-25 08:18:37.889291', NULL, 44, 41, '2025-11-25 08:18:37.889291', 'user-0/20251125_081834_1.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/user-0/20251125_081834_1.png'),
    (1, '2025-11-25 08:30:26.990081', NULL, 45, 42, '2025-11-25 08:30:26.990081', 'user-0/20251125_083023_1.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/user-0/20251125_083023_1.png'),
    (1, '2025-11-25 08:41:06.043313', NULL, 46, 43, '2025-11-25 08:41:06.043313', 'user-0/20251125_084102_1.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/user-0/20251125_084102_1.png'),
    (1, '2025-11-25 09:14:54.008874', NULL, 47, 44, '2025-11-25 09:14:54.008874', 'user-0/20251125_091451_1.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/user-0/20251125_091451_1.png'),

    -- ID 45-47: gen_hist 48-49 (최신 생성, 48은 2개 이미지)
    (1, '2025-11-25 09:18:08.978871', NULL, 48, 45, '2025-11-25 09:18:08.978871', 'user-0/20251125_091757_1.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/user-0/20251125_091757_1.png'),
    (2, '2025-11-25 09:18:08.979685', NULL, 48, 46, '2025-11-25 09:18:08.979685', 'user-0/20251125_091804_2.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/user-0/20251125_091804_2.png'),
    (1, '2025-11-25 09:39:04.980634', NULL, 49, 47, '2025-11-25 09:39:04.980634', 'user-0/20251125_093900_1.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/user-0/20251125_093900_1.png'),

    -- ID 48-56: gen_hist 50-54 (테스트 유저의 생성 이미지 - 기존 모델 샘플 재사용)
    -- gen_hist 50 (Model 1 - Reze): 기존 이미지 1, 2 재사용
    (1, '2025-11-26 10:03:00', 1024000, 50, 48, '2025-11-26 10:03:00', '0/reze.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/reze.png'),
    (2, '2025-11-26 10:03:00', 1024000, 50, 49, '2025-11-26 10:03:00', '0/20251111_181540_3.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/20251111_181540_3.png'),
    -- gen_hist 51 (Model 3 - Mouri): 기존 이미지 5 재사용
    (1, '2025-11-26 11:04:00', 1024000, 51, 50, '2025-11-26 11:04:00', '0/2.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/2.png'),
    -- gen_hist 52 (Model 6 - Animal Crossing): 기존 이미지 11, 12, 13 재사용
    (1, '2025-11-26 12:05:00', 1024000, 52, 51, '2025-11-26 12:05:00', '0/animal_1.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/animal_1.png'),
    (2, '2025-11-26 12:05:00', 1024000, 52, 52, '2025-11-26 12:05:00', '0/animal_2.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/animal_2.png'),
    (3, '2025-11-26 12:05:00', 1024000, 52, 53, '2025-11-26 12:05:00', '0/animal_3.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/animal_3.png'),
    -- gen_hist 53 (Model 4 - Naruto): 기존 이미지 7 재사용
    (1, '2025-11-26 13:03:00', 1024000, 53, 54, '2025-11-26 13:03:00', '0/naru_11.webp', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/naru_11.webp'),
    -- gen_hist 54 (Model 9 - Minimalist): 기존 이미지 20, 21 재사용
    (1, '2025-11-26 14:02:00', 1024000, 54, 55, '2025-11-26 14:02:00', '0/minial_1.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/minial_1.png'),
    (2, '2025-11-26 14:02:00', 1024000, 54, 56, '2025-11-26 14:02:00', '0/minial_2.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/minial_2.png');

-- 10. Model Samples (기존 데이터 유지)
INSERT INTO model_samples (DISPLAY_ORDER, IS_PRIMARY, CREATED_AT, GENERATED_IMAGE_ID, ID, MODEL_ID, UPDATED_AT) VALUES
    -- Model 1 samples (generated_images ID 1, 2 사용)
    (1, TRUE, '2025-11-25 04:33:49.56957', 1, 1, 1, '2025-11-25 04:33:49.56957'),
    (2, FALSE, '2025-11-25 04:33:49.56957', 2, 2, 1, '2025-11-25 04:33:49.56957'),

    -- Model 2 samples (generated_images ID 3, 4 사용)
    (1, TRUE, '2025-11-25 04:33:49.56957', 3, 3, 2, '2025-11-25 04:33:49.56957'),
    (2, FALSE, '2025-11-25 04:33:49.56957', 4, 4, 2, '2025-11-25 04:33:49.56957'),

    -- Model 3 samples (generated_images ID 5, 6 사용)
    (1, TRUE, '2025-11-25 04:33:49.56957', 5, 5, 3, '2025-11-25 04:33:49.56957'),
    (2, FALSE, '2025-11-25 04:33:49.56957', 6, 6, 3, '2025-11-25 04:33:49.56957'),

    -- Model 4 samples (generated_images ID 7, 8 사용)
    (1, TRUE, '2025-11-25 04:33:49.56957', 7, 7, 4, '2025-11-25 04:33:49.56957'),
    (2, FALSE, '2025-11-25 04:33:49.56957', 8, 8, 4, '2025-11-25 04:33:49.56957'),

    -- Model 5 samples (generated_images ID 9, 10 사용)
    (1, TRUE, '2025-11-25 04:33:49.56957', 9, 9, 5, '2025-11-25 04:33:49.56957'),
    (2, FALSE, '2025-11-25 04:33:49.56957', 10, 10, 5, '2025-11-25 04:33:49.56957'),

    -- Model 6 samples (generated_images ID 11, 12, 13 사용)
    (1, TRUE, '2025-11-25 04:33:49.56957', 11, 11, 6, '2025-11-25 04:33:49.56957'),
    (2, FALSE, '2025-11-25 04:33:49.56957', 12, 12, 6, '2025-11-25 04:33:49.56957'),
    (3, FALSE, '2025-11-25 04:33:49.56957', 13, 13, 6, '2025-11-25 04:33:49.56957'),

    -- Model 7 samples (generated_images ID 14, 15, 16 사용)
    (1, TRUE, '2025-11-25 04:33:49.56957', 14, 14, 7, '2025-11-25 04:33:49.56957'),
    (2, FALSE, '2025-11-26 04:33:49.56957', 15, 15, 7, '2025-11-25 04:33:49.56957'),
    (3, FALSE, '2025-11-25 04:33:49.56957', 16, 16, 7, '2025-11-25 04:33:49.56957'),

    -- Model 8 samples (generated_images ID 17, 18, 19 사용)
    (1, TRUE, '2025-11-25 04:33:49.56957', 17, 17, 8, '2025-11-25 04:33:49.56957'),
    (2, FALSE, '2025-11-25 04:33:49.56957', 18, 18, 8, '2025-11-25 04:33:49.56957'),
    (3, FALSE, '2025-11-25 04:33:49.56957', 19, 19, 8, '2025-11-25 04:33:49.56957'),

    -- Model 9 samples (generated_images ID 20, 21 사용)
    (1, TRUE, '2025-11-25 04:33:49.56957', 20, 20, 9, '2025-11-25 04:33:49.56957'),
    (2, FALSE, '2025-11-25 04:33:49.56957', 21, 21, 9, '2025-11-25 04:33:49.56957');

-- 11. Model Prompts (유니코드 한글 복원)
INSERT INTO model_prompts (DISPLAY_ORDER, CREATED_AT, ID, MODEL_ID, UPDATED_AT, TITLE, DESCRIPTION, NEGATIVE_PROMPT, PROMPT) VALUES
                                                                                                                                 (1, '2025-11-25 04:33:49.572518', 1, 1, '2025-11-25 04:33:49.572518', 'Reze Trigger', '기본적인 만화 소녀', 'lowres, bad anatomy, bad hands, text, error, missing fingers, extra digit, fewer digits, cropped, worst quality, low quality', 'sks, a manga girl with black hair and a white shirt, mangastyle, monochrome,black and white, grayscale'),
                                                                                                                                 (2, '2025-11-25 04:33:49.572518', 2, 2, '2025-11-25 04:33:49.572518', 'pochita pochita Trigger', 'trigger', 'lowres, bad anatomy, bad hands, text, error, missing fingers, extra digit, fewer digits, cropped, worst quality, low quality ', 'pochita, chainsaw, no humans'),
                                                                                                                                 (1, '2025-11-25 04:33:49.572518', 3, 3, '2025-11-25 14:58:41.654117', 'Mouri Kogorou Trigger', '', 'lowres, bad anatomy, bad hands, text, error, missing fingers, extra digit, fewer digits, cropped, worst quality, low quality', '1boy,solo, black eyes,mustache,black hair'),
                                                                                                                                 (1, '2025-11-25 04:33:49.572518', 4, 4, '2025-11-25 04:33:49.572518', 'Naruto Style', 'Trigger', 'realistic', 'fantasy castle...'),
                                                                                                                                 (1, '2025-11-25 04:33:49.572518', 5, 5, '2025-11-25 04:33:49.572518', 'Kamado Tanjiro Trigger', 'Trigger', 'realistic', 'demon slayer, tanjiro'),
                                                                                                                                 (1, '2025-11-25 04:33:49.572518', 6, 6, '2025-11-25 04:33:49.572518', 'Animal Crossing Trigger', 'Trigger', 'low quality', 'chibi, 3d render'),
                                                                                                                                 (1, '2025-11-25 04:33:49.572518', 7, 7, '2025-11-25 04:33:49.572518', 'Trigger', '귀여운 동물', 'masterpiece, best quality, 1girl, solo, long_hair, looking_at_viewer, smile, bangs, skirt, shirt, long_sleeves, hat, dress, bow, holding, closed_mouth, flower, frills, hair_flower, petals, bouquet, holding_flower, center_frills, bonnet, holding_bouquet, flower field, flower field, lineart, monochrome, <lora:animeoutlineV4_16:1>', 'lineart, monochrome'),
                                                                                                                                 (1, '2025-11-25 04:33:49.572518', 8, 8, '2025-11-25 04:33:49.572518', 'Trigger', '귀여운 동물', '(painting by bad-artist-anime:0.9), (painting by bad-artist:0.9), watermark, text, error, blurry, jpeg artifacts, cropped, worst quality, low quality, normal quality, jpeg artifacts, (signature), watermark, username, artist name, (worst quality, low quality:1.4), bad anatomy', 'botw style'),
                                                                                                                                 (1, '2025-11-25 04:33:49.572518', 9, 9, '2025-11-25 04:33:49.572518', 'Trigger', '귀여운 동물', '(worst quality, low quality:1.4), (realistic, lip, nose, tooth, rouge, lipstick, eyeshadow:1.0), (abs, muscular, rib:1.0),', 'anime minimalist');

-- 12. Model Likes (테스트 유저의 좋아요 목록)
INSERT INTO model_likes (CREATED_AT, ID, MODEL_ID, UPDATED_AT, USER_ID) VALUES
    ('2025-11-26 10:00:00.000000', 1, 1, '2025-11-26 10:00:00.000000', 100),  -- 테스트 유저가 Model 1 (Reze) 좋아요
    ('2025-11-26 10:30:00.000000', 2, 6, '2025-11-26 10:30:00.000000', 100);  -- 테스트 유저가 Model 6 (Animal Crossing Style) 좋아요

-- 13. Comments (각 모델에 댓글 하나씩, fake_user1과 fake_user2가 골고루 작성)
-- 컬럼 순서: CONTENT, LIKE_COUNT, CREATED_AT, ID, MODEL_ID, UPDATED_AT, USER_ID
INSERT INTO comments (CONTENT, LIKE_COUNT, CREATED_AT, ID, MODEL_ID, UPDATED_AT, USER_ID) VALUES
    ('This is my first model.', 12, '2025-11-12 15:00:00.000000', 1, 1, '2025-11-12 15:00:00.000000', 0),  -- 호그라이더 -> Model 1 (Reze)
    ('This is amazing! The style is perfect for manga art.', 5, '2025-11-26 11:00:00.000000', 2, 1, '2025-11-26 11:00:00.000000', 101),  -- fake_user1 -> Model 1
    ('Love this! Pochita looks so cute!', 3, '2025-11-26 11:30:00.000000', 3, 2, '2025-11-26 11:30:00.000000', 102),  -- fake_user2 -> Model 2
    ('I agree! This is really well made.', 2, '2025-11-26 16:00:00.000000', 4, 2, '2025-11-26 16:00:00.000000', 101),  -- fake_user1 -> Model 2 (추가)
    ('Great work on this character model!', 4, '2025-11-26 12:00:00.000000', 5, 3, '2025-11-26 12:00:00.000000', 101),  -- fake_user1 -> Model 3
    ('Will definitely use this. Thanks!', 3, '2025-11-26 16:30:00.000000', 6, 3, '2025-11-26 16:30:00.000000', 102),  -- fake_user2 -> Model 3 (추가)
    ('The Naruto style is on point! Well done.', 7, '2025-11-26 12:30:00.000000', 7, 4, '2025-11-26 12:30:00.000000', 102),  -- fake_user2 -> Model 4
    ('Beautiful Tanjiro render! Really captures the essence.', 6, '2025-11-26 13:00:00.000000', 8, 5, '2025-11-26 13:00:00.000000', 101),  -- fake_user1 -> Model 5
    ('So adorable! Perfect for creating cute characters.', 8, '2025-11-26 13:30:00.000000', 9, 6, '2025-11-26 13:30:00.000000', 102),  -- fake_user2 -> Model 6
    ('The outline style is very clean and professional!', 5, '2025-11-26 14:00:00.000000', 10, 7, '2025-11-26 14:00:00.000000', 101),  -- fake_user1 -> Model 7
    ('Zelda vibes are amazing! Love this art style.', 9, '2025-11-26 14:30:00.000000', 11, 8, '2025-11-26 14:30:00.000000', 102),  -- fake_user2 -> Model 8
    ('Minimalist perfection! Exactly what I needed.', 4, '2025-11-26 15:00:00.000000', 12, 9, '2025-11-26 15:00:00.000000', 101);  -- fake_user1 -> Model 9

-- 14. 중요! 외래 키 체크 다시 켜기 (필수)
SET FOREIGN_KEY_CHECKS = 1;
