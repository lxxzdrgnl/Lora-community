package rheon.wsd_lora_community.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import org.springframework.web.bind.annotation.*;
import rheon.wsd_lora_community.admin.dto.RoleUpdateRequest;
import rheon.wsd_lora_community.admin.dto.UserAdminResponse;
import rheon.wsd_lora_community.admin.service.AdminUserService;
import rheon.wsd_lora_community.global.config.CommonApiResponses;
import rheon.wsd_lora_community.global.dto.ApiResponse;
import rheon.wsd_lora_community.global.dto.ErrorResponse;

/**
 * 관리자용 유저 관리 컨트롤러
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "관리자 전용 API")
@CommonApiResponses
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    /**
     * 모든 유저 조회 (삭제된 유저 포함)
     */
    @GetMapping
    @Operation(summary = "모든 유저 조회", description = "삭제된 유저를 포함한 모든 유저를 조회합니다. (관리자 전용)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요 (JWT 토큰 없음 또는 만료)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN 권한 필요)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<Page<UserAdminResponse>>> getAllUsers(
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<UserAdminResponse> users = adminUserService.getAllUsers(pageable);

        return ResponseEntity.ok(
                ApiResponse.success("유저 목록 조회 성공", users)
        );
    }

    /**
     * 활성 유저 조회 (삭제되지 않은 유저만)
     */
    @GetMapping("/active")
    @Operation(summary = "활성 유저 조회", description = "삭제되지 않은 유저만 조회합니다. (관리자 전용)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<Page<UserAdminResponse>>> getActiveUsers(
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<UserAdminResponse> users = adminUserService.getActiveUsers(pageable);

        return ResponseEntity.ok(
                ApiResponse.success("활성 유저 목록 조회 성공", users)
        );
    }

    /**
     * 유저 상세 조회
     */
    @GetMapping("/{userId}")
    @Operation(summary = "유저 상세 조회", description = "특정 유저의 상세 정보를 조회합니다. (관리자 전용)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<UserAdminResponse>> getUserById(
            @Parameter(description = "유저 ID", required = true)
            @PathVariable Long userId
    ) {
        UserAdminResponse user = adminUserService.getUserById(userId);

        return ResponseEntity.ok(
                ApiResponse.success("유저 조회 성공", user)
        );
    }

    /**
     * 유저 역할 변경
     */
    @PutMapping("/{userId}/role")
    @Operation(summary = "유저 역할 변경", description = "유저의 역할을 변경합니다. (USER, TEST, ADMIN) (관리자 전용)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "역할 변경 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (유효하지 않은 역할)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<UserAdminResponse>> updateUserRole(
            @Parameter(description = "유저 ID", required = true)
            @PathVariable Long userId,
            @Valid @RequestBody RoleUpdateRequest request
    ) {
        UserAdminResponse user = adminUserService.updateUserRole(userId, request.getRole());

        return ResponseEntity.ok(
                ApiResponse.success("유저 역할 변경 성공", user)
        );
    }

    /**
     * 유저 삭제 (소프트 삭제)
     */
    @DeleteMapping("/{userId}")
    @Operation(summary = "유저 삭제", description = "유저를 소프트 삭제합니다. (관리자 전용)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @Parameter(description = "유저 ID", required = true)
            @PathVariable Long userId
    ) {
        adminUserService.deleteUser(userId);

        return ResponseEntity.ok(
                ApiResponse.success("유저 삭제 성공", null)
        );
    }
}
