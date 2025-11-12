package rheon.wsd_lora_community.entity.model;

import jakarta.persistence.*;
import lombok.*;
import rheon.wsd_lora_community.entity.common.BaseEntity;

/**
 * 태그 엔티티
 */
@Entity
@Table(name = "tags", indexes = {
        @Index(name = "idx_name", columnList = "name"),
        @Index(name = "idx_category", columnList = "category")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Tag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private TagCategory category = TagCategory.OTHER;

    @Column
    @Builder.Default
    private Integer usageCount = 0;

    /**
     * 태그 카테고리
     */
    public enum TagCategory {
        STYLE,      // 스타일 (예: manga, anime, realistic)
        CHARACTER,  // 캐릭터 (예: girl, boy, animal)
        GENRE,      // 장르 (예: action, romance, fantasy)
        OTHER       // 기타
    }

    // 비즈니스 메서드
    public void incrementUsageCount() {
        this.usageCount++;
    }

    public void decrementUsageCount() {
        if (this.usageCount > 0) {
            this.usageCount--;
        }
    }

    public void updateCategory(TagCategory category) {
        this.category = category;
    }
}
