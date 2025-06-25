package com.pickteam.service.user;

import com.pickteam.dto.user.*;
import com.pickteam.repository.user.AccountRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final AccountRepository accountRepository;

    @Override
    public void registerUser(UserRegisterRequest request) {
        // TODO : 구현 예정
    }

    @Override
    public boolean checkDuplicateId(String email) {
        // TODO : 구현 예정
        return false;
    }

    @Override
    public boolean validatePassword(String password) {
        // TODO : 구현 예정
        return false;
    }

    @Override
    public void requestEmailVerification(String email) {
        // TODO : 구현 예정
    }

    @Override
    public boolean verifyEmail(String email, String verificationCode) {
        // TODO : 구현 예정
        return false;
    }

    @Override
    public UserProfileResponse login(UserLoginRequest request) {
        // TODO : 구현 예정
        return null;
    }

    @Override
    public UserProfileResponse getMyProfile(Long userId) {
        // TODO : 구현 예정
        return null;
    }

    @Override
    public void updateMyProfile(Long userId, UserProfileUpdateRequest request) {
        // TODO : 구현 예정
    }

    @Override
    public UserProfileResponse getUserProfile(Long userId) {
        // TODO : 구현 예정
        return null;
    }

    @Override
    public List<UserProfileResponse> getAllUserProfile() {
        // TODO : 구현 예정
        return null;
    }

    @Override
    public List<UserProfileResponse> getRecommendedTeamMembers(Long userId) {
        // TODO : 구현 예정
        return null;
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        // TODO : 구현 예정
    }

    @Override
    public void deleteAccount(Long userId) {
        // TODO : 구현 예정
    }
}
