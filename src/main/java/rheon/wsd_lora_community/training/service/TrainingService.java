package rheon.wsd_lora_community.training.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rheon.wsd_lora_community.global.exception.CustomException;
import rheon.wsd_lora_community.global.exception.ErrorCode;
import rheon.wsd_lora_community.model.entity.LoraModel;
import rheon.wsd_lora_community.model.entity.ModelPrompt;
import rheon.wsd_lora_community.model.entity.ModelSample;
import rheon.wsd_lora_community.model.repository.LoraModelRepository;
import rheon.wsd_lora_community.model.repository.ModelSampleRepository;
import rheon.wsd_lora_community.training.dto.TrainingJobResponse;
import rheon.wsd_lora_community.training.entity.TrainingJob;
import rheon.wsd_lora_community.training.repository.TrainingJobRepository;
import rheon.wsd_lora_community.user.entity.User;
import rheon.wsd_lora_community.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;
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
    private final ModelSampleRepository modelSampleRepository;
    private final UserRepository userRepository;
    private final rheon.wsd_lora_community.model.repository.ModelPromptRepository modelPromptRepository;

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
                .modelName(model.getTitle())  // 모델의 title을 modelName으로 사용
                .modelDescription(model.getDescription())  // 모델의 description 사용
                .status(TrainingJob.TrainingStatus.PENDING)
                .currentEpoch(0)
                .build();

        TrainingJob saved = trainingJobRepository.save(job);

        return TrainingJobResponse.from(saved);
    }

    /**
     * 학습 작업 생성 및 시작 (모델 없이)
     * - 학습 완료 후 모델 생성
     */
    @Transactional
    public TrainingJobResponse createAndStartTrainingJob(
            Long userId, String modelName, String modelDescription, Integer trainingImagesCount,
            Integer epochs, Double learningRate, Integer loraRank, String baseModel, String triggerWord) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 중복 방지: 이미 진행 중인 학습이 있는지 확인
        List<TrainingJob.TrainingStatus> activeStatuses = List.of(
                TrainingJob.TrainingStatus.PENDING,
                TrainingJob.TrainingStatus.PREPROCESSING,
                TrainingJob.TrainingStatus.TRAINING
        );
        Optional<TrainingJob> existingJob = trainingJobRepository
                .findTopByUserAndStatusInOrderByCreatedAtDesc(user, activeStatuses);

        if (existingJob.isPresent()) {
            throw new CustomException(ErrorCode.TRAINING_ALREADY_IN_PROGRESS);
        }

        // TrainingJob 생성
        TrainingJob job = TrainingJob.builder()
                .user(user)
                .model(null)  // 학습 완료 후 생성
                .modelName(modelName)
                .modelDescription(modelDescription)
                .trainingImagesCount(trainingImagesCount)
                .epochs(epochs)
                .learningRate(learningRate)
                .loraRank(loraRank)
                .baseModel(baseModel)
                .triggerWord(triggerWord)
                .status(TrainingJob.TrainingStatus.PENDING)
                .currentEpoch(0)
                .build();

        job = trainingJobRepository.save(job);
        job.start(epochs);
        job = trainingJobRepository.save(job);  // start() 후 변경사항 저장

        return TrainingJobResponse.from(job);
    }

    /**
     * 학습 작업 자동 생성 또는 기존 작업 가져오기
     * - 이미 진행 중인 작업이 있으면 에러
     * - PENDING 상태 작업이 있으면 재사용
     * - 없으면 새로 생성
     * @deprecated 더 이상 사용하지 않음 - createAndStartTrainingJob 사용
     */
    @Deprecated
    @Transactional
    public TrainingJobResponse createOrGetTrainingJob(Long modelId, Long userId, Integer totalEpochs) {
        LoraModel model = loraModelRepository.findById(modelId)
                .orElseThrow(() -> new CustomException(ErrorCode.MODEL_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 권한 확인: 모델 소유자만 학습 가능
        if (!model.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // 최신 학습 작업 확인
        TrainingJob job = trainingJobRepository.findTopByModelOrderByCreatedAtDesc(model)
                .map(existingJob -> {
                    // 이미 진행 중이면 에러
                    if (existingJob.isInProgress()) {
                        throw new CustomException(ErrorCode.DUPLICATE_RESOURCE);
                    }
                    // PENDING 상태면 재사용
                    if (existingJob.getStatus() == TrainingJob.TrainingStatus.PENDING) {
                        existingJob.start(totalEpochs);
                        return existingJob;
                    }
                    // 완료/실패 상태면 null 반환 (새로 생성)
                    return null;
                })
                .orElse(null);

        // TrainingJob이 없으면 새로 생성
        if (job == null) {
            job = TrainingJob.builder()
                    .model(model)
                    .user(user)
                    .status(TrainingJob.TrainingStatus.PENDING)
                    .currentEpoch(0)
                    .build();
            job = trainingJobRepository.save(job);
            job.start(totalEpochs);
        }

        return TrainingJobResponse.from(job);
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
     * 학습 성공 처리 - 모델 생성
     */
    @Transactional
    public Long handleTrainingSuccess(Long jobId, String s3Key, Long fileSize) {
        TrainingJob job = trainingJobRepository.findById(jobId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        // 이미 완료되었거나 실패한 경우 처리하지 않음
        if (job.getStatus() == TrainingJob.TrainingStatus.SUCCESS) {
            System.out.println("⚠️ 이미 완료된 학습 작업입니다. jobId: " + jobId);
            // 이미 생성된 모델 ID 반환
            return job.getModel() != null ? job.getModel().getId() : null;
        }
        if (job.getStatus() == TrainingJob.TrainingStatus.FAILED) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // LoraModel 생성
        LoraModel model = LoraModel.builder()
                .user(job.getUser())
                .trainingJobId(jobId)
                .title(job.getModelName())
                .description(job.getModelDescription())
                .s3Key(s3Key)
                .fileSize(fileSize)
                .isPublic(false)  // 기본값: 비공개
                .viewCount(0)
                .likeCount(0)
                .build();

        model = loraModelRepository.save(model);

        // 예시 프롬프트 생성 (triggerWord가 있는 경우)
        if (job.getTriggerWord() != null && !job.getTriggerWord().trim().isEmpty()) {
            ModelPrompt examplePrompt = ModelPrompt.builder()
                    .model(model)
                    .title("Example Prompt")
                    .prompt(job.getTriggerWord())
                    .negativePrompt("")
                    .displayOrder(0)
                    .build();
            modelPromptRepository.save(examplePrompt);
        }

        // TrainingJob에 모델 연결 및 완료 처리
        job.linkModel(model);
        job.complete();

        return model.getId();
    }

    /**
     * TrainingJob 엔티티 조회
     */
    public TrainingJob getTrainingJobEntity(Long jobId) {
        return trainingJobRepository.findById(jobId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    /**
     * 학습 완료 처리
     * @deprecated handleTrainingSuccess 사용
     */
    @Deprecated
    @Transactional
    public TrainingJobResponse completeTraining(Long jobId) {
        TrainingJob job = trainingJobRepository.findById(jobId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!job.isInProgress()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        job.complete();

        return TrainingJobResponse.from(job);
    }

    /**
     * 학습 실패 처리
     */
    @Transactional
    public TrainingJobResponse failTraining(Long jobId, String errorMessage) {
        TrainingJob job = trainingJobRepository.findById(jobId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        // 이미 완료된 경우 실패 처리하지 않음
        if (job.getStatus() == TrainingJob.TrainingStatus.SUCCESS) {
            System.out.println("⚠️ 이미 완료된 학습 작업은 실패 처리하지 않습니다. jobId: " + jobId);
            return TrainingJobResponse.from(job);
        }
        // 이미 실패한 경우 그대로 반환
        if (job.getStatus() == TrainingJob.TrainingStatus.FAILED) {
            System.out.println("⚠️ 이미 실패한 학습 작업입니다. jobId: " + jobId);
            return TrainingJobResponse.from(job);
        }

        job.fail(errorMessage);

        // 모델은 생성되지 않음 (학습 실패 시)

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

        return trainingJobRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(job -> {
                    // 모델이 있으면 대표 샘플 이미지 조회
                    if (job.getModel() != null) {
                        Optional<ModelSample> primarySample = modelSampleRepository
                                .findPrimarySampleByModelId(job.getModel().getId());

                        if (primarySample.isPresent()) {
                            String thumbnailUrl = primarySample.get().getGeneratedImage().getS3Url();
                            return TrainingJobResponse.from(job, thumbnailUrl);
                        }
                    }
                    // 모델이 없거나 샘플이 없으면 썸네일 null
                    return TrainingJobResponse.from(job);
                })
                .collect(Collectors.toList());
    }

    /**
     * 사용자의 진행 중인 학습 작업 조회
     */
    public TrainingJobResponse getUserActiveTrainingJob(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<TrainingJob.TrainingStatus> activeStatuses = List.of(
                TrainingJob.TrainingStatus.PENDING,
                TrainingJob.TrainingStatus.PREPROCESSING,
                TrainingJob.TrainingStatus.TRAINING,
                TrainingJob.TrainingStatus.UPLOADING
        );

        return trainingJobRepository
                .findTopByUserAndStatusInOrderByCreatedAtDesc(user, activeStatuses)
                .map(TrainingJobResponse::from)
                .orElse(null);
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
        model.setModelFileInfo(s3ModelKey, fileSize);

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

        // TrainingJob 실패 처리
        trainingJobRepository.findTopByModelOrderByCreatedAtDesc(model)
                .ifPresent(job -> {
                    if (job.isInProgress()) {
                        job.fail(errorMessage);
                    }
                });
    }

    /**
     * 학습 작업 삭제
     * - 진행 중인 작업은 삭제 불가
     * - 연결된 모델의 trainingJobId를 null로 설정
     * - TrainingJob 레코드 삭제
     *
     * @param jobId 학습 작업 ID
     * @param userId 요청한 사용자 ID
     */
    @Transactional
    public void deleteTrainingJob(Long jobId, Long userId) {
        TrainingJob job = trainingJobRepository.findById(jobId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        // 권한 확인: 본인의 작업만 삭제 가능
        if (!job.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // 진행 중인 작업은 삭제 불가
        if (job.isInProgress()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "진행 중인 학습 작업은 삭제할 수 없습니다.");
        }

        // 연결된 모델이 있다면 trainingJobId를 null로 설정
        if (job.getModel() != null) {
            LoraModel model = job.getModel();
            model.setTrainingJobId(null);
            loraModelRepository.save(model);
        }

        // TrainingJob 삭제
        trainingJobRepository.delete(job);
    }
}
