package rheon.wsd_lora_community.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rheon.wsd_lora_community.admin.dto.ModelStatisticsResponse;
import rheon.wsd_lora_community.admin.dto.ModelUsageStatistics;
import rheon.wsd_lora_community.admin.service.AdminStatisticsService;
import rheon.wsd_lora_community.global.config.CommonApiResponses;
import rheon.wsd_lora_community.global.dto.ApiResponse;
import rheon.wsd_lora_community.global.dto.ErrorResponse;

/**
 * 관리자용 통계 컨트롤러
 */
@RestController
@RequestMapping("/api/admin/statistics")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "관리자 전용 API")
@CommonApiResponses
@PreAuthorize("hasRole('ADMIN')")
public class AdminStatisticsController {

    private final AdminStatisticsService adminStatisticsService;

    /**
     * 최근 일주일간 가장 많이 사용된 모델 (페이징)
     */
    @GetMapping("/models/most-used")
    @Operation(summary = "가장 많이 사용된 모델", description = "최근 일주일간 이미지 생성에 가장 많이 사용된 모델을 조회합니다. (관리자 전용)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요 (JWT 토큰 없음 또는 만료)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN 권한 필요)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<Page<ModelUsageStatistics>>> getMostUsedModels(
            @ParameterObject
            @PageableDefault(size = 10, sort = "usageCount", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ModelUsageStatistics> statistics = adminStatisticsService.getMostUsedModelsInLastWeek(pageable);

        return ResponseEntity.ok(
                ApiResponse.success("가장 많이 사용된 모델 조회 성공", statistics)
        );
    }

    /**
     * 가장 많이 조회된 모델 (페이징)
     */
    @GetMapping("/models/most-viewed")
    @Operation(summary = "가장 많이 조회된 모델", description = "조회수가 많은 모델을 조회합니다. (관리자 전용)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요 (JWT 토큰 없음 또는 만료)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN 권한 필요)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<Page<ModelStatisticsResponse>>> getMostViewedModels(
            @ParameterObject
            @PageableDefault(size = 10, sort = "viewCount", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ModelStatisticsResponse> statistics = adminStatisticsService.getMostViewedModels(pageable);

        return ResponseEntity.ok(
                ApiResponse.success("가장 많이 조회된 모델 조회 성공", statistics)
        );
    }
}
