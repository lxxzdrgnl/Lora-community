package rheon.wsd_lora_community.training.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rheon.wsd_lora_community.global.exception.CustomException;
import rheon.wsd_lora_community.global.exception.ErrorCode;
import rheon.wsd_lora_community.model.entity.LoraModel;
import rheon.wsd_lora_community.model.repository.LoraModelRepository;
import rheon.wsd_lora_community.training.dto.TrainingJobResponse;
import rheon.wsd_lora_community.training.entity.TrainingJob;
import rheon.wsd_lora_community.training.repository.TrainingJobRepository;
import rheon.wsd_lora_community.user.entity.User;
import rheon.wsd_lora_community.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 학습 작업 관리 서비스
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TrainingService {

    private final TrainingJobRepository trainingJobRepository;
    private final LoraModelRepository loraModelRepository;
    private final UserRepository userRepository;

    /**
     * 학습 작업 생성
     */
    @Transactional
    public TrainingJobResponse createTrainingJob(Long modelId, Long userId) {
        LoraModel model = loraModelRepository.findById(modelId)
                .orElseThrow(() -> new CustomException(ErrorCode.MODEL_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 권한 확인: 모델 소유자만 학습 가능
        if (!model.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // 이미 진행 중인 학습이 있는지 확인
        trainingJobRepository.findTopByModelOrderByCreatedAtDesc(model)
                .ifPresent(existingJob -> {
                    if (existingJob.isInProgress()) {
                        throw new CustomException(ErrorCode.DUPLICATE_RESOURCE);
                    }
                });

        TrainingJob job = TrainingJob.builder()
                .model(model)
                .user(user)
                .status(TrainingJob.TrainingStatus.PENDING)
                .currentEpoch(0)
                .build();

        TrainingJob saved = trainingJobRepository.save(job);

        // 모델 상태 업데이트
        model.updateStatus(LoraModel.ModelStatus.TRAINING);

        return TrainingJobResponse.from(saved);
    }

    /**
     * 학습 작업 시작
     */
    @Transactional
    public TrainingJobResponse startTraining(Long jobId, Integer totalEpochs) {
        TrainingJob job = trainingJobRepository.findById(jobId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        if (job.getStatus() != TrainingJob.TrainingStatus.PENDING) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        job.start(totalEpochs);

        return TrainingJobResponse.from(job);
    }

    /**
     * 학습 진행률 업데이트
     */
    @Transactional
    public TrainingJobResponse updateProgress(Long jobId, Integer currentEpoch, String phase) {
        TrainingJob job = trainingJobRepository.findById(jobId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!job.isInProgress()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        job.updateProgress(currentEpoch, phase);

        return TrainingJobResponse.from(job);
    }

    /**
     * 학습 완료 처리
     */
    @Transactional
    public TrainingJobResponse completeTraining(Long jobId) {
        TrainingJob job = trainingJobRepository.findById(jobId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!job.isInProgress()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        job.complete();

        // 모델 상태 업데이트
        LoraModel model = job.getModel();
        model.updateStatus(LoraModel.ModelStatus.COMPLETED);

        return TrainingJobResponse.from(job);
    }

    /**
     * 학습 실패 처리
     */
    @Transactional
    public TrainingJobResponse failTraining(Long jobId, String errorMessage) {
        TrainingJob job = trainingJobRepository.findById(jobId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!job.isInProgress()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        job.fail(errorMessage);

        // 모델 상태 업데이트
        LoraModel model = job.getModel();
        model.updateStatus(LoraModel.ModelStatus.FAILED);

        return TrainingJobResponse.from(job);
    }

    /**
     * 학습 작업 조회
     */
    public TrainingJobResponse getTrainingJob(Long jobId) {
        TrainingJob job = trainingJobRepository.findById(jobId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        return TrainingJobResponse.from(job);
    }

    /**
     * 모델의 최신 학습 작업 조회
     */
    public TrainingJobResponse getLatestTrainingJobByModel(Long modelId) {
        LoraModel model = loraModelRepository.findById(modelId)
                .orElseThrow(() -> new CustomException(ErrorCode.MODEL_NOT_FOUND));

        TrainingJob job = trainingJobRepository.findTopByModelOrderByCreatedAtDesc(model)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        return TrainingJobResponse.from(job);
    }

    /**
     * 사용자의 모든 학습 작업 조회
     */
    public List<TrainingJobResponse> getUserTrainingJobs(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return trainingJobRepository.findByUser(user).stream()
                .map(TrainingJobResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 상태별 학습 작업 조회
     */
    public List<TrainingJobResponse> getTrainingJobsByStatus(TrainingJob.TrainingStatus status) {
        return trainingJobRepository.findByStatus(status).stream()
                .map(TrainingJobResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 진행 중인 학습 작업 조회
     */
    public List<TrainingJobResponse> getInProgressJobs() {
        return trainingJobRepository.findAll().stream()
                .filter(TrainingJob::isInProgress)
                .map(TrainingJobResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * FastAPI 학습 진행률 콜백 처리
     *
     * @param userId 사용자 ID
     * @param modelId 모델 ID
     * @param message 진행 상태 메시지
     */
    @Transactional
    public void handleTrainingProgress(String userId, Integer modelId, String message) {
        Long userIdLong = Long.parseLong(userId);
        Long modelIdLong = Long.valueOf(modelId);

        // 모델 찾기
        LoraModel model = loraModelRepository.findById(modelIdLong)
                .orElseThrow(() -> new CustomException(ErrorCode.MODEL_NOT_FOUND));

        // 진행 중인 TrainingJob 찾기
        trainingJobRepository.findTopByModelOrderByCreatedAtDesc(model)
                .ifPresent(job -> {
                    if (job.isInProgress()) {
                        // phase 필드 업데이트
                        job.updateProgress(job.getCurrentEpoch(), message);
                    }
                });
    }

    /**
     * FastAPI 학습 완료 콜백 처리
     * - 사용자 ID와 모델 ID로 모델을 찾아 S3 정보 저장
     *
     * @param userId 사용자 ID
     * @param modelId 모델 ID
     * @param modelName 모델 이름
     * @param s3ModelKey S3 모델 파일 키
     * @param fileSize 파일 크기
     */
    @Transactional
    public void handleTrainingCallback(String userId, Integer modelId, String modelName, String s3ModelKey, Long fileSize) {
        Long userIdLong = Long.parseLong(userId);
        Long modelIdLong = Long.valueOf(modelId);

        // 유저 확인
        User user = userRepository.findById(userIdLong)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 모델 찾기 (modelId로 직접 조회)
        LoraModel model = loraModelRepository.findById(modelIdLong)
                .orElseThrow(() -> new CustomException(ErrorCode.MODEL_NOT_FOUND));

        // 권한 확인
        if (!model.getUser().getId().equals(userIdLong)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // S3 키로 모델 업데이트 (단일 .safetensors 파일 경로)
        model.completeTrainingWithS3(s3ModelKey, fileSize);

        // 해당 모델의 진행 중인 TrainingJob도 완료 처리
        trainingJobRepository.findTopByModelOrderByCreatedAtDesc(model)
                .ifPresent(job -> {
                    if (job.isInProgress()) {
                        job.complete();
                    }
                });
    }

    /**
     * FastAPI 학습 실패 콜백 처리
     *
     * @param userId 사용자 ID
     * @param modelId 모델 ID
     * @param errorMessage 에러 메시지
     */
    @Transactional
    public void handleTrainingFailure(String userId, Integer modelId, String errorMessage) {
        Long userIdLong = Long.parseLong(userId);
        Long modelIdLong = Long.valueOf(modelId);

        // 모델 찾기
        LoraModel model = loraModelRepository.findById(modelIdLong)
                .orElseThrow(() -> new CustomException(ErrorCode.MODEL_NOT_FOUND));

        // 모델 실패 처리
        model.failTraining();

        // TrainingJob도 실패 처리
        trainingJobRepository.findTopByModelOrderByCreatedAtDesc(model)
                .ifPresent(job -> {
                    if (job.isInProgress()) {
                        job.fail(errorMessage);
                    }
                });
    }
}
