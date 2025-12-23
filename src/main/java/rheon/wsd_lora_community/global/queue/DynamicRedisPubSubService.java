package rheon.wsd_lora_community.global.queue;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 동적 Redis Pub/Sub 관리 서비스
 * - 작업이 진행 중일 때만 Pub/Sub을 활성화하여 Redis 읽기 비용 절감
 * - 작업 시작 시 구독, 작업 완료/실패 시 구독 해제
 */
@Slf4j
@Service
public class DynamicRedisPubSubService {

    private final RedisMessageListenerContainer container;
    private final MessageListener messageListener;
    private final ChannelTopic trainingTopic = new ChannelTopic("job:training:progress");
    private final ChannelTopic generationTopic = new ChannelTopic("job:generation:progress");

    // 활성 작업 카운터 (학습, 생성 각각)
    private final AtomicInteger activeTrainingJobs = new AtomicInteger(0);
    private final AtomicInteger activeGenerationJobs = new AtomicInteger(0);

    public DynamicRedisPubSubService(
            RedisMessageListenerContainer container,
            RedisMessageSubscriber messageListener
    ) {
        this.container = container;
        this.messageListener = messageListener;
    }

    /**
     * 학습 작업 시작 - Pub/Sub 구독 활성화
     */
    public synchronized void startTrainingJob() {
        int count = activeTrainingJobs.incrementAndGet();
        log.info("🎬 학습 작업 시작: 활성 작업 수 = {}", count);

        if (count == 1) {
            // 첫 작업 시작 - 구독 활성화
            container.addMessageListener(messageListener, trainingTopic);
            if (!container.isRunning()) {
                container.start();
            }
            log.info("📡 Redis Pub/Sub 활성화: job:training:progress");
        }
    }

    /**
     * 학습 작업 종료 - 모든 작업 완료 시 Pub/Sub 구독 해제
     */
    public synchronized void stopTrainingJob() {
        int count = activeTrainingJobs.decrementAndGet();
        log.info("🛑 학습 작업 종료: 활성 작업 수 = {}", count);

        if (count <= 0) {
            activeTrainingJobs.set(0); // 음수 방지
            // 모든 작업 완료 - 구독 해제
            container.removeMessageListener(messageListener, trainingTopic);
            log.info("📴 Redis Pub/Sub 비활성화: job:training:progress");

            // 생성 작업도 없으면 컨테이너 중지
            if (activeGenerationJobs.get() <= 0) {
                stopContainerIfIdle();
            }
        }
    }

    /**
     * 생성 작업 시작 - Pub/Sub 구독 활성화
     */
    public synchronized void startGenerationJob() {
        int count = activeGenerationJobs.incrementAndGet();
        log.info("🎬 생성 작업 시작: 활성 작업 수 = {}", count);

        if (count == 1) {
            // 첫 작업 시작 - 구독 활성화
            container.addMessageListener(messageListener, generationTopic);
            if (!container.isRunning()) {
                container.start();
            }
            log.info("📡 Redis Pub/Sub 활성화: job:generation:progress");
        }
    }

    /**
     * 생성 작업 종료 - 모든 작업 완료 시 Pub/Sub 구독 해제
     */
    public synchronized void stopGenerationJob() {
        int count = activeGenerationJobs.decrementAndGet();
        log.info("🛑 생성 작업 종료: 활성 작업 수 = {}", count);

        if (count <= 0) {
            activeGenerationJobs.set(0); // 음수 방지
            // 모든 작업 완료 - 구독 해제
            container.removeMessageListener(messageListener, generationTopic);
            log.info("📴 Redis Pub/Sub 비활성화: job:generation:progress");

            // 학습 작업도 없으면 컨테이너 중지
            if (activeTrainingJobs.get() <= 0) {
                stopContainerIfIdle();
            }
        }
    }

    /**
     * 모든 작업이 없으면 컨테이너 중지
     */
    private void stopContainerIfIdle() {
        if (activeTrainingJobs.get() <= 0 && activeGenerationJobs.get() <= 0) {
            if (container.isRunning()) {
                container.stop();
                log.info("⏸️ Redis Pub/Sub 컨테이너 중지 (모든 작업 완료)");
            }
        }
    }

    /**
     * 활성 작업 수 조회 (디버깅용)
     */
    public String getStatus() {
        return String.format("Training: %d, Generation: %d, Container: %s",
                activeTrainingJobs.get(),
                activeGenerationJobs.get(),
                container.isRunning() ? "RUNNING" : "STOPPED");
    }
}
