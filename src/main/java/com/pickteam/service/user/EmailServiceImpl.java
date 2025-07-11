package com.pickteam.service.user;

import com.pickteam.domain.user.EmailVerification;
import com.pickteam.exception.email.EmailSendException;
import com.pickteam.exception.user.AccountWithdrawalException;
import com.pickteam.constants.EmailErrorMessages;
import com.pickteam.constants.UserErrorMessages;
import com.pickteam.repository.user.EmailVerificationRepository;
import com.pickteam.repository.user.AccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.security.SecureRandom;

/**
 * 이메일 서비스 구현체
 * - 회원가입 시 이메일 인증을 위한 메일 발송
 * - 인증 코드 생성, 저장, 검증 기능 제공
 * - HTML 형태의 이메일 컨텐츠 생성
 * - MySQL DB를 통한 인증 코드 영구 저장 및 만료 관리
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final EmailVerificationRepository emailVerificationRepository;
    private final AccountRepository accountRepository;

    /** 이메일 발신자 주소 */
    @Value("${app.mail.from}")
    private String fromEmail;

    /** 이메일 발신자 이름 */
    @Value("${app.mail.from.name}")
    private String fromName;

    /** 인증 코드 유효 시간 (5분) */
    private static final int VERIFICATION_CODE_EXPIRY_MINUTES = 5;

    /** 인증 코드 생성용 보안 랜덤 객체 (스레드 안전) */
    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * 이메일 인증 코드 발송
     * - HTML 형태의 인증 이메일 생성 및 발송
     * - 이메일 발송 성공/실패 로깅
     * 
     * @param email            인증 메일을 받을 이메일 주소
     * @param verificationCode 발송할 6자리 인증 코드
     * @throws EmailSendException 이메일 발송 실패 시
     */
    @Override
    public void sendVerificationEmail(String email, String verificationCode) {
        log.info("인증 메일 발송 시작: {}", email);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, fromName);
            helper.setTo(email);
            helper.setSubject(EmailErrorMessages.EMAIL_SUBJECT);
            helper.setText(createVerificationEmailContent(verificationCode), true);
            mailSender.send(message);
            log.info("인증 메일 발송 완료: {}", email);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("인증 메일 발송 실패: {}", email, e);
            throw new EmailSendException(EmailErrorMessages.EMAIL_SEND_FAILED, e);
        }
    }

    /**
     * 6자리 랜덤 인증 코드 생성
     * - 000000 ~ 999999 범위의 6자리 숫자 코드 생성
     * - 앞자리가 0인 경우도 6자리로 포맷팅
     * - SecureRandom 사용으로 보안성 강화 및 동일 코드 생성 방지
     * 
     * @return 6자리 숫자 문자열 인증 코드
     */
    @Override
    public String generateVerificationCode() {
        String code = String.format("%06d", secureRandom.nextInt(1000000));
        log.debug("인증 코드 생성 완료");
        return code;
    }

    /**
     * 인증 코드를 데이터베이스에 저장
     * - 기존 미인증 코드가 있으면 삭제 후 새 코드 저장
     * - 5분 만료시간 설정으로 보안 강화
     * 
     * @param email 인증 코드와 연결할 이메일 주소
     * @param code  저장할 인증 코드
     */
    @Override
    public void storeVerificationCode(String email, String code) {
        log.info("인증 코드 저장 시작: {}", email);

        // 탈퇴 계정 검증 (유예 기간 중인 계정 확인)
        accountRepository.findWithdrawnAccountByEmail(email)
                .ifPresent(withdrawnAccount -> {
                    throw new AccountWithdrawalException(
                            UserErrorMessages.EMAIL_VERIFICATION_BLOCKED_WITHDRAWAL,
                            withdrawnAccount.getPermanentDeletionDate());
                });

        // 기존 미인증 코드 삭제
        emailVerificationRepository.deleteByEmail(email);

        // 새 인증 코드 저장
        EmailVerification emailVerification = EmailVerification.builder()
                .email(email)
                .verificationCode(code)
                .expiresAt(LocalDateTime.now().plusMinutes(VERIFICATION_CODE_EXPIRY_MINUTES))
                .isVerified(false)
                .build();

        emailVerificationRepository.save(emailVerification);
        log.info("인증 코드 DB 저장 완료: {}", email);
    }

    /**
     * 이메일 인증 코드 검증
     * - DB에서 유효한 인증 코드 조회 및 검증
     * - 만료시간 자동 확인으로 보안 강화
     * - 인증 성공 시 인증 상태 업데이트
     * 
     * @param email 인증할 이메일 주소
     * @param code  사용자가 입력한 인증 코드
     * @return 인증 성공 시 true, 실패 시 false
     */
    @Override
    public boolean verifyCode(String email, String code) {
        log.info("이메일 인증 시도: {}", email);

        return emailVerificationRepository
                .findByEmailAndVerificationCodeAndIsVerifiedFalse(email, code)
                .map(verification -> {
                    if (verification.isExpired()) {
                        log.warn("{}에 대한 {}", EmailErrorMessages.VERIFICATION_CODE_EXPIRED, email);
                        return false;
                    }

                    // 인증 성공 처리
                    verification.setIsVerified(true);
                    emailVerificationRepository.save(verification);
                    log.info("{}: {}", EmailErrorMessages.EMAIL_VERIFICATION_SUCCESS, email);
                    return true;
                })
                .orElseGet(() -> {
                    log.warn("잘못된 인증 코드 사용 시도: {}", email);
                    return false;
                });
    }

    /**
     * 이메일 인증 완료 여부 확인
     * - 해당 이메일 주소의 인증 완료 상태를 조회
     * - 회원가입 시 이메일 인증 검증에 사용
     * 
     * @param email 인증 상태를 확인할 이메일 주소
     * @return 인증이 완료된 경우 true, 미완료 시 false
     */
    @Override
    public boolean isEmailVerified(String email) {
        boolean isVerified = emailVerificationRepository
                .findTopByEmailOrderByCreatedAtDesc(email)
                .map(EmailVerification::getIsVerified)
                .orElse(false); // 인증 기록이 없으면 미인증으로 처리

        log.debug("이메일 인증 상태 확인: {} -> {}", email, isVerified);
        return isVerified;
    }

    /**
     * 비밀번호 재설정 이메일 발송
     * - 비밀번호 찾기 기능을 위한 이메일 발송
     * - 기존 이메일 인증과 동일한 SMTP 설정 사용
     * 
     * @param email     비밀번호 재설정 메일을 받을 이메일 주소
     * @param resetCode 비밀번호 재설정용 6자리 코드
     */
    @Override
    public void sendPasswordResetEmail(String email, String resetCode) {
        log.info("비밀번호 재설정 메일 발송 시작: {}", email);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, fromName);
            helper.setTo(email);
            helper.setSubject("Pick Team 비밀번호 재설정");
            helper.setText(createPasswordResetEmailContent(resetCode), true);
            mailSender.send(message);
            log.info("비밀번호 재설정 메일 발송 완료: {}", email);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("비밀번호 재설정 메일 발송 실패: {}", email, e);
            throw new EmailSendException("비밀번호 재설정 메일 발송에 실패했습니다.", e);
        }
    }

    /**
     * 이메일 인증 HTML 컨텐츠 생성
     * - Pick Team 브랜딩이 적용된 HTML 이메일 템플릿
     * - 인증 코드가 강조 표시된 사용자 친화적 디자인
     * - 5분 유효시간 안내 포함
     * 
     * @param code 이메일에 포함할 인증 코드
     * @return HTML 형태의 이메일 컨텐츠
     */
    private String createVerificationEmailContent(String code) {
        return String.format(
                "<h2>Pick Team 이메일 인증</h2>" +
                        "<p>안녕하세요! Pick Team 서비스 이용을 위해 이메일 인증을 완료해주세요.</p>" +
                        "<h3>인증 코드: <strong style='color: #007bff; font-size: 24px;'>%s</strong></h3>" +
                        "<p>위 인증 코드를 입력하여 이메일 인증을 완료해주세요.</p>" +
                        "<p><strong>인증 코드는 5분간 유효합니다.</strong></p>" +
                        "<p>만약 이메일 인증을 요청하지 않으셨다면, 이 이메일을 무시하시기 바랍니다.</p>" +
                        "<hr>" +
                        "<p style='color: #666; font-size: 12px;'>Pick Team 서비스</p>",
                code);
    }

    /**
     * 비밀번호 재설정 이메일 HTML 컨텐츠 생성
     * - Pick Team 브랜딩이 적용된 HTML 이메일 템플릿
     * - 재설정 코드가 강조 표시된 사용자 친화적 디자인
     * - 5분 유효시간 안내 포함
     * 
     * @param resetCode 이메일에 포함할 비밀번호 재설정 코드
     * @return HTML 형태의 이메일 컨텐츠
     */
    private String createPasswordResetEmailContent(String resetCode) {
        return String.format(
                "<h2>Pick Team 비밀번호 재설정</h2>" +
                        "<p>안녕하세요! Pick Team 계정의 비밀번호 재설정을 요청하셨습니다.</p>" +
                        "<h3>재설정 코드: <strong style='color: #007bff; font-size: 24px;'>%s</strong></h3>" +
                        "<p>위 재설정 코드를 입력하여 새로운 비밀번호를 설정해주세요.</p>" +
                        "<p><strong>재설정 코드는 5분간 유효합니다.</strong></p>" +
                        "<p>만약 비밀번호 재설정을 요청하지 않으셨다면, 이 이메일을 무시하시기 바랍니다.</p>" +
                        "<hr>" +
                        "<p style='color: #666; font-size: 12px;'>Pick Team 서비스</p>",
                resetCode);
    }
}