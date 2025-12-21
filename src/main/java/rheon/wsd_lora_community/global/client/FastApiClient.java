package rheon.wsd_lora_community.global.client;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * FastAPI 서버와 통신하는 HTTP 클라이언트 (Redis Pub/Sub 방식)
 *
 * FastAPI 엔드포인트:
 * - POST /train - 학습 시작 (진행률은 Redis Pub/Sub + WebSocket으로 전송)
 * - POST /generate - 이미지 생성 시작 (진행률은 Redis Pub/Sub + WebSocket으로 전송)
 * - GET /generate/images - 생성된 이미지 S3 키 목록 조회
 * - GET / - 서버 상태 확인
 */
@Slf4j
@Component
public class FastApiClient {

    private final WebClient webClient;

    public FastApiClient(
            @Value("${fastapi.base-url:http://127.0.0.1:8000}") String baseUrl
    ) {
        // HTTP 클라이언트에 타임아웃 설정 (Modal 서버 연결 실패 시 즉시 감지)
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000) // 연결 타임아웃: 10초
                .responseTimeout(Duration.ofSeconds(10)) // 응답 타임아웃: 10초
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(10, TimeUnit.SECONDS))
                            .addHandlerLast(new WriteTimeoutHandler(10, TimeUnit.SECONDS))
                );

        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(16 * 1024 * 1024)) // 16MB 버퍼 크기
                .build();
        log.info("FastAPI 클라이언트 초기화 완료. Base URL: {} (타임아웃: 10초)", baseUrl);
    }

    // ========== 학습 관련 API ==========

    /**
     * LoRA 모델 학습 시작 (Modal API)
     *
     * @param userId 사용자 ID
     * @param modelId 모델 ID
     * @param jobId 학습 작업 ID
     * @param modelName 모델 이름
     * @param trainingImageUrls S3 presigned URL 리스트
     * @param triggerWord 트리거 워드 (프롬프트에 사용될 키워드)
     * @param epochs 학습 에포크 수
     * @param learningRate 학습률
     * @param loraRank LoRA Rank (4, 8, 16, 32, 64)
     * @param baseModel 베이스 모델 (예: stablediffusionapi/anything-v5)
     * @param skipPreprocessing 전처리 스킵 여부
     * @param callbackUrl 완료 시 호출할 콜백 URL
     * @return 응답 메시지
     */
    public Mono<String> startTraining(String userId, Long modelId, Long jobId, String modelName, List<String> trainingImageUrls,
                                      String triggerWord, Integer epochs, Double learningRate, Integer loraRank,
                                      String baseModel, Boolean skipPreprocessing, String callbackUrl) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("user_id", userId);
        requestBody.put("model_id", modelId);
        requestBody.put("job_id", jobId);
        requestBody.put("model_name", modelName);
        requestBody.put("training_image_urls", trainingImageUrls);
        if (triggerWord != null && !triggerWord.isEmpty()) {
            requestBody.put("trigger_word", triggerWord);
        }
        if (epochs != null) {
            requestBody.put("epochs", epochs);
        }
        if (learningRate != null) {
            requestBody.put("learning_rate", learningRate);
        }
        if (loraRank != null) {
            requestBody.put("lora_rank", loraRank);
        }
        if (baseModel != null && !baseModel.isEmpty()) {
            requestBody.put("base_model", baseModel);
        }
        if (skipPreprocessing != null) {
            requestBody.put("skip_preprocessing", skipPreprocessing);
        }
        if (callbackUrl != null) {
            requestBody.put("callback_url", callbackUrl);
        }

        return webClient.post()
                .uri("/train")
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("FastAPI 에러 응답 ({}): {}", response.statusCode(), errorBody);
                                    return Mono.error(new RuntimeException(
                                            String.format("FastAPI Error %s: %s",
                                                    response.statusCode(), errorBody)
                                    ));
                                })
                )
                .bodyToMono(Map.class)
                .map(response -> (String) response.get("message"))
                .doOnSuccess(msg -> log.info("학습 시작 성공 (Modal): {}", msg))
                .doOnError(error -> log.error("학습 시작 실패 (Modal): {}", error.getMessage()));
    }

    // ========== 이미지 생성 관련 API ==========

    /**
     * 이미지 생성 시작 (Modal API)
     *
     * @param userId 사용자 ID
     * @param modelId 모델 ID
     * @param prompt 이미지 생성 프롬프트
     * @param negativePrompt 네거티브 프롬프트
     * @param loraModelUrl LoRA 모델 S3 Presigned URL
     * @param numImages 생성할 이미지 개수
     * @param steps 생성 스텝 수
     * @param guidanceScale CFG Scale
     * @param loraScale LoRA 강도 (0.0~2.0)
     * @param seed 랜덤 시드 (null 가능)
     * @param baseModel 베이스 모델 ID (null 가능)
     * @param callbackUrl 완료 시 콜백 URL (null 가능)
     * @return 응답 메시지
     */
    public Mono<String> startImageGeneration(
            String userId,
            Long modelId,
            Long historyId,
            String prompt,
            String negativePrompt,
            String loraModelUrl,
            int numImages,
            int steps,
            double guidanceScale,
            Double loraScale,
            Long seed,
            String baseModel,
            String callbackUrl
    ) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("user_id", userId);
        requestBody.put("model_id", modelId);
        if (historyId != null) {
            requestBody.put("history_id", historyId);
        }
        requestBody.put("prompt", prompt);
        requestBody.put("negative_prompt", negativePrompt);
        requestBody.put("lora_model_url", loraModelUrl);  // S3 Presigned URL
        requestBody.put("num_images", numImages);
        requestBody.put("steps", steps);
        requestBody.put("guidance_scale", guidanceScale);
        requestBody.put("lora_scale", loraScale != null ? loraScale : 1.0);  // 기본값 1.0
        if (seed != null) {
            requestBody.put("seed", seed);
        }
        if (baseModel != null && !baseModel.isEmpty()) {
            requestBody.put("base_model", baseModel);
        }
        if (callbackUrl != null) {
            requestBody.put("callback_url", callbackUrl);
        }

        return webClient.post()
                .uri("/generate")
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("FastAPI 에러 응답 ({}): {}", response.statusCode(), errorBody);
                                    return Mono.error(new RuntimeException(
                                            String.format("FastAPI Error %s: %s",
                                                    response.statusCode(), errorBody)
                                    ));
                                })
                )
                .bodyToMono(Map.class)
                .map(response -> (String) response.get("message"))
                .doOnSuccess(msg -> log.info("이미지 생성 시작 성공 (Modal): {}", msg))
                .doOnError(error -> log.error("이미지 생성 시작 실패 (Modal): {}", error.getMessage()));
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
