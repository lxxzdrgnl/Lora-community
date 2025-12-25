package rheon.wsd_lora_community.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rheon.wsd_lora_community.admin.dto.UserAdminResponse;
import rheon.wsd_lora_community.global.exception.CustomException;
import rheon.wsd_lora_community.global.exception.ErrorCode;
import rheon.wsd_lora_community.user.entity.User;
import rheon.wsd_lora_community.user.repository.UserRepository;

/**
 * 관리자용 유저 관리 서비스
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    /**
     * 모든 유저 조회 (삭제된 유저 포함)
     */
    public Page<UserAdminResponse> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return users.map(UserAdminResponse::from);
    }

    /**
     * 삭제되지 않은 유저만 조회
     */
    public Page<UserAdminResponse> getActiveUsers(Pageable pageable) {
        Page<User> users = userRepository.findAllByDeletedAtIsNull(pageable);
        return users.map(UserAdminResponse::from);
    }

    /**
     * 유저 역할 변경
     */
    @Transactional
    public UserAdminResponse updateUserRole(Long userId, String roleString) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // Role enum 변환
        User.Role role;
        try {
            role = User.Role.valueOf(roleString.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // User 엔티티에 setRole() 메서드 추가 필요
        user.updateRole(role);

        return UserAdminResponse.from(user);
    }

    /**
     * 유저 삭제 (소프트 삭제)
     */
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        user.softDelete();
    }

    /**
     * 유저 상세 조회
     */
    public UserAdminResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return UserAdminResponse.from(user);
    }
}
