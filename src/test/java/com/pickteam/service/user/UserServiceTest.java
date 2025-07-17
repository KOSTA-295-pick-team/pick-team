package com.pickteam.service.user;

import com.pickteam.domain.enums.UserRole;
import com.pickteam.domain.user.Account;
import com.pickteam.dto.user.*;
import com.pickteam.exception.user.DuplicateEmailException;
import com.pickteam.exception.user.UserNotFoundException;
import com.pickteam.exception.validation.ValidationException;
import com.pickteam.repository.user.AccountRepository;
import com.pickteam.repository.user.RefreshTokenRepository;
import com.pickteam.repository.user.UserHashtagRepository;
import com.pickteam.repository.user.UserHashtagListRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * 사용자 서비스 단위 테스트
 * @ExtendWith(MockitoExtension.class)를 사용하여 Mock 객체들을 주입
 * 비즈니스 로직만 단위 테스트로 검증
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserHashtagRepository userHashtagRepository;

    @Mock
    private UserHashtagListRepository userHashtagListRepository;

    @Mock
    private AuthService authService;

    @Mock
    private EmailService emailService;

    @Mock
    private ValidationService validationService;

    @Test
    @DisplayName("이메일 중복 검사를 할 수 있다 - 중복된 이메일")
    void checkDuplicateId_ExistingEmail_ReturnsTrue() {
        // Given
        String email = "test@gmail.com";
        given(accountRepository.existsByEmailAndDeletedAtIsNull(email)).willReturn(true);

        // When
        boolean result = userService.checkDuplicateId(email);

        // Then
        assertThat(result).isTrue();
        verify(accountRepository).existsByEmailAndDeletedAtIsNull(email);
    }

    @Test
    @DisplayName("이메일 중복 검사를 할 수 있다 - 사용 가능한 이메일")
    void checkDuplicateId_AvailableEmail_ReturnsFalse() {
        // Given
        String email = "available@gmail.com";
        given(accountRepository.existsByEmailAndDeletedAtIsNull(email)).willReturn(false);

        // When
        boolean result = userService.checkDuplicateId(email);

        // Then
        assertThat(result).isFalse();
        verify(accountRepository).existsByEmailAndDeletedAtIsNull(email);
    }

    @Test
    @DisplayName("비밀번호 유효성 검사를 할 수 있다 - 유효한 비밀번호")
    void validatePassword_ValidPassword_ReturnsTrue() {
        // Given
        String validPassword = "ValidPass123!";
        given(validationService.isValidPassword(validPassword)).willReturn(true);

        // When
        boolean result = userService.validatePassword(validPassword);

        // Then
        assertThat(result).isTrue();
        verify(validationService).isValidPassword(validPassword);
    }

    @Test
    @DisplayName("비밀번호 유효성 검사를 할 수 있다 - 무효한 비밀번호")
    void validatePassword_InvalidPassword_ReturnsFalse() {
        // Given
        String invalidPassword = "weak";
        given(validationService.isValidPassword(invalidPassword)).willReturn(false);

        // When
        boolean result = userService.validatePassword(invalidPassword);

        // Then
        assertThat(result).isFalse();
        verify(validationService).isValidPassword(invalidPassword);
    }

    @Test
    @DisplayName("사용자 프로필을 조회할 수 있다")
    void getUserProfile_ValidUserId_ReturnsUserProfile() {
        // Given
        Long userId = 1L;
        Account account = createTestAccount();
        given(accountRepository.findByIdAndIsDeletedFalse(userId)).willReturn(Optional.of(account));

        // When
        UserProfileResponse result = userService.getUserProfile(userId);

        // Then
        assertThat(result.getName()).isEqualTo("테스트 사용자");
        assertThat(result.getEmail()).isEqualTo("test@gmail.com");
        assertThat(result.getMbti()).isEqualTo("ENFP");
        verify(accountRepository).findByIdAndIsDeletedFalse(userId);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 조회 시 예외가 발생한다")
    void getUserProfile_NonExistentUser_ThrowsException() {
        // Given
        Long userId = 999L;
        given(accountRepository.findByIdAndIsDeletedFalse(userId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserProfile(userId))
                .isInstanceOf(UserNotFoundException.class);
        
        verify(accountRepository).findByIdAndIsDeletedFalse(userId);
    }

    @Test
    @DisplayName("전체 사용자 프로필 목록을 조회할 수 있다")
    void getAllUserProfile_MultipleUsers_ReturnsUserList() {
        // Given
        List<Account> accounts = List.of(
                createTestAccount(),
                createAnotherTestAccount()
        );
        given(accountRepository.findAllByIsDeletedFalse()).willReturn(accounts);

        // When
        List<UserProfileResponse> result = userService.getAllUserProfile();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("테스트 사용자");
        assertThat(result.get(1).getName()).isEqualTo("다른 사용자");
        verify(accountRepository).findAllByIsDeletedFalse();
    }

    @Test
    @DisplayName("MBTI 기반으로 추천 팀원을 조회할 수 있다")
    void getRecommendedTeamMembers_ValidUserId_ReturnsRecommendedUsers() {
        // Given
        Long userId = 1L;
        Account currentUser = createTestAccount();
        List<Account> recommendedUsers = List.of(createAnotherTestAccount());

        given(accountRepository.findByIdAndIsDeletedFalse(userId)).willReturn(Optional.of(currentUser));
        given(accountRepository.findRecommendedTeamMembers("ENFP", "적극적", userId))
                .willReturn(recommendedUsers);

        // When
        List<UserProfileResponse> result = userService.getRecommendedTeamMembers(userId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("다른 사용자");
        verify(accountRepository).findByIdAndIsDeletedFalse(userId);
        verify(accountRepository).findRecommendedTeamMembers("ENFP", "적극적", userId);
    }

    @Test
    @DisplayName("사용자 등록을 할 수 있다")
    void registerUser_ValidRequest_RegistersSuccessfully() {
        // Given
        SignupRequest request = createSignupRequest();
        
        // 유효성 검사 Mock 설정
        given(validationService.isValidEmail(request.getEmail())).willReturn(true);
        given(validationService.isValidPassword(request.getPassword())).willReturn(true);
        
        // 중복 검사 Mock 설정
        given(accountRepository.existsByEmailAndDeletedAtIsNull(request.getEmail())).willReturn(false);
        given(accountRepository.findWithdrawnAccountByEmail(request.getEmail())).willReturn(Optional.empty());
        
        // 이메일 인증 Mock 설정
        given(emailService.isEmailVerified(request.getEmail())).willReturn(true);
        
        // 비밀번호 암호화 Mock 설정
        given(authService.encryptPassword(request.getPassword())).willReturn("encodedPassword");
        
        // 계정 저장 Mock 설정
        given(accountRepository.save(any(Account.class))).willReturn(createTestAccount());
        
        // defaultGracePeriodDays 필드 설정
        ReflectionTestUtils.setField(userService, "defaultGracePeriodDays", 30);

        // When
        userService.registerUser(request);

        // Then
        verify(validationService).isValidEmail(request.getEmail());
        verify(validationService).isValidPassword(request.getPassword());
        verify(accountRepository).existsByEmailAndDeletedAtIsNull(request.getEmail());
        verify(emailService).isEmailVerified(request.getEmail());
        verify(authService).encryptPassword(request.getPassword());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    @DisplayName("이미 존재하는 이메일로 등록 시 예외가 발생한다")
    void registerUser_DuplicateEmail_ThrowsException() {
        // Given
        SignupRequest request = createSignupRequest();
        
        given(validationService.isValidEmail(request.getEmail())).willReturn(true);
        given(validationService.isValidPassword(request.getPassword())).willReturn(true);
        given(accountRepository.findWithdrawnAccountByEmail(request.getEmail())).willReturn(Optional.empty());
        given(emailService.isEmailVerified(request.getEmail())).willReturn(true);
        given(accountRepository.existsByEmailAndDeletedAtIsNull(request.getEmail())).willReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.registerUser(request))
                .isInstanceOf(DuplicateEmailException.class);
        
        verify(accountRepository).existsByEmailAndDeletedAtIsNull(request.getEmail());
    }

    @Test
    @DisplayName("무효한 이메일로 등록 시 예외가 발생한다")
    void registerUser_InvalidEmail_ThrowsException() {
        // Given
        SignupRequest request = createSignupRequest();
        request.setEmail("invalid-email");
        
        given(validationService.isValidEmail(request.getEmail())).willReturn(false);

        // When & Then
        assertThatThrownBy(() -> userService.registerUser(request))
                .isInstanceOf(ValidationException.class);
        
        verify(validationService).isValidEmail(request.getEmail());
    }

    @Test
    @DisplayName("프로필을 수정할 수 있다")
    void updateMyProfile_ValidRequest_UpdatesSuccessfully() {
        // Given
        Long userId = 1L;
        UserProfileUpdateRequest request = createProfileUpdateRequest();
        Account account = createTestAccount();

        given(accountRepository.findByIdAndIsDeletedFalse(userId)).willReturn(Optional.of(account));
        given(validationService.isValidName(request.getName())).willReturn(true);
        given(validationService.isValidAge(request.getAge())).willReturn(true);
        given(validationService.isValidMbti(request.getMbti())).willReturn(true);
        given(accountRepository.save(any(Account.class))).willReturn(account);

        // When
        userService.updateMyProfile(userId, request);

        // Then
        assertThat(account.getName()).isEqualTo("수정된 이름");
        assertThat(account.getAge()).isEqualTo(25);
        assertThat(account.getMbti()).isEqualTo("INTJ");
        verify(accountRepository).findByIdAndIsDeletedFalse(userId);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    @DisplayName("계정을 삭제할 수 있다")
    void deleteAccount_ValidUserId_DeletesSuccessfully() {
        // Given
        Long userId = 1L;
        Account account = createTestAccount();

        given(accountRepository.findByIdAndIsDeletedFalse(userId)).willReturn(Optional.of(account));

        // defaultGracePeriodDays 필드 설정
        ReflectionTestUtils.setField(userService, "defaultGracePeriodDays", 30);

        // When
        userService.deleteAccount(userId);

        // Then
        assertThat(account.getDeletedAt()).isNotNull();
        assertThat(account.getPermanentDeletionDate()).isNotNull();

        long daysBetween = java.time.Duration.between(account.getDeletedAt(), account.getPermanentDeletionDate()).toDays();
        assertThat(daysBetween).isEqualTo(30);

        verify(accountRepository).findByIdAndIsDeletedFalse(userId);
        verify(accountRepository).save(account);
    }

    @Test
    @DisplayName("이메일 인증을 요청할 수 있다")
    void requestEmailVerification_ValidEmail_SendsVerificationCode() {
        // Given
        String email = "test@gmail.com";
        String verificationCode = "123456";
        
        given(validationService.isValidEmail(email)).willReturn(true);
        given(emailService.generateVerificationCode()).willReturn(verificationCode);

        // When
        userService.requestEmailVerification(email);

        // Then
        verify(validationService).isValidEmail(email);
        verify(emailService).generateVerificationCode();
        verify(emailService).storeVerificationCode(email, verificationCode);
        verify(emailService).sendVerificationEmail(email, verificationCode);
    }

    @Test
    @DisplayName("이메일 인증을 확인할 수 있다")
    void verifyEmail_ValidCode_ReturnsTrue() {
        // Given
        String email = "test@gmail.com";
        String verificationCode = "123456";
        
        given(emailService.verifyCode(email, verificationCode)).willReturn(true);

        // When
        boolean result = userService.verifyEmail(email, verificationCode);

        // Then
        assertThat(result).isTrue();
        verify(emailService).verifyCode(email, verificationCode);
    }

    private Account createTestAccount() {
        return Account.builder()
                .id(1L)
                .email("test@gmail.com")
                .password("encodedPassword")
                .name("테스트 사용자")
                .age(30)
                .mbti("ENFP")
                .disposition("적극적")
                .introduction("안녕하세요")
                .role(UserRole.USER)
                .build();
    }

    private Account createAnotherTestAccount() {
        return Account.builder()
                .id(2L)
                .email("another@gmail.com")
                .password("encodedPassword")
                .name("다른 사용자")
                .age(25)
                .mbti("ENFP")
                .disposition("협력적")
                .introduction("반갑습니다")
                .role(UserRole.USER)
                .build();
    }

    private SignupRequest createSignupRequest() {
        SignupRequest request = new SignupRequest();
        request.setEmail("newuser@gmail.com");
        request.setPassword("NewPassword123!");
        request.setConfirmPassword("NewPassword123!");
        return request;
    }

    private UserProfileUpdateRequest createProfileUpdateRequest() {
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setName("수정된 이름");
        request.setAge(25);
        request.setMbti("INTJ");
        request.setDisposition("신중함");
        request.setIntroduction("새로운 자기소개");
        return request;
    }
}
