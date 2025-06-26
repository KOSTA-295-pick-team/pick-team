package com.pickteam.service.user;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    // 임시 저장소 (실제로는 Redis나 DB 사용 권장)
    private final Map<String, String> verificationCodeStore = new ConcurrentHashMap<>();

    @Override
    public void sendVerificationEmail(String email, String verificationCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("Pick Team 이메일 인증");
            helper.setText(createVerificationEmailContent(verificationCode), true);

            mailSender.send(message);
            log.info("인증 메일 발송 완료: {}", email);

        } catch (MessagingException e) {
            log.error("인증 메일 발송 실패: {}", email, e);
            throw new RuntimeException("이메일 발송에 실패했습니다.", e);
        }
    }

    @Override
    public String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    @Override
    public void storeVerificationCode(String email, String code) {
        verificationCodeStore.put(email, code);
        // TODO: 만료시간 설정 (5분 등)
    }

    @Override
    public boolean verifyCode(String email, String code) {
        String storedCode = verificationCodeStore.get(email);
        if (storedCode != null && storedCode.equals(code)) {
            verificationCodeStore.remove(email); // 인증 후 코드 삭제
            return true;
        }
        return false;
    }

    @Override
    public void sendPasswordResetEmail(String email, String resetToken) {
        // TODO: 구현 예정
    }

    private String createVerificationEmailContent(String code) {
        return String.format(
                "<h2>Pick Team 이메일 인증</h2>" +
                        "<p>안녕하세요! Pick Team 서비스 이용을 위해 이메일 인증을 완료해주세요.</p>" +
                        "<h3>인증 코드: <strong>%s</strong></h3>" +
                        "<p>위 인증 코드를 입력하여 이메일 인증을 완료해주세요.</p>" +
                        "<p>인증 코드는 5분간 유효합니다.</p>",
                code);
    }
}
