package rheon.wsd_lora_community.generation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자가 이미지 생성에 사용한 모델 목록 응답 DTO
 * - 생성 히스토리 필터링용
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AvailableModelResponse {

    /**
     * 모델 ID
     */
    private Long id;

    /**
     * 모델 제목
     */
    private String title;
}
