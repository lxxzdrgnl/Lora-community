-- ===========================
-- LoRA Community Platform
-- 초기 데이터 SQL (H2 Database)
-- ===========================

-- 1. 사용자 데이터 (Users)
-- created_at, updated_at은 BaseEntity에서 자동 생성되므로 현재 타임스탬프 사용
INSERT INTO users (id, email, name, nickname, profile_image_url, oauth_provider, oauth_provider_id, role, created_at, updated_at) VALUES
(0, 'dldydwo9@gmail.com', '호그라이더', '호그라이더', 'https://i.namu.wiki/i/aQOMWPkdAQdPbTFq54MsoZiwOsxCWYwioCRnDuuP6hoihR5DlP3quxzC9hSP3y7H2bFIu6blY-sX7KvP6NsABA.webp', 'GOOGLE', 'google_hograider_000', 'USER', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

INSERT INTO users (email, name, nickname, profile_image_url, oauth_provider, oauth_provider_id, role, created_at, updated_at) VALUES
('admin@example.com', 'Admin User', 'admin', 'https://picsum.photos/200/200?random=1', 'GOOGLE', 'google_admin_123', 'ADMIN', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('john.doe@example.com', 'John Doe', 'johndoe', 'https://picsum.photos/200/200?random=2', 'GOOGLE', 'google_john_456', 'USER', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('jane.smith@example.com', 'Jane Smith', 'janesmith', 'https://picsum.photos/200/200?random=3', 'GOOGLE', 'google_jane_789', 'USER', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('artist.kim@example.com', 'Kim Artist', 'artist_kim', 'https://picsum.photos/200/200?random=4', 'GOOGLE', 'google_kim_101', 'USER', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('creator.lee@example.com', 'Lee Creator', 'creator_lee', 'https://picsum.photos/200/200?random=5', 'GOOGLE', 'google_lee_202', 'USER', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 2. 태그 데이터 (Tags)
INSERT INTO tags (name, category, usage_count, created_at, updated_at) VALUES
-- STYLE 카테고리
('anime', 'STYLE', 15, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('manga', 'STYLE', 12, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('realistic', 'STYLE', 8, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('cartoon', 'STYLE', 6, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('watercolor', 'STYLE', 4, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('pixel_art', 'STYLE', 3, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
-- CHARACTER 카테고리
('girl', 'CHARACTER', 20, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('boy', 'CHARACTER', 10, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('animal', 'CHARACTER', 7, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('robot', 'CHARACTER', 5, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
-- GENRE 카테고리
('fantasy', 'GENRE', 14, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('scifi', 'GENRE', 9, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('romance', 'GENRE', 6, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('action', 'GENRE', 11, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('horror', 'GENRE', 4, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
-- OTHER 카테고리
('cute', 'OTHER', 18, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('cool', 'OTHER', 13, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('elegant', 'OTHER', 8, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 3. LoRA 모델 데이터 (Lora_Models)
INSERT INTO lora_models (user_id, title, description, character_name, style, training_images_count, epochs, learning_rate, lora_rank, s3_key, file_size, base_model, is_public, status, view_count, like_count, created_at, updated_at) VALUES
-- 호그라이더 사용자의 테스트 모델 (user_id=0)
(0, 'Reze', '체인소맨 레제 만화버전', 'Reze', 'manga', 20, 200, 0.0001, 16, '0/Reze.safetensors', 15728640, 'stablediffusionapi/anything-v5', TRUE, 'COMPLETED', 505412, 14545, DATEADD('DAY', -15, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
-- 완료된 공개 모델 (COMPLETED, PUBLIC)
(0, 'Pochita', '체인소맨 포치타', 'Pochita', 'anime', 30, 10, 0.0001, 16, '0/Pochita.safetensors', 52428800, 'stablediffusionapi/anything-v5', TRUE, 'COMPLETED', 320, 45, DATEADD('DAY', -10, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
(3, 'Realistic Portrait Master', '사실적인 인물 초상화를 생성하는 고품질 LoRA 모델입니다. 실제 사진과 구별하기 어려운 수준의 디테일을 제공합니다.', 'Emma', 'realistic', 200, 40, 0.00008, 32, 'models/3/realistic_portrait.safetensors', 104857600, 'stablediffusionapi/realistic-vision-v5', TRUE, 'COMPLETED', 580, 92, DATEADD('DAY', -8, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
(4, 'Fantasy Landscape Generator', '판타지 풍경과 배경을 생성하는 LoRA 모델입니다. 마법적이고 신비로운 장면을 만들어냅니다.', NULL, 'fantasy', 180, 35, 0.00012, 24, 'models/4/fantasy_landscape.safetensors', 78643200, 'stablediffusionapi/anything-v5', TRUE, 'COMPLETED', 210, 38, DATEADD('DAY', -7, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
(5, 'Manga Character Style', '흑백 만화 스타일의 캐릭터를 생성하는 LoRA 모델입니다. 전통적인 일본 망가 스타일을 재현합니다.', 'Naruto', 'manga', 120, 25, 0.00015, 16, 'models/5/manga_character.safetensors', 52428800, 'stablediffusionapi/anything-v5', TRUE, 'COMPLETED', 155, 29, DATEADD('DAY', -5, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
(2, 'Cyberpunk Robot v2', '사이버펑크 스타일의 로봇과 메카를 생성하는 LoRA 모델입니다. 네온 조명과 미래적 디자인이 특징입니다.', 'Cyber-01', 'scifi', 160, 32, 0.0001, 20, 'models/2/cyberpunk_robot.safetensors', 62914560, 'stablediffusionapi/anything-v5', TRUE, 'COMPLETED', 275, 51, DATEADD('DAY', -4, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
(3, 'Watercolor Art Style', '수채화 스타일의 부드럽고 감성적인 이미지를 생성하는 LoRA 모델입니다.', NULL, 'watercolor', 140, 28, 0.00011, 16, 'models/3/watercolor_style.safetensors', 52428800, 'stablediffusionapi/anything-v5', TRUE, 'COMPLETED', 190, 33, DATEADD('DAY', -3, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP());
-- 4. 모델 태그 연결 (Model_Tags)
INSERT INTO model_tags (model_id, tag_id, created_at, updated_at) VALUES
-- Model 1: Anime Girl Style v1
(1, 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),  -- anime
(1, 7, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),  -- girl
-- Model 2: Realistic Portrait Master
(2, 3, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),  -- realistic
(2, 7, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),  -- girl
-- Model 3: Fantasy Landscape Generator
(3, 11, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()), -- fantasy
(3, 5, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),  -- watercolor
-- Model 4: Manga Character Style
(4, 2, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),  -- manga
(4, 8, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),  -- boy
(4, 14, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()), -- action
-- Model 5: Cyberpunk Robot v2
(5, 12, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()), -- scifi
(5, 10, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()), -- robot
(5, 17, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()), -- cool
-- Model 6: Watercolor Art Style
(6, 5, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),  -- watercolor
(6, 18, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()), -- elegant
-- Model 7: Cute Animal Characters
(7, 4, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),  -- cartoon
(7, 9, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),  -- animal
(7, 16, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()); -- cute

-- 5. 모델 샘플 이미지 (Model_Samples)
INSERT INTO model_samples (model_id, image_url, prompt, negative_prompt, steps, guidance_scale, seed, display_order, is_primary, created_at, updated_at) VALUES
-- Model 1 샘플들 (Reze - user_id=0)
(1, 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/reze.png', 'sks, a manga girl with black hair and a white shirt, get a flower, mangastyle, monochrome,black and white, grayscale', 'ugly, blurry, low quality', 35, 6.5, null, 1, TRUE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(1, 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/20251111_181540_3.png', 'sks, a manga girl with black hair and a white shirt, get a flower, mangastyle, monochrome,black and white, grayscale', 'ugly, blurry, low quality', 35, 7.5, null, 2, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
-- Model 2 샘플들
(2, 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/pochita_1.jpeg', 'pochita, chainsaw, no humans', 'cartoon, anime, painting', 40, 9.0, 111222, 1, TRUE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/pochita_2.jpeg', 'pochita, chainsaw, no humans', 'low quality, blurry', 35, 8.5, 333444, 2, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
-- Model 3 샘플들
(3, 'https://picsum.photos/768/512?random=302', 'fantasy castle, floating islands, clouds, sunset', 'dark, gloomy', 35, 8.5, 777888, 2, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
-- Model 4 샘플들
(4, 'https://picsum.photos/512/768?random=401', 'manga character, spiky hair, action pose, speed lines', 'color, realistic', 25, 7.0, 999000, 1, TRUE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(4, 'https://picsum.photos/512/768?random=402', 'manga character, ninja, kunai, dramatic shadows', 'blurry, low quality', 28, 7.5, 121212, 2, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
-- Model 5 샘플들
(5, 'https://picsum.photos/512/512?random=501', 'cyberpunk robot, neon lights, city background, rain', 'organic, natural', 30, 8.0, 131313, 1, TRUE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(5, 'https://picsum.photos/512/512?random=502', 'cyberpunk mecha, weapons, futuristic, glowing', 'cute, friendly', 32, 8.2, 141414, 2, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
-- Model 6 샘플들
(6, 'https://picsum.photos/768/512?random=601', 'watercolor landscape, mountains, lake, soft colors', 'sharp, digital', 22, 6.5, 151515, 1, TRUE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
-- Model 7 샘플들
(7, 'https://picsum.photos/512/512?random=701', 'cute rabbit, big eyes, fluffy, flowers', 'realistic, scary', 20, 7.0, 161616, 1, TRUE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(7, 'https://picsum.photos/512/512?random=702', 'cute cat, colorful, happy, playing', 'dark, evil', 22, 7.2, 171717, 2, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 6. 모델 프롬프트 예시 (Model_Prompts)
INSERT INTO model_prompts (model_id, title, prompt, negative_prompt, description, display_order, created_at, updated_at) VALUES
-- Model 1 프롬프트들 (Reze - user_id=0)
(1, 'Basic Manga Girl', 'sks, manga girl, beautiful, detailed eyes, black hair', 'ugly, deformed, blurry, bad anatomy, low quality, color', '기본적인 만화 소녀 생성 프롬프트', 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(8, 'Magical Girl', 'sks, magical girl, transformation, sparkles, ribbons, wand, mangastyle, monochrome', 'dark, evil, scary, color', '마법소녀 컨셉 프롬프트', 2, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(8, 'School Uniform', 'sks, manga girl, school uniform, classroom, books, cheerful, mangastyle', 'adult, provocative, color', '학교 배경 프롬프트', 3, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
-- Model 2 프롬프트들
(2, 'Professional Portrait', 'professional portrait, studio lighting, sharp focus, high detail', 'cartoon, anime, painting, drawing', '전문 스튜디오 촬영 스타일', 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 'Natural Light', 'portrait, natural lighting, outdoor, golden hour, soft', 'artificial, indoor, flash', '자연광 초상화', 2, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
-- Model 3 프롬프트들
(3, 'Magical Forest', 'fantasy forest, glowing plants, mystical atmosphere, detailed', 'realistic, modern, urban', '신비로운 숲 배경', 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(3, 'Floating Castle', 'fantasy castle, floating, clouds, epic, majestic', 'ground level, simple', '공중 성 배경', 2, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
-- Model 4 프롬프트들
(4, 'Action Scene', 'manga character, action pose, speed lines, dynamic, intense', 'static, boring, peaceful', '액션 장면 프롬프트', 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
-- Model 5 프롬프트들
(5, 'Cyberpunk Street', 'cyberpunk robot, neon signs, rainy street, detailed', 'nature, organic, ancient', '사이버펑크 거리 배경', 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
-- Model 7 프롬프트들
(7, 'Cute Animal', 'cute animal, big eyes, fluffy, colorful, happy', 'realistic, scary, dark', '귀여운 동물 기본 프롬프트', 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 7. 댓글 (Comments)
INSERT INTO comments (model_id, user_id, content, like_count, created_at, updated_at) VALUES
-- Model 1에 대한 댓글 (Reze - user_id=0)
(1, 3, '정말 귀여운 스타일이네요! 학습 파라미터 설정이 궁금합니다.', 12, DATEADD('DAY', -5, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
(8, 4, '만화 스타일 재현이 완벽하네요! 200 epochs, learning rate 0.0001로 설정했어요. LoRA rank는 16입니다!', 8, DATEADD('DAY', -4, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
(1, 5, '샘플 이미지들이 정말 일관성 있게 나왔네요. 학습 데이터셋은 어떻게 준비하셨나요?', 5, DATEADD('DAY', -3, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
-- Model 2에 대한 댓글
(2, 2, '사실적인 표현이 놀랍습니다! 상업적으로도 활용 가능할 것 같아요.', 15, DATEADD('DAY', -6, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
(2, 4, '프롬프트 가이드가 정말 도움이 되었습니다. 감사합니다!', 9, DATEADD('DAY', -5, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
(2, 5, '저도 이 프롬프트 사용해서 좋은 결과 얻었어요. 공유해주셔서 감사합니다!', 6, DATEADD('DAY', -4, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
-- Model 3에 대한 댓글
(3, 5, '판타지 배경으로 정말 좋네요. 게임 콘셉트 아트에 사용하려고 합니다.', 7, DATEADD('DAY', -4, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
-- Model 4에 대한 댓글
(4, 2, '망가 스타일 재현이 완벽하네요!', 11, DATEADD('DAY', -3, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
(4, 3, '흑백 톤 표현이 자연스러워요. 학습 시간은 얼마나 걸렸나요?', 4, DATEADD('DAY', -2, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
-- Model 5에 대한 댓글
(5, 3, '사이버펑크 감성 최고예요! 네온 효과가 정말 멋집니다.', 13, DATEADD('DAY', -2, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
-- Model 7에 대한 댓글
(7, 2, '아이들 동화책 일러스트로 완벽할 것 같아요!', 20, DATEADD('DAY', -1, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
(7, 3, '동의합니다! 동물 캐릭터 디자인이 정말 사랑스러워요.', 14, DATEADD('DAY', -1, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
(7, 4, '색감이 정말 좋네요. 어떤 베이스 모델을 사용하셨나요?', 8, DATEADD('HOUR', -12, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP());

-- 8. 댓글 좋아요 (Comment_Likes)
INSERT INTO comment_likes (comment_id, user_id, created_at, updated_at) VALUES
-- Comment 1 좋아요들
(1, 2, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(1, 4, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(1, 5, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
-- Comment 2 좋아요들
(2, 3, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 5, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
-- Comment 4 좋아요들
(4, 3, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(4, 5, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
-- Comment 11 좋아요들
(11, 4, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(11, 5, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 9. 모델 좋아요 (Model_Likes)
INSERT INTO model_likes (model_id, user_id, created_at, updated_at) VALUES
-- Model 1 좋아요들
(1, 3, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(1, 4, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(1, 5, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
-- Model 2 좋아요들
(2, 2, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 4, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 5, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
-- Model 7 좋아요들 (가장 인기 많음)
(7, 2, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(7, 3, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(7, 4, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());


-- 11. 이미지 생성 기록 (Generation_History)
INSERT INTO generation_history (user_id, model_id, prompt, negative_prompt, steps, guidance_scale, seed, num_images, status, created_at, updated_at) VALUES
-- 호그라이더(user_id=0)의 생성 기록
(0, 0, 'sks, manga girl, black hair, white shirt, flower, mangastyle, monochrome', 'ugly, blurry, color', 35, 7.5, 111111, 1, 'SUCCESS', DATEADD('DAY', -5, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
(0, 1, 'sks, manga girl, pink hair, magical girl, sparkles, mangastyle', 'ugly, bad anatomy, color', 30, 8.0, 222222, 1, 'SUCCESS', DATEADD('DAY', -4, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
(0, 1, 'sks, manga girl, blonde hair, summer dress, beach, mangastyle', 'deformed, bad quality, color', 28, 7.8, 333333, 1, 'SUCCESS', DATEADD('DAY', -3, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP());
-- 12. 생성된 이미지 (Generated_Images)
INSERT INTO generated_images (generation_history_id, s3_url, s3_key, file_size, display_order, is_sample, created_at, updated_at) VALUES
-- History 1의 이미지 (호그라이더 - user_id=0, model_id=1)
(1, 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/reze.png', '0/reze.png', 2048576, 1, FALSE, DATEADD('DAY', -5, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
-- History 2의 이미지 (호그라이더 - user_id=0, model_id=1)
(2, 'https://picsum.photos/512/512?random=102', '0/generated_102.png', 2097152, 1, FALSE, DATEADD('DAY', -4, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
-- History 3의 이미지 (호그라이더 - user_id=0, model_id=1)
(3, 'https://picsum.photos/512/512?random=103', '0/generated_103.png', 2150000, 1, FALSE, DATEADD('DAY', -3, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP());
-- 12. 학습 작업 (Training_Jobs)
INSERT INTO training_jobs (user_id, model_id, status, current_epoch, total_epochs, phase, error_message, started_at, completed_at, created_at, updated_at) VALUES
-- 완료된 학습 작업들
(0, 1, 'SUCCESS', 30, 30, 'completed', NULL, DATEADD('DAY', -10, CURRENT_TIMESTAMP()), DATEADD('DAY', -9, CURRENT_TIMESTAMP()), DATEADD('DAY', -11, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),

-- ===========================
-- 데이터 검증 쿼리 (주석)
-- ===========================
-- SELECT COUNT(*) FROM users;              -- 5명
-- SELECT COUNT(*) FROM tags;               -- 18개
-- SELECT COUNT(*) FROM lora_models;        -- 10개 (COMPLETED: 7, TRAINING: 2, FAILED: 1)
-- SELECT COUNT(*) FROM model_tags;         -- 태그 연결
-- SELECT COUNT(*) FROM model_samples;      -- 샘플 이미지
-- SELECT COUNT(*) FROM model_prompts;      -- 프롬프트 예시
-- SELECT COUNT(*) FROM comments;           -- 13개
-- SELECT COUNT(*) FROM model_likes;        -- 9개
-- SELECT COUNT(*) FROM generation_history; -- 7개
-- SELECT COUNT(*) FROM training_jobs;      -- 6개
