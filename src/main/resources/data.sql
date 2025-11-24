-- ===========================
-- LoRA Community Platform
-- 초기 데이터 SQL (H2 Database) - 수정 완료 버전
-- ===========================

-- 1. 사용자 데이터 (Users)
INSERT INTO users (id, email, name, nickname, profile_image_url, oauth_provider, oauth_provider_id, role, created_at, updated_at) VALUES
    (0, 'dldydwo9@gmail.com', '호그라이더', '호그라이더', 'https://i.namu.wiki/i/aQOMWPkdAQdPbTFq54MsoZiwOsxCWYwioCRnDuuP6hoihR5DlP3quxzC9hSP3y7H2bFIu6blY-sX7KvP6NsABA.webp', 'GOOGLE', 'google_hograider_000', 'USER', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

INSERT INTO users (email, name, nickname, profile_image_url, oauth_provider, oauth_provider_id, role, created_at, updated_at) VALUES
                                                                                                                                  ('admin@example.com', 'Admin User', 'admin', 'https://picsum.photos/200/200?random=1', 'GOOGLE', 'google_admin_123', 'ADMIN', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                                                                                  ('john.doe@example.com', 'John Doe', 'johndoe', 'https://picsum.photos/200/200?random=2', 'GOOGLE', 'google_john_456', 'USER', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                                                                                  ('jane.smith@example.com', 'Jane Smith', 'janesmith', 'https://picsum.photos/200/200?random=3', 'GOOGLE', 'google_jane_789', 'USER', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                                                                                  ('artist.kim@example.com', 'Kim Artist', 'artist_kim', 'https://picsum.photos/200/200?random=4', 'GOOGLE', 'google_kim_101', 'USER', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                                                                                  ('creator.lee@example.com', 'Lee Creator', 'creatorlee', 'https://picsum.photos/200/200?random=5', 'GOOGLE', 'google_lee_202', 'USER', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 2. 태그 데이터 (Tags)
INSERT INTO tags (name, category, usage_count, created_at, updated_at) VALUES
                                                                           ('anime', 'STYLE', 15, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                           ('manga', 'STYLE', 12, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                           ('realistic', 'STYLE', 8, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                           ('cartoon', 'STYLE', 6, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                           ('watercolor', 'STYLE', 4, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                           ('pixel_art', 'STYLE', 3, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                           ('girl', 'CHARACTER', 20, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                           ('boy', 'CHARACTER', 10, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                           ('animal', 'CHARACTER', 7, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                           ('robot', 'CHARACTER', 5, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                           ('fantasy', 'GENRE', 14, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                           ('scifi', 'GENRE', 9, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                           ('romance', 'GENRE', 6, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                           ('action', 'GENRE', 11, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                           ('horror', 'GENRE', 4, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                           ('cute', 'OTHER', 18, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                           ('cool', 'OTHER', 13, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                           ('elegant', 'OTHER', 8, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 3. LoRA 모델 데이터 (Lora_Models)
-- 주의: ID는 자동생성되므로 순서대로 1, 2, 3... 7까지 생성됩니다.
INSERT INTO lora_models (user_id, title, description, character_name, style, training_images_count, epochs, learning_rate, lora_rank, s3_key, file_size, base_model, is_public, status, view_count, like_count, created_at, updated_at) VALUES
                                                                                                                                                                                                                                            (0, 'Reze', '체인소맨 레제 만화버전', 'Reze', 'manga', 20, 200, 0.0001, 16, '0/Reze.safetensors', 15728640, 'stablediffusionapi/anything-v5', TRUE, 'COMPLETED', 505412, 14545, DATEADD('DAY', -15, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
                                                                                                                                                                                                                                            (0, 'Pochita', '체인소맨 포치타', 'Pochita', 'anime', 30, 10, 0.0001, 16, '0/Pochita.safetensors', 52428800, 'stablediffusionapi/anything-v5', TRUE, 'COMPLETED', 320, 45, DATEADD('DAY', -10, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
                                                                                                                                                                                                                                            (3, 'Realistic Portrait Master', '사실적인 인물 초상화를 생성하는 고품질 LoRA 모델입니다.', 'Emma', 'realistic', 200, 40, 0.00008, 32, 'models/3/realistic_portrait.safetensors', 104857600, 'stablediffusionapi/realistic-vision-v5', TRUE, 'COMPLETED', 580, 92, DATEADD('DAY', -8, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
                                                                                                                                                                                                                                            (4, 'Fantasy Landscape Generator', '판타지 풍경과 배경을 생성하는 LoRA 모델입니다.', NULL, 'fantasy', 180, 35, 0.00012, 24, 'models/4/fantasy_landscape.safetensors', 78643200, 'stablediffusionapi/anything-v5', TRUE, 'COMPLETED', 210, 38, DATEADD('DAY', -7, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
                                                                                                                                                                                                                                            (5, 'Manga Character Style', '흑백 만화 스타일의 캐릭터를 생성하는 LoRA 모델입니다.', 'Naruto', 'manga', 120, 25, 0.00015, 16, 'models/5/manga_character.safetensors', 52428800, 'stablediffusionapi/anything-v5', TRUE, 'COMPLETED', 155, 29, DATEADD('DAY', -5, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
                                                                                                                                                                                                                                            (2, 'Cyberpunk Robot v2', '사이버펑크 스타일의 로봇과 메카를 생성하는 LoRA 모델입니다.', 'Cyber-01', 'scifi', 160, 32, 0.0001, 20, 'models/2/cyberpunk_robot.safetensors', 62914560, 'stablediffusionapi/anything-v5', TRUE, 'COMPLETED', 275, 51, DATEADD('DAY', -4, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
                                                                                                                                                                                                                                            (3, 'Watercolor Art Style', '수채화 스타일의 부드럽고 감성적인 이미지를 생성하는 LoRA 모델입니다.', NULL, 'watercolor', 140, 28, 0.00011, 16, 'models/3/watercolor_style.safetensors', 52428800, 'stablediffusionapi/anything-v5', TRUE, 'COMPLETED', 190, 33, DATEADD('DAY', -3, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP());

-- 4. 모델 태그 연결 (Model_Tags)
INSERT INTO model_tags (model_id, tag_id, created_at, updated_at) VALUES
                                                                      (1, 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                      (1, 7, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                      (2, 3, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                      (2, 7, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                      (3, 11, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                      (3, 5, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                      (4, 2, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                      (4, 8, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                      (4, 14, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                      (5, 12, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                      (5, 10, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                      (5, 17, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                      (6, 5, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                      (6, 18, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                      (7, 4, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                      (7, 9, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                      (7, 16, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 5. 모델 샘플 이미지 (Model_Samples)
INSERT INTO model_samples (model_id, image_url, prompt, negative_prompt, steps, guidance_scale, seed, display_order, is_primary, created_at, updated_at) VALUES
                                                                                                                                                             (1, 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/reze.png', 'sks, manga girl...', 'ugly...', 35, 6.5, null, 1, TRUE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                                                                                                             (1, 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/20251111_181540_3.png', 'sks, manga girl...', 'ugly...', 35, 7.5, null, 2, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                                                                                                             (2, 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/pochita_1.jpeg', 'pochita...', 'cartoon...', 40, 9.0, 111222, 1, TRUE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                                                                                                             (2, 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/pochita_2.jpeg', 'pochita...', 'low quality...', 35, 8.5, 333444, 2, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                                                                                                             (3, 'https://picsum.photos/768/512?random=302', 'fantasy castle...', 'dark...', 35, 8.5, 777888, 2, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                                                                                                             (4, 'https://picsum.photos/512/768?random=401', 'manga character...', 'color...', 25, 7.0, 999000, 1, TRUE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                                                                                                             (4, 'https://picsum.photos/512/768?random=402', 'manga character...', 'blurry...', 28, 7.5, 121212, 2, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                                                                                                             (5, 'https://picsum.photos/512/512?random=501', 'cyberpunk robot...', 'organic...', 30, 8.0, 131313, 1, TRUE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                                                                                                             (5, 'https://picsum.photos/512/512?random=502', 'cyberpunk mecha...', 'cute...', 32, 8.2, 141414, 2, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                                                                                                             (6, 'https://picsum.photos/768/512?random=601', 'watercolor...', 'sharp...', 22, 6.5, 151515, 1, TRUE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                                                                                                             (7, 'https://picsum.photos/512/512?random=701', 'cute rabbit...', 'realistic...', 20, 7.0, 161616, 1, TRUE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                                                                                                             (7, 'https://picsum.photos/512/512?random=702', 'cute cat...', 'dark...', 22, 7.2, 171717, 2, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 6. 모델 프롬프트 예시 (Model_Prompts)
-- [수정] model_id 8번은 없으므로 1번으로 변경했습니다.
INSERT INTO model_prompts (model_id, title, prompt, negative_prompt, description, display_order, created_at, updated_at) VALUES
                                                                                                                             (1, 'Basic Manga Girl', 'sks, manga girl, beautiful...', 'ugly...', '기본적인 만화 소녀', 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                                                                             (1, 'Magical Girl', 'sks, magical girl...', 'dark...', '마법소녀 컨셉', 2, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                                                                             (1, 'School Uniform', 'sks, manga girl, school uniform...', 'adult...', '학교 배경', 3, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                                                                             (2, 'Professional Portrait', 'professional portrait...', 'cartoon...', '전문 스튜디오', 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                                                                             (2, 'Natural Light', 'portrait, natural lighting...', 'artificial...', '자연광', 2, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                                                                             (3, 'Magical Forest', 'fantasy forest...', 'realistic...', '신비로운 숲', 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                                                                             (3, 'Floating Castle', 'fantasy castle...', 'ground...', '공중 성', 2, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                                                                             (4, 'Action Scene', 'manga character...', 'static...', '액션 장면', 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                                                                             (5, 'Cyberpunk Street', 'cyberpunk robot...', 'nature...', '사이버펑크 거리', 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                                                                             (7, 'Cute Animal', 'cute animal...', 'realistic...', '귀여운 동물', 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 7. 댓글 (Comments)
INSERT INTO comments (model_id, user_id, content, like_count, created_at, updated_at) VALUES
                                                                                          (1, 3, '정말 귀여운 스타일이네요!', 12, DATEADD('DAY', -5, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
                                                                                          (1, 5, '샘플 이미지들이 정말 일관성 있게 나왔네요.', 5, DATEADD('DAY', -3, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
                                                                                          (2, 2, '사실적인 표현이 놀랍습니다!', 15, DATEADD('DAY', -6, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
                                                                                          (2, 4, '프롬프트 가이드가 정말 도움이 되었습니다.', 9, DATEADD('DAY', -5, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
                                                                                          (2, 5, '저도 이 프롬프트 사용해서 좋은 결과 얻었어요.', 6, DATEADD('DAY', -4, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
                                                                                          (3, 5, '판타지 배경으로 정말 좋네요.', 7, DATEADD('DAY', -4, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
                                                                                          (4, 2, '망가 스타일 재현이 완벽하네요!', 11, DATEADD('DAY', -3, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
                                                                                          (4, 3, '흑백 톤 표현이 자연스러워요.', 4, DATEADD('DAY', -2, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
                                                                                          (5, 3, '사이버펑크 감성 최고예요!', 13, DATEADD('DAY', -2, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
                                                                                          (7, 2, '아이들 동화책 일러스트로 완벽할 것 같아요!', 20, DATEADD('DAY', -1, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
                                                                                          (7, 3, '동의합니다!', 14, DATEADD('DAY', -1, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
                                                                                          (7, 4, '색감이 정말 좋네요.', 8, DATEADD('HOUR', -12, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP());

-- 8. 댓글 좋아요 (Comment_Likes)
INSERT INTO comment_likes (comment_id, user_id, created_at, updated_at) VALUES
                                                                            (1, 2, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                            (1, 4, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                            (1, 5, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                            (2, 3, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                            (2, 5, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                            (4, 3, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                            (4, 5, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                            (11, 4, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                            (11, 5, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 9. 모델 좋아요 (Model_Likes)
INSERT INTO model_likes (model_id, user_id, created_at, updated_at) VALUES
                                                                        (1, 3, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                        (1, 4, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                        (1, 5, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                        (2, 2, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                        (2, 4, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                        (2, 5, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                        (7, 2, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                        (7, 3, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
                                                                        (7, 4, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 11. 이미지 생성 기록 (Generation_History)
-- [수정] model_id 0번은 없으므로 1번으로 변경했습니다.
INSERT INTO generation_history (user_id, model_id, prompt, negative_prompt, steps, guidance_scale, seed, num_images, status, created_at, updated_at) VALUES
                                                                                                                                                         (0, 1, 'sks, manga girl...', 'ugly...', 35, 7.5, 111111, 1, 'SUCCESS', DATEADD('DAY', -5, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
                                                                                                                                                         (0, 1, 'sks, manga girl...', 'ugly...', 30, 8.0, 222222, 1, 'SUCCESS', DATEADD('DAY', -4, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
                                                                                                                                                         (0, 1, 'sks, manga girl...', 'deformed...', 28, 7.8, 333333, 1, 'SUCCESS', DATEADD('DAY', -3, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP());

-- 12. 생성된 이미지 (Generated_Images)
INSERT INTO generated_images (generation_history_id, s3_url, s3_key, file_size, display_order, is_sample, created_at, updated_at) VALUES
                                                                                                                                      (1, 'https://lora-generated-image-bucket.s3.ap-northeast-2.amazonaws.com/0/reze.png', '0/reze.png', 2048576, 1, FALSE, DATEADD('DAY', -5, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
                                                                                                                                      (2, 'https://picsum.photos/512/512?random=102', '0/generated_102.png', 2097152, 1, FALSE, DATEADD('DAY', -4, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
                                                                                                                                      (3, 'https://picsum.photos/512/512?random=103', '0/generated_103.png', 2150000, 1, FALSE, DATEADD('DAY', -3, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP());

-- 12. 학습 작업 (Training_Jobs)
-- [수정] 끝에 콤마(,)를 세미콜론(;)으로 수정
INSERT INTO training_jobs (user_id, model_id, status, current_epoch, total_epochs, phase, error_message, started_at, completed_at, created_at, updated_at) VALUES
    (0, 1, 'SUCCESS', 30, 30, 'completed', NULL, DATEADD('DAY', -10, CURRENT_TIMESTAMP()), DATEADD('DAY', -9, CURRENT_TIMESTAMP()), DATEADD('DAY', -11, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP());