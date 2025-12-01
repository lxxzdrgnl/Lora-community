package rheon.wsd_lora_community.global.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rheon.wsd_lora_community.global.client.FastApiClient;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.*;

/**
 * GPU 리소스 관리 서비스 (대기열 지원)
 *
 * Modal에서 학습 + 생성을 합쳐서 최대 10개까지만 동시 실행하도록 제한합니다.
 * GPU가 꽉 차면 대기열에 추가하고, GPU가 여유 생기면 자동으로 실행합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GpuResourceManager {

    private static final int MAX_GPU_TASKS = 10; // 학습 + 생성 합쳐서 최대 10개

    private final Semaphore gpuSemaphore = new Semaphore(MAX_GPU_TASKS);
    private final BlockingQueue<GpuTask> taskQueue = new LinkedBlockingQueue<>();
    private final Map<Long, GpuTask> runningTasks = new ConcurrentHashMap<>();
    private final ExecutorService queueProcessor = Executors.newSingleThreadExecutor();
    private final FastApiClient fastApiClient;

    // 초기화 블록 - 대기열 처리 스레드 시작
    {
        queueProcessor.submit(this::processQueue);
    }

    /**
     * 작업 유형
     */
    public enum TaskType {
        TRAINING,
        GENERATION
    }

    /**
     * GPU 작업 정보
     */
    public static class GpuTask {
        public final Long taskId; // jobId 또는 historyId
        public final TaskType type;
        public final Runnable execution; // 실제 실행할 작업
        public final long queuedAt; // 큐에 추가된 시간

        public GpuTask(Long taskId, TaskType type, Runnable execution) {
            this.taskId = taskId;
            this.type = type;
            this.execution = execution;
            this.queuedAt = System.currentTimeMillis();
        }
    }

    /**
     * 대기열 처리 스레드
     * GPU가 여유 생기면 대기 중인 작업을 꺼내서 실행
     */
    private void processQueue() {
        log.info("🔄 GPU 대기열 처리 스레드 시작");

        while (!Thread.currentThread().isInterrupted()) {
            try {
                // 대기열에서 작업 꺼내기 (블로킹)
                GpuTask task = taskQueue.take();

                // GPU 리소스 획득 (블로킹 - 여유 생길 때까지 대기)
                gpuSemaphore.acquire();

                log.info("✅ GPU 리소스 획득 - 대기 중이던 작업 실행: {} (type: {}, 대기 시간: {}ms)",
                        task.taskId, task.type,
                        System.currentTimeMillis() - task.queuedAt);

                // 실행 중 작업 목록에 추가
                runningTasks.put(task.taskId, task);

                // 작업 실행 (별도 스레드에서)
                CompletableFuture.runAsync(task.execution)
                        .exceptionally(ex -> {
                            log.error("❌ GPU 작업 실행 중 오류 발생: {} ({})", task.taskId, ex.getMessage());
                            release(task.taskId); // 실패해도 리소스 반환
                            return null;
                        });

            } catch (InterruptedException e) {
                log.warn("⚠️ GPU 대기열 처리 스레드 중단");
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * GPU 작업 큐에 추가
     *
     * @param taskId 작업 ID (jobId 또는 historyId)
     * @param type 작업 유형
     * @param execution 실행할 작업
     * @return 큐에 추가 성공 여부
     */
    public boolean enqueueTask(Long taskId, TaskType type, Runnable execution) {
        GpuTask task = new GpuTask(taskId, type, execution);

        boolean added = taskQueue.offer(task);
        if (added) {
            int queueSize = taskQueue.size();
            int running = MAX_GPU_TASKS - gpuSemaphore.availablePermits();
            log.info("📥 GPU 작업 큐에 추가: {} (type: {}, 대기열: {}개, 실행 중: {}/10)",
                    taskId, type, queueSize, running);
        } else {
            log.error("❌ GPU 작업 큐 추가 실패: {} (큐가 꽉 참)", taskId);
        }

        return added;
    }

    /**
     * GPU 리소스 반환
     *
     * @param taskId 작업 ID
     */
    public void release(Long taskId) {
        runningTasks.remove(taskId);
        gpuSemaphore.release();

        int available = gpuSemaphore.availablePermits();
        int queueSize = taskQueue.size();
        log.info("🔓 GPU 리소스 반환: {} (사용 중: {}/10, 대기열: {}개)",
                taskId, MAX_GPU_TASKS - available, queueSize);
    }

    /**
     * 현재 대기열 크기 조회
     */
    public int getQueueSize() {
        return taskQueue.size();
    }

    /**
     * 현재 사용 가능한 GPU 슬롯 수 조회
     */
    public int getAvailableSlots() {
        return gpuSemaphore.availablePermits();
    }

    /**
     * 현재 사용 중인 GPU 슬롯 수 조회
     */
    public int getUsedSlots() {
        return MAX_GPU_TASKS - gpuSemaphore.availablePermits();
    }

    /**
     * GPU 리소스 상태 조회
     */
    public GpuResourceStatus getStatus() {
        int available = gpuSemaphore.availablePermits();
        int used = MAX_GPU_TASKS - available;
        int queued = taskQueue.size();
        return new GpuResourceStatus(MAX_GPU_TASKS, used, available, queued);
    }

    /**
     * GPU 리소스 상태 정보
     */
    public static class GpuResourceStatus {
        public final int total;
        public final int used;
        public final int available;
        public final int queued; // 대기 중인 작업 수

        public GpuResourceStatus(int total, int used, int available, int queued) {
            this.total = total;
            this.used = used;
            this.available = available;
            this.queued = queued;
        }
    }
}
