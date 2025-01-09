package com.uijae.cms.service.customer;

import com.uijae.cms.domain.SignUpForm;
import com.uijae.cms.domain.model.Customer;
import com.uijae.cms.domain.repository.CustomerRepository;
import com.uijae.cms.exception.CustomException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

import static com.uijae.cms.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class SignUpCustomerService {
    private final CustomerRepository customerRepository;

    public Customer signUp(SignUpForm form) {
        return customerRepository.save(Customer.from(form));
    }

    public boolean isEmailExist(String email) {
        return customerRepository.findByEmail(
                email.toLowerCase(Locale.ROOT)).isPresent();
    }

    @Transactional
    public void verifyEmail(String email, String code) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(NOT_FOUND_USER));

//        // 인증 만료 시간을 현재 시간 + 10분으로 설정
//        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(10);
//        customer.setVerifyExpiredAt(expirationTime);

        if (customer.isVerify()) {
            throw new CustomException(ALREADY_VERIFY);
        } else if(!customer.getVerificationCode().equals(code)) {
            throw new CustomException(WRONG_VERIFICATION);
        } else if (customer.getVerifyExpiredAt().isBefore(LocalDateTime.now())) {
            throw new CustomException(EXPIRE_CODE);
        }

        customer.setVerify(true);
    }

    @Transactional
    public LocalDateTime changeCustomerValidateEmail(Long customerId, String verificationCode) {
        Optional<Customer> c = customerRepository.findById(customerId);

        if (c.isPresent()) {
            Customer cust = c.get();
            cust.setVerificationCode(verificationCode);
            cust.setVerifyExpiredAt(LocalDateTime.now().plusMinutes(15));
            return cust.getVerifyExpiredAt();
        }
        // TODO : 예외 처리
        throw new CustomException(NOT_FOUND_USER);
    }
}
