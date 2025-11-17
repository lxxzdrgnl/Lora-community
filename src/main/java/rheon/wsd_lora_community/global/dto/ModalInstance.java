package rheon.wsd_lora_community.global.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Modal 인스턴스 정보
 * - 멀티 인스턴스 관리를 위한 DTO
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
public class ModalInstance {

    /**
     * 인스턴스 URL
     */
    private String url;

    /**
     * 인스턴스 상태
     */
    private InstanceStatus status;

    /**
     * 현재 처리 중인 작업 수
     */
    private int currentLoad;

    /**
     * 최대 동시 처리 가능 작업 수
     */
    private int maxLoad;

    /**
     * 마지막 헬스 체크 시간
     */
    private LocalDateTime lastHealthCheck;

    /**
     * 마지막 사용 시간
     */
    private LocalDateTime lastUsedAt;

    /**
     * 연속 실패 횟수
     */
    private int consecutiveFailures;

    /**
     * 우선순위 (낮을수록 우선)
     */
    private int priority;

    /**
     * 인스턴스 상태
     */
    public enum InstanceStatus {
        IDLE,       // 대기 중 (작업 할당 가능)
        BUSY,       // 작업 처리 중
        FULL,       // 최대 부하 도달
        ERROR,      // 에러 발생
        OFFLINE     // 오프라인 (헬스 체크 실패)
    }

    /**
     * 인스턴스가 작업을 할당받을 수 있는지 확인
     */
    public boolean isAvailable() {
        return (status == InstanceStatus.IDLE || status == InstanceStatus.BUSY)
                && currentLoad < maxLoad
                && consecutiveFailures < 3;
    }

    /**
     * 작업 할당 (부하 증가)
     */
    public void assignTask() {
        this.currentLoad++;
        this.lastUsedAt = LocalDateTime.now();
        updateStatus();
    }

    /**
     * 작업 완료 (부하 감소)
     */
    public void releaseTask() {
        if (this.currentLoad > 0) {
            this.currentLoad--;
        }
        updateStatus();
    }

    /**
     * 상태 업데이트
     */
    private void updateStatus() {
        if (consecutiveFailures >= 3) {
            this.status = InstanceStatus.ERROR;
        } else if (currentLoad >= maxLoad) {
            this.status = InstanceStatus.FULL;
        } else if (currentLoad > 0) {
            this.status = InstanceStatus.BUSY;
        } else {
            this.status = InstanceStatus.IDLE;
        }
    }

    /**
     * 헬스 체크 성공
     */
    public void markHealthy() {
        this.lastHealthCheck = LocalDateTime.now();
        this.consecutiveFailures = 0;
        if (this.status == InstanceStatus.ERROR || this.status == InstanceStatus.OFFLINE) {
            updateStatus();
        }
    }

    /**
     * 헬스 체크 실패
     */
    public void markUnhealthy() {
        this.lastHealthCheck = LocalDateTime.now();
        this.consecutiveFailures++;
        if (consecutiveFailures >= 3) {
            this.status = InstanceStatus.OFFLINE;
        }
    }

    /**
     * 부하율 계산 (0.0 ~ 1.0)
     */
    public double getLoadRatio() {
        return maxLoad > 0 ? (double) currentLoad / maxLoad : 0.0;
    }
}
