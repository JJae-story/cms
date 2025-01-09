package com.uijae.cms.application;

import com.uijae.cms.client.MailgunClient;
import com.uijae.cms.client.mailgun.SendMailForm;
import com.uijae.cms.domain.SignUpForm;
import com.uijae.cms.domain.common.UserType;
import com.uijae.cms.domain.config.JwtAuthenticationProvider;
import com.uijae.cms.domain.model.Customer;
import com.uijae.cms.domain.model.Seller;
import com.uijae.cms.exception.CustomException;
import com.uijae.cms.service.customer.SignUpCustomerService;
import com.uijae.cms.service.seller.SignUpSellerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import static com.uijae.cms.exception.ErrorCode.ALREADY_REGISTER_USER;

@Service
@Slf4j
@RequiredArgsConstructor
public class SignUpApplication {
    private final MailgunClient mailgunClient;
    private final SignUpCustomerService signUpCustomerService;
    private final SignUpSellerService signUpSellerService;
    private final JwtAuthenticationProvider provider;

    public void customerVerify(String email, String code) {
        signUpCustomerService.verifyEmail(email, code);
    }

    public String customerSignUp(SignUpForm form) {
        if (signUpCustomerService.isEmailExist(form.getEmail())) {
            // exception
            throw new CustomException(ALREADY_REGISTER_USER);
        } else {
            Customer c = signUpCustomerService.signUp(form);

            String code = getRandomCode();

            SendMailForm build = SendMailForm.builder()
                    .from("jjae@sandbox8088465865ad490a9095c5a14e6ccc5e.mailgun.org")
                    .to(form.getEmail())
                    .subject("Verification Email")
                    .text(getVerificationEmailBody(c.getEmail(), c.getName(), "customer", code))
                    .build();

            mailgunClient.sendEmail(build);

            // 로그 확인
            log.info("Send email result -> " + mailgunClient.sendEmail(build).getBody());

            signUpCustomerService.changeCustomerValidateEmail(c.getId(), code);
            return "회원 가입이 완료되었습니다.";
        }
    }

    public void sellerVerify(String email, String code) {
        signUpSellerService.verifyEmail(email, code);
    }

    public String sellerSignUp(SignUpForm form) {
        if (signUpSellerService.isEmailExist(form.getEmail())) {
            throw new CustomException(ALREADY_REGISTER_USER);
        } else {
            // 1. 회원가입 처리
            Seller s = signUpSellerService.signUp(form);

            // 2. 인증 코드 생성
            String code = getRandomCode();

            // 3. 이메일 본문 작성
            SendMailForm build = SendMailForm.builder()
                    .from("jjae@sandbox8088465865ad490a9095c5a14e6ccc5e.mailgun.org")
                    .to(form.getEmail())
                    .subject("Verification Email")
                    .text(getVerificationEmailBody(s.getEmail(), s.getName(), "seller", code))
                    .build();

            // 4. 이메일 전송
            mailgunClient.sendEmail(build);
            log.info("Send email result -> " + mailgunClient.sendEmail(build).getBody());

            // 5. 인증 코드, 만료시간을 데이터베이스에 저장
            signUpSellerService.changeSellerValidateEmail(s.getId(), code);

            return "회원 가입이 완료되었습니다.";
        }
    }

    private String getRandomCode() {
        return RandomStringUtils.random(10, true, true);
    }

    private String getVerificationEmailBody(String email, String name, String type, String code) {
        StringBuilder sb = new StringBuilder();
        return sb.append("Hello ").append(name).append("! Please Click Link for verification.\n\n")
                .append("http://localhost:8081/signup/" + type + "/verify?email=")
                .append(email)
                .append("&code=")
                .append(code).toString();
    }
}
