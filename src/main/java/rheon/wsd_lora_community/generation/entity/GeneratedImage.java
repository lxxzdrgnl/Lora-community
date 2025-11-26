package rheon.wsd_lora_community.generation.entity;

import jakarta.persistence.*;
import lombok.*;
import rheon.wsd_lora_community.global.dto.BaseEntity;

/**
 * 생성된 이미지 엔티티
 * - 한 번의 GenerationHistory 요청으로 여러 이미지가 생성될 수 있음 (1:N 관계)
 * - S3에 저장된 이미지 URL을 관리
 *
 * NOTE: GenerationHistory와의 관계를 통해 prompt, steps 등의 정보를 가져옴
 * Repository에서 JOIN FETCH를 사용하여 N+1 문제 해결
 */
@Entity
@Table(name = "generated_images", indexes = {
        @Index(name = "idx_generated_images_history", columnList = "generation_history_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class GeneratedImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generation_history_id", nullable = false)
    private GenerationHistory generationHistory;

    /**
     * S3에 저장된 이미지 URL
     * 예: https://lora-generated-image-bucket.s3.ap-southeast-2.amazonaws.com/user-123/20250118_143025_1.png
     */
    @Column(name = "s3_url", nullable = false, length = 500)
    private String s3Url;

    /**
     * S3 키 (버킷 내 경로)
     * 예: user-123/20250118_143025_1.png
     */
    @Column(name = "s3_key", nullable = false, length = 255)
    private String s3Key;

    /**
     * 파일 크기 (바이트)
     */
    @Column(name = "file_size")
    private Long fileSize;

    /**
     * 이미지 순서 (같은 GenerationHistory 내에서 생성 순서)
     * 1부터 시작
     */
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    // 비즈니스 메서드
    public void updateDisplayOrder(Integer newOrder) {
        this.displayOrder = newOrder;
    }
}
