package rheon.wsd_lora_community.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rheon.wsd_lora_community.user.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    // 삭제되지 않은 유저만 조회
    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    Optional<User> findByOauthProviderAndOauthProviderId(User.OAuthProvider provider, String providerId);

    // 삭제되지 않은 유저만 조회
    Optional<User> findByOauthProviderAndOauthProviderIdAndDeletedAtIsNull(User.OAuthProvider provider, String providerId);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Optional<User> findByIdAndDeletedAtIsNull(Long id);

    Page<User> findByNicknameContainingIgnoreCase(String nickname, Pageable pageable);

    // 모든 유저 조회 (관리자용, 삭제된 유저 포함)
    Page<User> findAll(Pageable pageable);

    // 삭제되지 않은 유저만 조회 (관리자용)
    Page<User> findAllByDeletedAtIsNull(Pageable pageable);
}
