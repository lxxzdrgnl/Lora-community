package rheon.wsd_lora_community.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "댓글 작성 요청")
public class CommentCreateRequest {

    @NotBlank(message = "댓글 내용은 필수입니다")
    @Size(max = 5000, message = "댓글 내용은 5000자 이하여야 합니다")
    @Schema(description = "댓글 내용", example = "정말 훌륭한 모델이네요!")
    private String content;
}
