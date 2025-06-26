package com.pickteam.service.user;

import org.springframework.stereotype.Service;
import java.util.regex.Pattern;

@Service
public class ValidationServiceImpl implements ValidationService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private static final Pattern PASSWORD_PATTERN = Pattern
            .compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");

    private static final Pattern NAME_PATTERN = Pattern.compile("^[가-힣a-zA-Z\\s]{2,50}$");

    private static final String[] VALID_MBTI = {
            "INTJ", "INTP", "ENTJ", "ENTP", "INFJ", "INFP", "ENFJ", "ENFP",
            "ISTJ", "ISFJ", "ESTJ", "ESFJ", "ISTP", "ISFP", "ESTP", "ESFP"
    };

    @Override
    public boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    @Override
    public boolean isValidPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    @Override
    public boolean isValidName(String name) {
        return name != null && NAME_PATTERN.matcher(name).matches();
    }

    @Override
    public boolean isValidAge(Integer age) {
        return age != null && age >= 14 && age <= 100;
    }

    @Override
    public boolean isValidMbti(String mbti) {
        if (mbti == null)
            return false;

        for (String validMbti : VALID_MBTI) {
            if (validMbti.equalsIgnoreCase(mbti)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public PasswordStrength getPasswordStrength(String password) {
        if (password == null || password.length() < 6) {
            return PasswordStrength.WEAK;
        }

        int score = 0;

        // 길이 체크
        if (password.length() >= 8)
            score++;
        if (password.length() >= 12)
            score++;

        // 문자 종류 체크
        if (password.matches(".*[a-z].*"))
            score++;
        if (password.matches(".*[A-Z].*"))
            score++;
        if (password.matches(".*[0-9].*"))
            score++;
        if (password.matches(".*[@#$%^&+=!].*"))
            score++;

        return switch (score) {
            case 0, 1, 2 -> PasswordStrength.WEAK;
            case 3, 4 -> PasswordStrength.MEDIUM;
            case 5 -> PasswordStrength.STRONG;
            default -> PasswordStrength.VERY_STRONG;
        };
    }
}
