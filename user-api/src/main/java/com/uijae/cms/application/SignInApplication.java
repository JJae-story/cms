package com.uijae.cms.application;

import com.uijae.cms.domain.SignInForm;
import com.uijae.cms.domain.common.UserType;
import com.uijae.cms.domain.config.JwtAuthenticationProvider;
import com.uijae.cms.domain.model.Customer;
import com.uijae.cms.domain.model.Seller;
import com.uijae.cms.exception.CustomException;
import com.uijae.cms.service.customer.CustomerService;
import com.uijae.cms.service.seller.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.uijae.cms.exception.ErrorCode.LOGIN_CHECK_FAIL;

@Service
@RequiredArgsConstructor
public class SignInApplication {
    private final CustomerService customerService;
    private final JwtAuthenticationProvider provider;
    private final SellerService sellerService;

    public String customerLogInToken(SignInForm form) {
        // 1. 로그인 가능 여부
        Customer c = customerService.findValidCustomer(
                form.getEmail(), form.getPassword())
                .orElseThrow(() -> new CustomException(LOGIN_CHECK_FAIL));
        // 2. 토큰을 발행
        // 3. 토큰을 response 한다.
        return provider.createToken(c.getEmail(), c.getId(), UserType.CUSTOMER);
    }

    public String sellerLogInToken(SignInForm form) {
        // 1. 로그인 가능 여부
        Seller s = sellerService.findValidSeller(
                        form.getEmail(), form.getPassword())
                .orElseThrow(() -> new CustomException(LOGIN_CHECK_FAIL));
        // 2. 토큰을 발행
        // 3. 토큰을 response 한다.
        return provider.createToken(s.getEmail(), s.getId(), UserType.SELLER);
    }
}
