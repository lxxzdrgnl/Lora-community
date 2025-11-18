package rheon.wsd_lora_community.global.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FastAPI 서버와 통신하는 HTTP 클라이언트
 *
 * FastAPI 엔드포인트:
 * - POST /train - 학습 시작
 * - GET /train/status - 학습 상태 확인
 * - GET /train/stream - 학습 진행률 SSE 스트림
 * - POST /generate - 이미지 생성 시작
 * - GET /generate/status - 생성 상태 확인
 * - GET /generate/stream - 생성 진행률 SSE 스트림
 */
@Slf4j
@Component
public class FastApiClient {

    private final WebClient webClient;

    public FastApiClient(
            @Value("${fastapi.base-url:http://127.0.0.1:8000}") String baseUrl
    ) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
        log.info("FastAPI 클라이언트 초기화 완료. Base URL: {}", baseUrl);
    }

    // ========== 학습 관련 API ==========

    /**
     * LoRA 모델 학습 시작
     *
     * @param rawDatasetPath 원본 데이터셋 경로
     * @param outputDir 학습된 모델이 저장될 경로
     * @param skipPreprocessing 전처리 과정 스킵 여부
     * @return 응답 메시지
     */
    public Mono<String> startTraining(String rawDatasetPath, String outputDir, boolean skipPreprocessing) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("raw_dataset_path", rawDatasetPath);
        requestBody.put("output_dir", outputDir);
        requestBody.put("skip_preprocessing", skipPreprocessing);

        return webClient.post()
                .uri("/train")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> (String) response.get("message"))
                .doOnSuccess(msg -> log.info("학습 시작 성공: {}", msg))
                .doOnError(error -> log.error("학습 시작 실패: {}", error.getMessage()));
    }

    /**
     * 학습 상태 조회
     *
     * @return 학습 상태 정보
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> getTrainingStatus() {
        return webClient.get()
                .uri("/train/status")
                .retrieve()
                .bodyToMono(Map.class)
                .map(map -> (Map<String, Object>) map)
                .doOnSuccess(status -> log.debug("학습 상태 조회 성공: {}", status.get("status")))
                .doOnError(error -> log.error("학습 상태 조회 실패: {}", error.getMessage()));
    }

    /**
     * 학습 진행률 SSE 스트림 구독
     *
     * @return 학습 상태 업데이트 스트림
     */
    @SuppressWarnings("unchecked")
    public Flux<Map<String, Object>> streamTrainingStatus() {
        return webClient.get()
                .uri("/train/stream")
                .retrieve()
                .bodyToFlux(Map.class)
                .map(map -> (Map<String, Object>) map)
                .doOnNext(status -> log.debug("학습 진행 상태 업데이트: {}", status))
                .doOnError(error -> log.error("학습 스트림 오류: {}", error.getMessage()))
                .doOnComplete(() -> log.info("학습 스트림 종료"));
    }

    // ========== 이미지 생성 관련 API ==========

    /**
     * 이미지 생성 시작
     *
     * @param prompt 이미지 생성 프롬프트
     * @param negativePrompt 네거티브 프롬프트
     * @param loraModelUrl LoRA 모델 S3 Presigned URL
     * @param numImages 생성할 이미지 개수
     * @param steps 생성 스텝 수
     * @param guidanceScale CFG Scale
     * @param seed 랜덤 시드 (null 가능)
     * @return 응답 메시지
     */
    public Mono<String> startImageGeneration(
            String prompt,
            String negativePrompt,
            String loraModelUrl,
            int numImages,
            int steps,
            double guidanceScale,
            Long seed
    ) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("prompt", prompt);
        requestBody.put("negative_prompt", negativePrompt);
        requestBody.put("lora_model_url", loraModelUrl);  // S3 Presigned URL
        requestBody.put("num_images", numImages);
        requestBody.put("steps", steps);
        requestBody.put("guidance_scale", guidanceScale);
        if (seed != null) {
            requestBody.put("seed", seed);
        }

        return webClient.post()
                .uri("/generate")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> (String) response.get("message"))
                .doOnSuccess(msg -> log.info("이미지 생성 시작 성공: {}", msg))
                .doOnError(error -> log.error("이미지 생성 시작 실패: {}", error.getMessage()));
    }

    /**
     * 이미지 생성 상태 조회
     *
     * @return 생성 상태 정보 (image_urls 포함)
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> getGenerationStatus() {
        return webClient.get()
                .uri("/generate/status")
                .retrieve()
                .bodyToMono(Map.class)
                .map(map -> (Map<String, Object>) map)
                .doOnSuccess(status -> log.debug("생성 상태 조회 성공: {}", status.get("status")))
                .doOnError(error -> log.error("생성 상태 조회 실패: {}", error.getMessage()));
    }

    /**
     * 이미지 생성 진행률 SSE 스트림 구독
     *
     * @return 생성 상태 업데이트 스트림
     */
    @SuppressWarnings("unchecked")
    public Flux<Map<String, Object>> streamGenerationStatus() {
        return webClient.get()
                .uri("/generate/stream")
                .retrieve()
                .bodyToFlux(Map.class)
                .map(map -> (Map<String, Object>) map)
                .doOnNext(status -> log.debug("생성 진행 상태 업데이트: {}", status))
                .doOnError(error -> log.error("생성 스트림 오류: {}", error.getMessage()))
                .doOnComplete(() -> log.info("생성 스트림 종료"));
    }

    /**
     * 생성된 이미지 S3 키 목록 조회
     *
     * 이미지 생성이 완료된 후 호출하여 S3에 업로드된 이미지 키 목록을 가져옵니다.
     *
     * @return S3 키 목록
     */
    @SuppressWarnings("unchecked")
    public Mono<List<String>> getGeneratedImageS3Keys() {
        return webClient.get()
                .uri("/generate/images")
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    Object s3Keys = response.get("s3_keys");
                    if (s3Keys instanceof List<?>) {
                        List<?> list = (List<?>) s3Keys;
                        return list.stream()
                                .filter(obj -> obj instanceof String)
                                .map(obj -> (String) obj)
                                .toList();
                    }
                    return List.<String>of();
                })
                .doOnSuccess(keys -> log.info("생성된 이미지 S3 키 {} 개: {}", keys.size(), keys))
                .doOnError(error -> log.error("S3 키 조회 실패: {}", error.getMessage()));
    }

    /**
     * 생성된 이미지 URL 목록 조회 (레거시, 호환성 유지)
     *
     * @deprecated 대신 getGeneratedImageS3Keys()를 사용하세요
     * @return 이미지 URL 목록
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public Mono<List<String>> getGeneratedImageUrls() {
        return getGenerationStatus()
                .map(status -> {
                    Object imageUrls = status.get("image_urls");
                    if (imageUrls instanceof List<?>) {
                        List<?> list = (List<?>) imageUrls;
                        return list.stream()
                                .filter(obj -> obj instanceof String)
                                .map(obj -> (String) obj)
                                .toList();
                    }
                    return List.<String>of();
                })
                .doOnSuccess(urls -> log.info("생성된 이미지 {} 개: {}", urls.size(), urls))
                .doOnError(error -> log.error("이미지 URL 조회 실패: {}", error.getMessage()));
    }

    // ========== 서버 상태 확인 ==========

    /**
     * FastAPI 서버 상태 확인
     *
     * @return 서버 실행 여부
     */
    public Mono<Boolean> checkServerHealth() {
        return webClient.get()
                .uri("/")
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> response.containsKey("message"))
                .onErrorReturn(false)
                .doOnSuccess(isHealthy -> log.info("FastAPI 서버 상태: {}", isHealthy ? "정상" : "비정상"))
                .doOnError(error -> log.error("FastAPI 서버 연결 실패: {}", error.getMessage()));
    }
}
