package rheon.wsd_lora_community.global.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import rheon.wsd_lora_community.global.dto.ModalInstance;
import rheon.wsd_lora_community.global.dto.ModalInstance.InstanceStatus;
import rheon.wsd_lora_community.global.exception.CustomException;
import rheon.wsd_lora_community.global.exception.ErrorCode;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Modal 멀티 인스턴스 관리 서비스
 * - 인스턴스 풀 관리
 * - 로드 밸런싱
 * - 헬스 체크
 */
@Slf4j
@Service
public class ModalInstanceManager {

    private final WebClient webClient;

    @Value("${modal.enabled:false}")
    private boolean modalEnabled;

    @Value("#{'${modal.instances:http://127.0.0.1:8000,http://127.0.0.1:8001,http://127.0.0.1:8002}'.split(',')}")
    private List<String> instanceUrls;

    @Value("${modal.max-instances:10}")
    private int maxInstances;

    @Value("${modal.load-balancing-strategy:LEAST_LOAD}")
    private String loadBalancingStrategy;

    // 인스턴스 풀 (Thread-safe)
    private final List<ModalInstance> instances = new CopyOnWriteArrayList<>();

    // 라운드 로빈용 인덱스
    private final AtomicInteger roundRobinIndex = new AtomicInteger(0);

    public ModalInstanceManager(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * 초기화: application.yml의 인스턴스 목록을 풀에 추가
     */
    @PostConstruct
    public void init() {
        if (!modalEnabled) {
            log.info("Modal is disabled. Using local FastAPI instead.");
            return;
        }

        log.info("Initializing Modal instance pool...");
        for (int i = 0; i < instanceUrls.size() && i < maxInstances; i++) {
            String url = instanceUrls.get(i);
            ModalInstance instance = ModalInstance.builder()
                    .url(url)
                    .status(InstanceStatus.IDLE)
                    .currentLoad(0)
                    .maxLoad(10)  // 인스턴스당 최대 10개 작업
                    .lastHealthCheck(LocalDateTime.now())
                    .lastUsedAt(LocalDateTime.now())
                    .consecutiveFailures(0)
                    .priority(i)
                    .build();
            instances.add(instance);
            log.info("Added instance: {} (priority: {})", url, i);
        }
        log.info("Modal instance pool initialized with {} instances", instances.size());
    }

    /**
     * 로드 밸런싱: 최적의 인스턴스 선택
     */
    public ModalInstance selectInstance() {
        if (!modalEnabled) {
            throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        // 사용 가능한 인스턴스 필터링
        List<ModalInstance> availableInstances = instances.stream()
                .filter(ModalInstance::isAvailable)
                .toList();

        if (availableInstances.isEmpty()) {
            log.error("No available instances! Total: {}, Offline: {}, Full: {}",
                    instances.size(),
                    instances.stream().filter(i -> i.getStatus() == InstanceStatus.OFFLINE).count(),
                    instances.stream().filter(i -> i.getStatus() == InstanceStatus.FULL).count()
            );
            throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        ModalInstance selected = switch (loadBalancingStrategy) {
            case "ROUND_ROBIN" -> selectRoundRobin(availableInstances);
            case "LEAST_LOAD" -> selectLeastLoad(availableInstances);
            case "PRIORITY" -> selectPriority(availableInstances);
            default -> selectLeastLoad(availableInstances);
        };

        log.info("Selected instance: {} (strategy: {}, load: {}/{})",
                selected.getUrl(), loadBalancingStrategy, selected.getCurrentLoad(), selected.getMaxLoad());

        return selected;
    }

    /**
     * 라운드 로빈 전략
     */
    private ModalInstance selectRoundRobin(List<ModalInstance> availableInstances) {
        int index = roundRobinIndex.getAndIncrement() % availableInstances.size();
        return availableInstances.get(index);
    }

    /**
     * 최소 부하 전략 (가장 부하가 적은 인스턴스)
     */
    private ModalInstance selectLeastLoad(List<ModalInstance> availableInstances) {
        return availableInstances.stream()
                .min(Comparator.comparingDouble(ModalInstance::getLoadRatio))
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    /**
     * 우선순위 전략 (우선순위가 높은 인스턴스부터)
     */
    private ModalInstance selectPriority(List<ModalInstance> availableInstances) {
        return availableInstances.stream()
                .min(Comparator.comparingInt(ModalInstance::getPriority))
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    /**
     * 인스턴스 헬스 체크 (30초마다 실행)
     */
    @Scheduled(fixedRateString = "${modal.health-check-interval:30000}")
    public void performHealthChecks() {
        if (!modalEnabled) {
            return;
        }

        log.debug("Performing health checks on {} instances", instances.size());
        for (ModalInstance instance : instances) {
            checkInstanceHealth(instance);
        }
    }

    /**
     * 개별 인스턴스 헬스 체크
     */
    private void checkInstanceHealth(ModalInstance instance) {
        webClient.get()
                .uri(instance.getUrl() + "/")
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(5))
                .subscribe(
                        response -> {
                            instance.markHealthy();
                            log.debug("Health check OK: {}", instance.getUrl());
                        },
                        error -> {
                            instance.markUnhealthy();
                            log.warn("Health check FAILED: {} (failures: {})",
                                    instance.getUrl(), instance.getConsecutiveFailures());
                        }
                );
    }

    /**
     * 인스턴스 상태 조회
     */
    public List<ModalInstance> getInstances() {
        return instances;
    }

    /**
     * 통계 정보
     */
    public InstancePoolStats getStats() {
        long idleCount = instances.stream().filter(i -> i.getStatus() == InstanceStatus.IDLE).count();
        long busyCount = instances.stream().filter(i -> i.getStatus() == InstanceStatus.BUSY).count();
        long fullCount = instances.stream().filter(i -> i.getStatus() == InstanceStatus.FULL).count();
        long errorCount = instances.stream().filter(i -> i.getStatus() == InstanceStatus.ERROR).count();
        long offlineCount = instances.stream().filter(i -> i.getStatus() == InstanceStatus.OFFLINE).count();
        int totalLoad = instances.stream().mapToInt(ModalInstance::getCurrentLoad).sum();

        return new InstancePoolStats(
                instances.size(),
                (int) idleCount,
                (int) busyCount,
                (int) fullCount,
                (int) errorCount,
                (int) offlineCount,
                totalLoad
        );
    }

    /**
     * 인스턴스 풀 통계
     */
    public record InstancePoolStats(
            int total,
            int idle,
            int busy,
            int full,
            int error,
            int offline,
            int totalLoad
    ) {}
}
