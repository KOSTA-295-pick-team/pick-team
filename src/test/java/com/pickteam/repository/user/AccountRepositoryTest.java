package com.pickteam.repository.user;

import com.pickteam.config.TestQueryDslConfig;
import com.pickteam.domain.enums.UserRole;
import com.pickteam.domain.user.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 계정 리포지토리 테스트
 * @DataJpaTest를 사용하여 JPA 관련 설정만 로드
 */
@DataJpaTest
@Import(TestQueryDslConfig.class)
@ActiveProfiles("test")
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = Account.builder()
                .email("test@example.com")
                .password("password")
                .name("테스트 사용자")
                .mbti("ENFP")
                .disposition("적극적")
                .role(UserRole.USER)
                .build();
    }

    @Test
    @DisplayName("이메일로 활성 계정의 존재 여부를 확인할 수 있다")
    void existsByEmailAndDeletedAtIsNull_ActiveAccount_ReturnsTrue() {
        // Given
        accountRepository.save(testAccount);

        // When
        boolean exists = accountRepository.existsByEmailAndDeletedAtIsNull("test@example.com");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("삭제된 계정은 활성 계정 존재 확인에서 false를 반환한다")
    void existsByEmailAndDeletedAtIsNull_DeletedAccount_ReturnsFalse() {
        // Given
        testAccount.markDeleted();
        accountRepository.save(testAccount);

        // When
        boolean exists = accountRepository.existsByEmailAndDeletedAtIsNull("test@example.com");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("이메일로 활성 사용자를 조회할 수 있다")
    void findByEmailAndDeletedAtIsNull_ActiveAccount_ReturnsAccount() {
        // Given
        Account savedAccount = accountRepository.save(testAccount);

        // When
        Optional<Account> result = accountRepository.findByEmailAndDeletedAtIsNull("test@example.com");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedAccount.getId());
        assertThat(result.get().getName()).isEqualTo("테스트 사용자");
    }

    @Test
    @DisplayName("삭제된 사용자는 활성 사용자 조회에서 반환되지 않는다")
    void findByEmailAndDeletedAtIsNull_DeletedAccount_ReturnsEmpty() {
        // Given
        testAccount.markDeleted();
        accountRepository.save(testAccount);

        // When
        Optional<Account> result = accountRepository.findByEmailAndDeletedAtIsNull("test@example.com");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("ID로 활성 사용자를 조회할 수 있다")
    void findByIdAndDeletedAtIsNull_ActiveAccount_ReturnsAccount() {
        // Given
        Account savedAccount = accountRepository.save(testAccount);

        // When
        Optional<Account> result = accountRepository.findByIdAndDeletedAtIsNull(savedAccount.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("전체 활성 사용자 목록을 조회할 수 있다")
    void findAllByDeletedAtIsNull_MultipleAccounts_ReturnsActiveOnly() {
        // Given
        Account activeAccount1 = Account.builder()
                .email("active1@example.com")
                .password("password")
                .name("활성 사용자 1")
                .build();

        Account activeAccount2 = Account.builder()
                .email("active2@example.com")
                .password("password")
                .name("활성 사용자 2")
                .build();

        Account deletedAccount = Account.builder()
                .email("deleted@example.com")
                .password("password")
                .name("삭제된 사용자")
                .build();
        deletedAccount.markDeleted();

        accountRepository.save(activeAccount1);
        accountRepository.save(activeAccount2);
        accountRepository.save(deletedAccount);

        // When
        List<Account> result = accountRepository.findAllByDeletedAtIsNull();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Account::getName)
                .containsExactlyInAnyOrder("활성 사용자 1", "활성 사용자 2");
    }

    @Test
    @DisplayName("MBTI로 추천 팀원을 조회할 수 있다")
    void findByMbtiAndDeletedAtIsNullAndIdNot_SameMbti_ReturnsRecommendedUsers() {
        // Given
        Account currentUser = accountRepository.save(testAccount);

        Account sameTypeMember = Account.builder()
                .email("same@example.com")
                .password("password")
                .name("같은 타입 사용자")
                .mbti("ENFP")
                .build();
        accountRepository.save(sameTypeMember);

        Account differentTypeMember = Account.builder()
                .email("different@example.com")
                .password("password")
                .name("다른 타입 사용자")
                .mbti("INTJ")
                .build();
        accountRepository.save(differentTypeMember);

        // When
        List<Account> result = accountRepository.findByMbtiAndDeletedAtIsNullAndIdNot("ENFP", currentUser.getId());

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("같은 타입 사용자");
    }

    @Test
    @DisplayName("성향으로 추천 팀원을 조회할 수 있다")
    void findByDispositionContainingAndDeletedAtIsNullAndIdNot_SimilarDisposition_ReturnsRecommendedUsers() {
        // Given
        Account currentUser = accountRepository.save(testAccount);

        Account similarDispositionMember = Account.builder()
                .email("similar@example.com")
                .password("password")
                .name("비슷한 성향 사용자")
                .disposition("적극적이고 활발한")
                .build();
        accountRepository.save(similarDispositionMember);

        Account differentDispositionMember = Account.builder()
                .email("different@example.com")
                .password("password")
                .name("다른 성향 사용자")
                .disposition("신중하고 조용한")
                .build();
        accountRepository.save(differentDispositionMember);

        // When
        List<Account> result = accountRepository.findByDispositionContainingAndDeletedAtIsNullAndIdNot(
                "적극적", currentUser.getId());

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("비슷한 성향 사용자");
    }

    @Test
    @DisplayName("역할별로 사용자를 조회할 수 있다")
    void findByRoleAndDeletedAtIsNull_UserRole_ReturnsUsersWithRole() {
        // Given
        Account adminUser = Account.builder()
                .email("admin@example.com")
                .password("password")
                .name("관리자")
                .role(UserRole.ADMIN)
                .build();

        Account regularUser = Account.builder()
                .email("user@example.com")
                .password("password")
                .name("일반 사용자")
                .role(UserRole.USER)
                .build();

        accountRepository.save(adminUser);
        accountRepository.save(regularUser);

        // When
        List<Account> adminUsers = accountRepository.findByRoleAndDeletedAtIsNull(UserRole.ADMIN);
        List<Account> normalUsers = accountRepository.findByRoleAndDeletedAtIsNull(UserRole.USER);

        // Then
        assertThat(adminUsers).hasSize(1);
        assertThat(adminUsers.get(0).getName()).isEqualTo("관리자");

        assertThat(normalUsers).hasSize(1);
        assertThat(normalUsers.get(0).getName()).isEqualTo("일반 사용자");
    }

    @Test
    @DisplayName("이름으로 사용자를 검색할 수 있다")
    void findByNameContainingIgnoreCaseAndDeletedAtIsNull_PartialName_ReturnsMatchingUsers() {
        // Given
        Account user1 = Account.builder()
                .email("john@example.com")
                .password("password")
                .name("John Doe")
                .build();

        Account user2 = Account.builder()
                .email("jane@example.com")
                .password("password")
                .name("Jane Smith")
                .build();

        Account user3 = Account.builder()
                .email("bob@example.com")
                .password("password")
                .name("Bob Johnson")
                .build();

        accountRepository.save(user1);
        accountRepository.save(user2);
        accountRepository.save(user3);

        // When
        List<Account> johnResults = accountRepository.findByNameContainingIgnoreCaseAndDeletedAtIsNull("john");

        // Then
        assertThat(johnResults).hasSize(2);
        assertThat(johnResults).extracting(Account::getName)
                .containsExactlyInAnyOrder("John Doe", "Bob Johnson");
    }
}
