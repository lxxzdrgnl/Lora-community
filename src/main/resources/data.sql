-- 1. 데이터 입력 중 외래키 에러 방지 (순서 무시)
SET FOREIGN_KEY_CHECKS = 0;

-- 2. Users 데이터 (U& 유니코드 -> 한글 복원 완료)
INSERT INTO users (CREATED_AT, DELETED_AT, ID, UPDATED_AT, NICKNAME, NAME, PROFILE_IMAGE_URL, EMAIL, OAUTH_PROVIDER_ID, OAUTH_PROVIDER, ROLE) VALUES
                                                                                                                                                  ('2025-11-25 04:33:49.538308', NULL, 0, '2025-11-25 04:39:12.588406', '호그라이더', '호그라이더', 'https://i.namu.wiki/i/aQOMWPkdAQdPbTFq54MsoZiwOsxCWYwioCRnDuuP6hoihR5DlP3quxzC9hSP3y7H2bFIu6blY-sX7KvP6NsABA.webp', 'dldydwo9@gmail.com', '115796410464082379941', 'GOOGLE', 'USER'),
                                                                                                                                                  ('2025-11-25 04:33:49.542115', NULL, 1, '2025-11-25 04:33:49.542115', 'admin', 'Admin User', 'https://picsum.photos/200/200?random=1', 'admin@example.com', 'google_admin_123', 'GOOGLE', 'ADMIN'),
                                                                                                                                                  ('2025-11-25 04:33:49.542115', NULL, 2, '2025-11-25 04:33:49.542115', 'johndoe', 'John Doe', 'https://picsum.photos/200/200?random=2', 'john.doe@example.com', 'google_john_456', 'GOOGLE', 'USER'),
                                                                                                                                                  ('2025-11-25 04:33:49.542115', NULL, 3, '2025-11-25 04:33:49.542115', 'janesmith', 'Jane Smith', 'https://picsum.photos/200/200?random=3', 'jane.smith@example.com', 'google_jane_789', 'GOOGLE', 'USER'),
                                                                                                                                                  ('2025-11-25 04:33:49.542115', NULL, 4, '2025-11-25 04:33:49.542115', 'artist_kim', 'Kim Artist', 'https://picsum.photos/200/200?random=4', 'artist.kim@example.com', 'google_kim_101', 'GOOGLE', 'USER'),
                                                                                                                                                  ('2025-11-25 04:33:49.542115', NULL, 5, '2025-11-25 04:33:49.542115', 'creatorlee', 'Lee Creator', 'https://picsum.photos/200/200?random=5', 'creator.lee@example.com', 'google_lee_202', 'GOOGLE', 'USER');

-- 3. Training Jobs
-- 컬럼 순서: CURRENT_EPOCH, TOTAL_EPOCHS, COMPLETED_AT, CREATED_AT, ID, MODEL_ID, STARTED_AT, UPDATED_AT, USER_ID, ERROR_MESSAGE, PHASE, STATUS
INSERT INTO training_jobs (CURRENT_EPOCH, TOTAL_EPOCHS, COMPLETED_AT, CREATED_AT, ID, MODEL_ID, STARTED_AT, UPDATED_AT, USER_ID, ERROR_MESSAGE, PHASE, STATUS) VALUES
    -- 완료된 학습 (모델 1 - Reze)
    (200, 200, '2025-11-12 14:30:15.123456', '2025-11-10 08:00:00.000000', 1, 1, '2025-11-10 08:05:30.000000', '2025-11-12 14:30:15.123456', 0, NULL, 'model_saved', 'SUCCESS'),

    -- 완료된 학습 (모델 2 - Pochita)
    (10, 10, '2025-11-15 16:45:22.987654', '2025-11-15 10:00:00.000000', 2, 2, '2025-11-15 10:10:00.000000', '2025-11-15 16:45:22.987654', 0, NULL, 'model_saved', 'SUCCESS');
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
INSERT INTO lora_models (EPOCHS, IS_PUBLIC, LEARNING_RATE, LIKE_COUNT, LORA_RANK, TRAINING_IMAGES_COUNT, VIEW_COUNT, CREATED_AT, DELETED_AT, FILE_SIZE, ID, UPDATED_AT, USER_ID, BASE_MODEL, TITLE, S3_KEY, DESCRIPTION, STATUS) VALUES
                                                                                                                                                                                                                           (200, TRUE, 0.00010000, 14545, 16, 20, 505451, '2025-11-10 04:33:49.55569', NULL, 15728640, 1, '2025-11-25 14:57:27.828744', 0, 'stablediffusionapi/anything-v5', 'Reze', '0/Reze.safetensors', '체인소맨 레제 만화버전', 'COMPLETED'),
                                                                                                                                                                                                                           (100, TRUE, 0.00010000, 45, 16, 30, 330, '2025-11-15 04:33:49.55569', NULL, 52428800, 2, '2025-11-25 09:36:10.137369', 0, 'stablediffusionapi/anything-v5', 'Pochita', '1/Pochita.safetensors', '체인소맨 포치타', 'COMPLETED'),
                                                                                                                                                                                                                           (150, TRUE, 0.00008000, 92, 32, 20, 594, '2025-11-17 04:33:49.55569', NULL, 104857600, 3, '2025-11-25 14:58:41.714603', 0, 'stablediffusionapi/anything-v5', 'Mouri Kogorou', '2/Mouri_Kogorou.safetensors', '명탐정 코난 모리 코고로 weight 0.6 ~ 0.7 추천', 'COMPLETED'),
                                                                                                                                                                                                                           (121, TRUE, 0.00012000, 38, 24, 180, 216, '2025-11-18 04:33:49.55569', NULL, 78643200, 4, '2025-11-25 14:58:57.144276', 0, 'Lykon/AnyLoRA', 'Naruto Style', '3/Naruto_Style.safetensors', '나루토 스타일의 캐릭터를 생성합니다.', 'COMPLETED'),
                                                                                                                                                                                                                           (130, TRUE, 0.00015000, 29, 16, 40, 162, '2025-11-20 04:33:49.55569', NULL, 52428800, 5, '2025-11-25 14:59:00.659209', 0, 'stablediffusionapi/anything-v5', 'Kamado Tanjiro', '4/Tanjiro.safetensors', '귀멸의 칼날 탄지로', 'COMPLETED'),
                                                                                                                                                                                                                           (140, TRUE, 0.00010000, 51, 16, 30, 331, '2025-11-21 04:33:49.55569', NULL, 62914560, 6, '2025-11-25 14:58:48.42624', 0, 'stablediffusionapi/anything-v5', 'Animal Crossing Style', '5/animal_crossing.safetensors', '동물의 숲 스타일 이미지를 생성합니다.', 'COMPLETED'),
                                                                                                                                                                                                                           (150, TRUE, 0.00015000, 29, 16, 40, 162, '2025-11-23 04:33:49.55569', NULL, 52428800, 7, '2025-11-25 14:59:00.659209', 0, 'stablediffusionapi/anything-v5', 'Manga Outline Style', '6/animeoutline.safetensors', '아웃라인 위주의 흑백 만화 스타일로 애니 캐릭터를 생성합니다.', 'COMPLETED'),
                                                                                                                                                                                                                           (150, TRUE, 0.00015000, 29, 16, 40, 162, '2025-11-24 04:33:49.55569', NULL, 52428800, 8, '2025-11-25 14:59:00.659209', 0, 'Lykon/AnyLoRA', 'Zelda Style', '7/zelda_style.safetensors', '젤다 야생의 숨결 스타일의 이미지를 생성합니다.', 'COMPLETED'),
                                                                                                                                                                                                                           (150, TRUE, 0.00015000, 29, 16, 40, 162, '2025-11-25 04:33:49.55569', NULL, 52428800, 9, '2025-11-25 14:59:00.659209', 0, 'stablediffusionapi/anything-v5', 'Miniallist', '8/anime_minimalist.safetensors', '간견한 표현이 특징인 Lora', 'COMPLETED');

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

-- 8. Generation History
INSERT INTO generation_history (GUIDANCE_SCALE, LORA_SCALE, NUM_IMAGES, STEPS, CREATED_AT, ID, MODEL_ID, SEED, UPDATED_AT, USER_ID, NEGATIVE_PROMPT, PROMPT, STATUS) VALUES
    -- Model 1 (Reze) samples
    (6.50, 1.0, 2, 35, '2025-11-25 02:00:00', 1, 1, 12345, '2025-11-25 02:05:00', 0, 'ugly, lowres, bad anatomy', 'sks, manga girl, black hair, white shirt', 'SUCCESS'),
    -- Model 2 (Pochita) samples
    (9.00, 1.0, 2, 40, '2025-11-25 03:00:00', 2, 2, 111222, '2025-11-25 03:05:00', 0, 'cartoon, low quality', 'pochita, chainsaw, no humans', 'SUCCESS'),
    -- Model 3 (Mouri Kogorou) samples
    (8.50, 1.0, 2, 35, '2025-11-25 04:00:00', 3, 3, 777888, '2025-11-25 04:05:00', 0, 'dark, blurry', '1boy, solo, black eyes, mustache, black hair', 'SUCCESS'),
    -- Model 4 (Naruto Style) samples
    (8.00, 1.0, 2, 30, '2025-11-25 05:00:00', 4, 4, 131313, '2025-11-25 05:05:00', 0, 'blurry, organic', 'manga character, ninja, orange jacket', 'SUCCESS'),
    -- Model 5 (Tanjiro) samples
    (6.50, 1.0, 2, 22, '2025-11-25 06:00:00', 5, 5, 151515, '2025-11-25 06:05:00', 0, 'cute, sharp', 'watercolor, boy, checkered haori', 'SUCCESS'),
    -- Model 6 (Animal Crossing) samples
    (7.00, 1.0, 3, 20, '2025-11-25 07:00:00', 6, 6, 161616, '2025-11-25 07:05:00', 0, 'realistic, dark', 'cute animal, cartoon style', 'SUCCESS'),
    -- Model 7 (Manga Outline) samples
    (7.00, 1.0, 3, 20, '2025-11-25 08:00:00', 7, 7, 171717, '2025-11-25 08:05:00', 0, 'EasyNegative, badhandv4', 'masterpiece, best quality, 1girl, solo, lineart, monochrome', 'SUCCESS'),
    -- Model 8 (Zelda Style) samples
    (9.00, 1.0, 3, 28, '2025-11-25 09:00:00', 8, 8, 181818, '2025-11-25 09:05:00', 0, 'painting by bad-artist, watermark, text, blurry', 'botw style, zelda, breath of the wild', 'SUCCESS'),
    -- Model 9 (Minimalist) samples
    (7.20, 1.0, 2, 22, '2025-11-25 10:00:00', 9, 9, 191919, '2025-11-25 10:05:00', 0, 'worst quality, low quality, realistic', 'anime minimalist, 1girl, solo', 'SUCCESS');

-- 9. Generated Images
INSERT INTO generated_images (DISPLAY_ORDER, CREATED_AT, FILE_SIZE, GENERATION_HISTORY_ID, ID, UPDATED_AT, S3_KEY, S3_URL) VALUES
    -- Model 1 images
    (1, '2025-11-25 02:05:00', 1024000, 1, 1, '2025-11-25 02:05:00', '0/reze.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/reze.png'),
    (2, '2025-11-25 02:05:00', 1024000, 1, 2, '2025-11-25 02:05:00', '0/20251111_181540_3.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/20251111_181540_3.png'),
    -- Model 2 images
    (1, '2025-11-25 03:05:00', 1024000, 2, 3, '2025-11-25 03:05:00', '0/pochita_1.jpeg', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/pochita_1.jpeg'),
    (2, '2025-11-25 03:05:00', 1024000, 2, 4, '2025-11-25 03:05:00', '0/pochita_2.jpeg', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/pochita_2.jpeg'),
    -- Model 3 images
    (1, '2025-11-25 04:05:00', 1024000, 3, 5, '2025-11-25 04:05:00', '0/2.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/2.png'),
    (2, '2025-11-25 04:05:00', 1024000, 3, 6, '2025-11-25 04:05:00', '0/3.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/3.png'),
    -- Model 4 images
    (1, '2025-11-25 05:05:00', 1024000, 4, 7, '2025-11-25 05:05:00', '0/naru_11.webp', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/naru_11.webp'),
    (2, '2025-11-25 05:05:00', 1024000, 4, 8, '2025-11-25 05:05:00', '0/naru_22.webp', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/naru_22.webp'),
    -- Model 5 images
    (1, '2025-11-25 06:05:00', 1024000, 5, 9, '2025-11-25 06:05:00', '0/tanji_1.webp', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/tanji_1.webp'),
    (2, '2025-11-25 06:05:00', 1024000, 5, 10, '2025-11-25 06:05:00', '0/tanji_22.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/tanji_22.png'),
    -- Model 6 images
    (1, '2025-11-25 07:05:00', 1024000, 6, 11, '2025-11-25 07:05:00', '0/animal_1.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/animal_1.png'),
    (2, '2025-11-25 07:05:00', 1024000, 6, 12, '2025-11-25 07:05:00', '0/animal_2.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/animal_2.png'),
    (3, '2025-11-25 07:05:00', 1024000, 6, 13, '2025-11-25 07:05:00', '0/animal_3.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/animal_3.png'),
    -- Model 7 images
    (1, '2025-11-25 08:05:00', 1024000, 7, 14, '2025-11-25 08:05:00', '0/manga_2.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/manga_2.png'),
    (2, '2025-11-25 08:05:00', 1024000, 7, 15, '2025-11-25 08:05:00', '0/manga_1.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/manga_1.png'),
    (3, '2025-11-25 08:05:00', 1024000, 7, 16, '2025-11-25 08:05:00', '0/manga_3.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/manga_3.png'),
    -- Model 8 images
    (1, '2025-11-25 09:05:00', 1024000, 8, 17, '2025-11-25 09:05:00', '0/zelda_1.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/zelda_1.png'),
    (2, '2025-11-25 09:05:00', 1024000, 8, 18, '2025-11-25 09:05:00', '0/zelda_2.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/zelda_2.png'),
    (3, '2025-11-25 09:05:00', 1024000, 8, 19, '2025-11-25 09:05:00', '0/zelda_3.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/zelda_3.png'),
    -- Model 9 images
    (1, '2025-11-25 10:05:00', 1024000, 9, 20, '2025-11-25 10:05:00', '0/minial_1.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/minial_1.png'),
    (2, '2025-11-25 10:05:00', 1024000, 9, 21, '2025-11-25 10:05:00', '0/minial_2.png', 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/minial_2.png');

-- 10. Model Samples (참조 방식으로 변경)
INSERT INTO model_samples (DISPLAY_ORDER, IS_PRIMARY, CREATED_AT, GENERATED_IMAGE_ID, ID, MODEL_ID, UPDATED_AT) VALUES
    -- Model 1 samples
    (1, TRUE, '2025-11-25 04:33:49.56957', 1, 1, 1, '2025-11-25 04:33:49.56957'),
    (2, FALSE, '2025-11-25 04:33:49.56957', 2, 2, 1, '2025-11-25 04:33:49.56957'),
    -- Model 2 samples
    (1, TRUE, '2025-11-25 04:33:49.56957', 3, 3, 2, '2025-11-25 04:33:49.56957'),
    (2, FALSE, '2025-11-25 04:33:49.56957', 4, 4, 2, '2025-11-25 04:33:49.56957'),
    -- Model 3 samples
    (1, TRUE, '2025-11-25 04:33:49.56957', 5, 5, 3, '2025-11-25 04:33:49.56957'),
    (2, FALSE, '2025-11-25 04:33:49.56957', 6, 6, 3, '2025-11-25 04:33:49.56957'),
    -- Model 4 samples
    (1, TRUE, '2025-11-25 04:33:49.56957', 7, 7, 4, '2025-11-25 04:33:49.56957'),
    (2, FALSE, '2025-11-25 04:33:49.56957', 8, 8, 4, '2025-11-25 04:33:49.56957'),
    -- Model 5 samples
    (1, TRUE, '2025-11-25 04:33:49.56957', 9, 9, 5, '2025-11-25 04:33:49.56957'),
    (2, FALSE, '2025-11-25 04:33:49.56957', 10, 10, 5, '2025-11-25 04:33:49.56957'),
    -- Model 6 samples
    (1, TRUE, '2025-11-25 04:33:49.56957', 11, 11, 6, '2025-11-25 04:33:49.56957'),
    (2, FALSE, '2025-11-25 04:33:49.56957', 12, 12, 6, '2025-11-25 04:33:49.56957'),
    (3, FALSE, '2025-11-25 04:33:49.56957', 13, 13, 6, '2025-11-25 04:33:49.56957'),
    -- Model 7 samples
    (1, TRUE, '2025-11-25 04:33:49.56957', 14, 14, 7, '2025-11-25 04:33:49.56957'),
    (2, FALSE, '2025-11-26 04:33:49.56957', 15, 15, 7, '2025-11-25 04:33:49.56957'),
    (3, FALSE, '2025-11-25 04:33:49.56957', 16, 16, 7, '2025-11-25 04:33:49.56957'),
    -- Model 8 samples
    (1, TRUE, '2025-11-25 04:33:49.56957', 17, 17, 8, '2025-11-25 04:33:49.56957'),
    (2, FALSE, '2025-11-25 04:33:49.56957', 18, 18, 8, '2025-11-25 04:33:49.56957'),
    (3, FALSE, '2025-11-25 04:33:49.56957', 19, 19, 8, '2025-11-25 04:33:49.56957'),
    -- Model 9 samples
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

-- 11. Comments (유니코드 한글 복원)
INSERT INTO comments (LIKE_COUNT, CREATED_AT, DELETED_AT, ID, MODEL_ID, UPDATED_AT, USER_ID, CONTENT) VALUES
                                                                                                          (12, '2025-11-20 04:33:49.583735', NULL, 1, 1, '2025-11-25 04:33:49.583735', 3, '정말 귀여운 스타일이네요!'),
                                                                                                          (5, '2025-11-22 04:33:49.583735', NULL, 2, 1, '2025-11-25 04:33:49.583735', 5, '샘플 이미지들이 정말 일관성 있게 나왔네요.'),
                                                                                                          (15, '2025-11-19 04:33:49.583735', NULL, 3, 2, '2025-11-25 04:33:49.583735', 2, '사실적인 표현이 놀랍습니다!'),
                                                                                                          (9, '2025-11-20 04:33:49.583735', NULL, 4, 2, '2025-11-25 04:33:49.583735', 4, '프롬프트 가이드가 정말 도움이 되었습니다.'),
                                                                                                          (6, '2025-11-21 04:33:49.583735', NULL, 5, 2, '2025-11-25 04:33:49.583735', 5, '저도 이 프롬프트 사용해서 좋은 결과 얻었어요.'),
                                                                                                          (7, '2025-11-21 04:33:49.583735', NULL, 6, 3, '2025-11-25 04:33:49.583735', 5, '판타지 배경으로 정말 좋네요.'),
                                                                                                          (11, '2025-11-22 04:33:49.583735', NULL, 7, 4, '2025-11-25 04:33:49.583735', 2, '망가 스타일 재현이 완벽하네요!'),
                                                                                                          (4, '2025-11-23 04:33:49.583735', NULL, 8, 4, '2025-11-25 04:33:49.583735', 3, '흑백 톤 표현이 자연스러워요.'),
                                                                                                          (13, '2025-11-23 04:33:49.583735', NULL, 9, 5, '2025-11-25 04:33:49.583735', 3, '사이버펑크 감성 최고예요!'),
                                                                                                          (20, '2025-11-24 04:33:49.583735', NULL, 10, 7, '2025-11-25 04:33:49.583735', 2, '아이들 동화책 일러스트로 완벽할 것 같아요!'),
                                                                                                          (14, '2025-11-24 04:33:49.583735', NULL, 11, 7, '2025-11-25 04:33:49.583735', 3, '동의합니다!'),
                                                                                                          (8, '2025-11-24 16:33:49.583735', NULL, 12, 7, '2025-11-25 04:33:49.583735', 4, '색감이 정말 좋네요.');

-- 12. 중요! 외래 키 체크 다시 켜기 (필수)
SET FOREIGN_KEY_CHECKS = 1;
