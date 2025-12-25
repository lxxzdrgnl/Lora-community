package rheon.wsd_lora_community.training.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 학습 이미지 업로드 URL 생성 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "학습 이미지 업로드 URL 생성 요청")
public class UploadUrlsRequest {

    @Schema(description = "파일명 리스트", example = "[\"image1.png\", \"image2.png\", \"image3.jpg\"]", required = true)
    @NotEmpty(message = "파일명 리스트는 필수입니다.")
    private List<String> fileNames;
}
