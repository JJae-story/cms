package com.uijae.cms.domain.config;

import com.uijae.cms.domain.common.UserType;
import com.uijae.cms.domain.common.UserVo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtAuthenticationProviderTest {

    public static void main(String[] args) {
        JwtAuthenticationProvider provider = new JwtAuthenticationProvider();

        String userPk = "TestUserPk123";
        Long id = 1L;
        UserType userType = UserType.SELLER;

        // 토큰 생성
        String token = provider.createToken(userPk, id, userType);
        System.out.println("Generated Token: " + token);

        // 토큰 검증
        boolean isValid = provider.validateToken(token);
        System.out.println("Is Token Valid: " + isValid);

        // 사용자 정보 추출
        UserVo userVo = provider.getUserVo(token);
        System.out.println("Extracted UserVo: " + userVo);
    }

}