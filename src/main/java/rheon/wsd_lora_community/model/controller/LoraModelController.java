package rheon.wsd_lora_community.model.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import rheon.wsd_lora_community.global.dto.ApiResponse;
import rheon.wsd_lora_community.model.dto.*;
import rheon.wsd_lora_community.model.service.LoraModelService;
import rheon.wsd_lora_community.model.service.SampleService;
import rheon.wsd_lora_community.model.service.PromptService;

import java.util.List;

/**
 * LoRA 모델 컨트롤러
 * - 모델 CRUD
 * - 모델 검색/필터링
 * - 샘플 이미지 관리
 * - 프롬프트 관리
 */
@RestController
@RequestMapping("/api/models")
@RequiredArgsConstructor
@Tag(name = "LoRA Model", description = "LoRA 모델 API")
public class LoraModelController {

    private final LoraModelService loraModelService;
    private final SampleService sampleService;
    private final PromptService promptService;

    /**
     * Authentication 객체에서 사용자 ID 추출
     * OAuth2User 또는 UserDetails 모두 지원
     */
    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalArgumentException("인증 정보가 없습니다.");
        }

        Object principal = authentication.getPrincipal();

        // OAuth2 로그인 (Google)
        if (principal instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) principal;
            return Long.valueOf(oauth2User.getAttribute("id").toString());
        }

        // JWT 인증
        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            return Long.valueOf(userDetails.getUsername()); // username에 userId 저장됨
        }

        throw new IllegalArgumentException("지원하지 않는 인증 방식입니다.");
    }

    /**
     * 모델 생성
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "모델 생성", description = "새로운 LoRA 모델을 생성합니다.")
    public ResponseEntity<ApiResponse<LoraModelResponse>> createModel(
            @Parameter(hidden = true)
            Authentication authentication,
            @Valid @RequestBody ModelCreateRequest request
    ) {
        Long userId = getUserIdFromAuthentication(authentication);
        LoraModelResponse model = loraModelService.createModel(userId, request);

        return ResponseEntity.ok(
                ApiResponse.success("모델 생성 성공", model)
        );
    }

    /**
     * 공개 모델 목록 조회 (최신순)
     */
    @GetMapping
    @Operation(summary = "공개 모델 목록 조회", description = "공개된 모델 목록을 최신순으로 조회합니다.")
    public ResponseEntity<ApiResponse<Page<LoraModelResponse>>> getPublicModels(
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<LoraModelResponse> models = loraModelService.getPublicModels(pageable);

        return ResponseEntity.ok(
                ApiResponse.success("모델 목록 조회 성공", models)
        );
    }

    /**
     * 인기 모델 목록 조회 (좋아요순)
     */
    @GetMapping("/popular")
    @Operation(summary = "인기 모델 목록 조회", description = "좋아요가 많은 순서로 모델 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<Page<LoraModelResponse>>> getPopularModels(
            @ParameterObject
            @PageableDefault(size = 20, sort = "likeCount", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<LoraModelResponse> models = loraModelService.getPopularModels(pageable);

        return ResponseEntity.ok(
                ApiResponse.success("인기 모델 목록 조회 성공", models)
        );
    }

    /**
     * 모델 상세 조회
     */
    @GetMapping("/{modelId}")
    @Operation(summary = "모델 상세 조회", description = "모델의 상세 정보를 조회합니다. (샘플, 프롬프트, 태그 포함)")
    public ResponseEntity<ApiResponse<LoraModelDetailResponse>> getModelDetail(
            @Parameter(description = "모델 ID", required = true)
            @PathVariable Long modelId,
            @Parameter(hidden = true)
            Authentication authentication
    ) {
        Long currentUserId = null;
        if (authentication != null && authentication.isAuthenticated()) {
            try {
                currentUserId = getUserIdFromAuthentication(authentication);
            } catch (Exception e) {
                // 비로그인 사용자는 null로 처리
                currentUserId = null;
            }
        }

        LoraModelDetailResponse model = loraModelService.getModelDetail(modelId, currentUserId);

        return ResponseEntity.ok(
                ApiResponse.success("모델 상세 조회 성공", model)
        );
    }

    /**
     * 모델 수정
     */
    @PutMapping("/{modelId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "모델 수정", description = "모델 정보를 수정합니다. (작성자만 가능)")
    public ResponseEntity<ApiResponse<LoraModelResponse>> updateModel(
            @Parameter(description = "모델 ID", required = true)
            @PathVariable Long modelId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal,
            @Valid @RequestBody ModelUpdateRequest request
    ) {
        Long userId = Long.valueOf(principal.getAttribute("id").toString());
        LoraModelResponse updatedModel = loraModelService.updateModel(modelId, userId, request);

        return ResponseEntity.ok(
                ApiResponse.success("모델 수정 성공", updatedModel)
        );
    }

    /**
     * 모델 삭제
     */
    @DeleteMapping("/{modelId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "모델 삭제", description = "모델을 삭제합니다. (작성자만 가능)")
    public ResponseEntity<ApiResponse<Void>> deleteModel(
            @Parameter(description = "모델 ID", required = true)
            @PathVariable Long modelId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal
    ) {
        Long userId = Long.valueOf(principal.getAttribute("id").toString());
        loraModelService.deleteModel(modelId, userId);

        return ResponseEntity.ok(
                ApiResponse.success("모델 삭제 성공")
        );
    }

    /**
     * 내가 작성한 모델 목록 조회
     */
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "내 모델 목록 조회", description = "현재 유저가 작성한 모델 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<Page<LoraModelResponse>>> getMyModels(
            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal,
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long userId = Long.valueOf(principal.getAttribute("id").toString());
        Page<LoraModelResponse> models = loraModelService.getModelsByUser(userId, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("내 모델 목록 조회 성공", models)
        );
    }

    /**
     * 특정 유저의 모델 목록 조회
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "유저별 모델 목록 조회", description = "특정 유저가 작성한 모델 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<Page<LoraModelResponse>>> getUserModels(
            @Parameter(description = "유저 ID", required = true)
            @PathVariable Long userId,
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<LoraModelResponse> models = loraModelService.getModelsByUser(userId, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("유저 모델 목록 조회 성공", models)
        );
    }

    /**
     * 키워드로 모델 검색
     */
    @GetMapping("/search")
    @Operation(summary = "모델 검색", description = "제목 또는 설명에서 키워드를 검색합니다.")
    public ResponseEntity<ApiResponse<Page<LoraModelResponse>>> searchModels(
            @Parameter(description = "검색 키워드", required = true)
            @RequestParam String query,
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<LoraModelResponse> models = loraModelService.searchModels(query, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("모델 검색 성공", models)
        );
    }

    /**
     * 태그로 모델 필터링
     */
    @GetMapping("/filter")
    @Operation(summary = "태그로 모델 필터링", description = "지정한 태그를 가진 모델을 조회합니다.")
    public ResponseEntity<ApiResponse<Page<LoraModelResponse>>> filterModelsByTags(
            @Parameter(description = "태그 이름 목록 (쉼표로 구분)", required = true)
            @RequestParam List<String> tags,
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<LoraModelResponse> models = loraModelService.searchModelsByTags(tags, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("모델 필터링 성공", models)
        );
    }

    // ========== 샘플 이미지 관리 ==========

    /**
     * 모델 샘플 이미지 목록 조회
     */
    @GetMapping("/{modelId}/samples")
    @Operation(summary = "샘플 이미지 목록 조회", description = "모델의 샘플 이미지 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<ModelSampleResponse>>> getModelSamples(
            @Parameter(description = "모델 ID", required = true)
            @PathVariable Long modelId
    ) {
        List<ModelSampleResponse> samples = sampleService.getSamplesByModel(modelId);

        return ResponseEntity.ok(
                ApiResponse.success("샘플 이미지 조회 성공", samples)
        );
    }

    /**
     * 샘플 이미지 추가
     */
    @PostMapping("/{modelId}/samples")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "샘플 이미지 추가", description = "모델에 샘플 이미지를 추가합니다. (작성자만 가능)")
    public ResponseEntity<ApiResponse<ModelSampleResponse>> addSample(
            @Parameter(description = "모델 ID", required = true)
            @PathVariable Long modelId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal,
            @Valid @RequestBody SampleCreateRequest request
    ) {
        Long userId = Long.valueOf(principal.getAttribute("id").toString());
        ModelSampleResponse sample = sampleService.createSample(modelId, userId, request);

        return ResponseEntity.ok(
                ApiResponse.success("샘플 이미지 추가 성공", sample)
        );
    }

    /**
     * 샘플 이미지 삭제
     */
    @DeleteMapping("/{modelId}/samples/{sampleId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "샘플 이미지 삭제", description = "샘플 이미지를 삭제합니다. (작성자만 가능)")
    public ResponseEntity<ApiResponse<Void>> deleteSample(
            @Parameter(description = "모델 ID", required = true)
            @PathVariable Long modelId,
            @Parameter(description = "샘플 ID", required = true)
            @PathVariable Long sampleId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal
    ) {
        Long userId = Long.valueOf(principal.getAttribute("id").toString());
        sampleService.deleteSample(sampleId, userId);

        return ResponseEntity.ok(
                ApiResponse.success("샘플 이미지 삭제 성공")
        );
    }

    /**
     * 대표 이미지 설정
     */
    @PutMapping("/{modelId}/samples/{sampleId}/primary")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "대표 이미지 설정", description = "샘플 이미지를 대표 이미지로 설정합니다. (작성자만 가능)")
    public ResponseEntity<ApiResponse<Void>> setPrimarySample(
            @Parameter(description = "모델 ID", required = true)
            @PathVariable Long modelId,
            @Parameter(description = "샘플 ID", required = true)
            @PathVariable Long sampleId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal
    ) {
        Long userId = Long.valueOf(principal.getAttribute("id").toString());
        sampleService.setPrimarySample(sampleId, userId);

        return ResponseEntity.ok(
                ApiResponse.success("대표 이미지 설정 성공")
        );
    }

    // ========== 프롬프트 관리 ==========

    /**
     * 모델 프롬프트 목록 조회
     */
    @GetMapping("/{modelId}/prompts")
    @Operation(summary = "프롬프트 목록 조회", description = "모델의 프롬프트 예시 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<PromptResponse>>> getModelPrompts(
            @Parameter(description = "모델 ID", required = true)
            @PathVariable Long modelId
    ) {
        List<PromptResponse> prompts = promptService.getPromptsByModel(modelId);

        return ResponseEntity.ok(
                ApiResponse.success("프롬프트 목록 조회 성공", prompts)
        );
    }

    /**
     * 프롬프트 추가
     */
    @PostMapping("/{modelId}/prompts")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "프롬프트 추가", description = "모델에 프롬프트 예시를 추가합니다. (작성자만 가능)")
    public ResponseEntity<ApiResponse<PromptResponse>> addPrompt(
            @Parameter(description = "모델 ID", required = true)
            @PathVariable Long modelId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal,
            @Valid @RequestBody PromptCreateRequest request
    ) {
        Long userId = Long.valueOf(principal.getAttribute("id").toString());
        PromptResponse prompt = promptService.createPrompt(modelId, userId, request);

        return ResponseEntity.ok(
                ApiResponse.success("프롬프트 추가 성공", prompt)
        );
    }

    /**
     * 프롬프트 수정
     */
    @PutMapping("/{modelId}/prompts/{promptId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "프롬프트 수정", description = "프롬프트를 수정합니다. (작성자만 가능)")
    public ResponseEntity<ApiResponse<PromptResponse>> updatePrompt(
            @Parameter(description = "모델 ID", required = true)
            @PathVariable Long modelId,
            @Parameter(description = "프롬프트 ID", required = true)
            @PathVariable Long promptId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal,
            @Valid @RequestBody PromptUpdateRequest request
    ) {
        Long userId = Long.valueOf(principal.getAttribute("id").toString());
        PromptResponse prompt = promptService.updatePrompt(promptId, userId, request);

        return ResponseEntity.ok(
                ApiResponse.success("프롬프트 수정 성공", prompt)
        );
    }

    /**
     * 프롬프트 삭제
     */
    @DeleteMapping("/{modelId}/prompts/{promptId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "프롬프트 삭제", description = "프롬프트를 삭제합니다. (작성자만 가능)")
    public ResponseEntity<ApiResponse<Void>> deletePrompt(
            @Parameter(description = "모델 ID", required = true)
            @PathVariable Long modelId,
            @Parameter(description = "프롬프트 ID", required = true)
            @PathVariable Long promptId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal
    ) {
        Long userId = Long.valueOf(principal.getAttribute("id").toString());
        promptService.deletePrompt(promptId, userId);

        return ResponseEntity.ok(
                ApiResponse.success("프롬프트 삭제 성공")
        );
    }
}
