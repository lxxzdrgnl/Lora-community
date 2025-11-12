package rheon.wsd_lora_community.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rheon.wsd_lora_community.global.dto.ApiResponse;

import java.util.Map;

/**
 * 헬스 체크 컨트롤러
 */
@Tag(name = "헬스 체크", description = "서버 상태 확인 API")
@RestController
@RequestMapping("/")
public class HealthCheckController {

    @Operation(summary = "서버 상태 확인", description = "서버가 정상적으로 실행 중인지 확인합니다.")
    @GetMapping
    public ApiResponse<Map<String, String>> healthCheck() {
        return ApiResponse.success("LoRA 모델 공유 플랫폼 API가 정상적으로 실행 중입니다.", Map.of(
                "status", "UP",
                "service", "WSD_Lora_community",
                "version", "1.0.0"
        ));
    }
}
