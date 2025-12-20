package rheon.wsd_lora_community.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rheon.wsd_lora_community.global.dto.ApiResponse;
import rheon.wsd_lora_community.global.queue.RedisQueueService;
import rheon.wsd_lora_community.global.queue.RedisQueueService.JobType;
import rheon.wsd_lora_community.global.queue.RedisQueueService.QueueStatus;

import java.util.Map;

/**
 * 헬스 체크 컨트롤러
 */
@Tag(name = "헬스 체크", description = "서버 상태 확인 API")
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class HealthCheckController {

    private final RedisQueueService queueService;

    @Operation(summary = "서버 상태 확인", description = "서버가 정상적으로 실행 중인지 확인합니다.")
    @GetMapping
    public ApiResponse<Map<String, Object>> healthCheck() {
        QueueStatus trainingQueue = queueService.getQueueStatus(JobType.TRAINING);
        QueueStatus generationQueue = queueService.getQueueStatus(JobType.GENERATION);

        return ApiResponse.success("LoRA 모델 공유 플랫폼 API가 정상적으로 실행 중입니다.", Map.of(
                "status", "UP",
                "service", "WSD_Lora_community",
                "version", "1.0.0",
                "queue", Map.of(
                        "training", Map.of(
                                "pending", trainingQueue.pending(),
                                "processing", trainingQueue.processing()
                        ),
                        "generation", Map.of(
                                "pending", generationQueue.pending(),
                                "processing", generationQueue.processing()
                        )
                )
        ));
    }
}
