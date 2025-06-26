package com.pickteam.service.user;

public interface EmailService {

    // 이메일 인증 코드 발송
    void sendVerificationEmail(String email, String verificationCode);

    // 인증 코드 생성
    String generateVerificationCode();

    // 인증 코드 저장 (MySQL DB 저장)
    void storeVerificationCode(String email, String code);

    // 인증 코드 검증
    boolean verifyCode(String email, String code);

    // 비밀번호 재설정 메일 발송
    void sendPasswordResetEmail(String email, String resetToken);
}
