package rheon.wsd_lora_community.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rheon.wsd_lora_community.generation.entity.GenerationHistory;
import rheon.wsd_lora_community.generation.repository.GenerationHistoryRepository;
import rheon.wsd_lora_community.global.exception.CustomException;
import rheon.wsd_lora_community.global.exception.ErrorCode;
import rheon.wsd_lora_community.training.entity.TrainingJob;
import rheon.wsd_lora_community.training.repository.TrainingJobRepository;

/**
 * 관리자용 학습/생성 작업 관리 서비스
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminJobService {

    private final TrainingJobRepository trainingJobRepository;
    private final GenerationHistoryRepository generationHistoryRepository;

    /**
     * 학습 작업 강제 실패 처리
     */
    @Transactional
    public void failTrainingJob(Long jobId, String errorMessage) {
        TrainingJob job = trainingJobRepository.findById(jobId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        job.fail(errorMessage);
    }

    /**
     * 생성 작업 강제 실패 처리
     */
    @Transactional
    public void failGenerationHistory(Long historyId, String errorMessage) {
        GenerationHistory history = generationHistoryRepository.findById(historyId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        history.markAsFailed(errorMessage);
    }
}
