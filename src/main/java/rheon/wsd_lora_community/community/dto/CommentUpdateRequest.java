package rheon.wsd_lora_community.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 댓글 수정 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "댓글 수정 요청")
public class CommentUpdateRequest {

    @Schema(description = "댓글 내용", example = "수정된 댓글 내용입니다.", required = true)
    @NotBlank(message = "댓글 내용은 필수입니다.")
    private String content;
}
