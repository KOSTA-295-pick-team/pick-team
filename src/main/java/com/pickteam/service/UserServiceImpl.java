package com.pickteam.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final AccountRepository accountRepository;

    @Override
    public void registerUser(UserRegisterRequest request) {
        // TODO : 구현 예정
    }

}
