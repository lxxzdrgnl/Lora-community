package rheon.wsd_lora_community.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rheon.wsd_lora_community.admin.dto.JobFailRequest;
import rheon.wsd_lora_community.admin.service.AdminJobService;
import rheon.wsd_lora_community.global.config.CommonApiResponses;
import rheon.wsd_lora_community.global.dto.ApiResponse;
import rheon.wsd_lora_community.global.dto.ErrorResponse;

/**
 * 관리자용 학습/생성 작업 관리 컨트롤러
 */
@RestController
@RequestMapping("/api/admin/jobs")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "관리자 전용 API")
@CommonApiResponses
@PreAuthorize("hasRole('ADMIN')")
public class AdminJobController {

    private final AdminJobService adminJobService;

    /**
     * 학습 작업 강제 실패 처리
     */
    @PostMapping("/training/{jobId}/fail")
    @Operation(summary = "학습 작업 강제 실패 처리", description = "진행 중인 학습 작업을 강제로 실패 상태로 변경합니다. (관리자 전용)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "실패 처리 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "학습 작업을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<Void>> failTrainingJob(
            @Parameter(description = "학습 작업 ID", required = true)
            @PathVariable Long jobId,
            @Valid @RequestBody JobFailRequest request
    ) {
        adminJobService.failTrainingJob(jobId, request.getErrorMessage());

        return ResponseEntity.ok(
                ApiResponse.success("학습 작업 실패 처리 성공", null)
        );
    }

    /**
     * 생성 작업 강제 실패 처리
     */
    @PostMapping("/generation/{historyId}/fail")
    @Operation(summary = "생성 작업 강제 실패 처리", description = "진행 중인 이미지 생성 작업을 강제로 실패 상태로 변경합니다. (관리자 전용)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "실패 처리 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "생성 기록을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<Void>> failGenerationHistory(
            @Parameter(description = "생성 기록 ID", required = true)
            @PathVariable Long historyId,
            @Valid @RequestBody JobFailRequest request
    ) {
        adminJobService.failGenerationHistory(historyId, request.getErrorMessage());

        return ResponseEntity.ok(
                ApiResponse.success("생성 작업 실패 처리 성공", null)
        );
    }
}
