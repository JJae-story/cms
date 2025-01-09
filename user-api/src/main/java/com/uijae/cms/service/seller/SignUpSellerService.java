package com.uijae.cms.service.seller;

import com.uijae.cms.domain.SignUpForm;
import com.uijae.cms.domain.model.Seller;
import com.uijae.cms.domain.repository.SellerRepository;
import com.uijae.cms.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

import static com.uijae.cms.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class SignUpSellerService {

    private final SellerRepository sellerRepository;

    public Seller signUp(SignUpForm form) {
        return sellerRepository.save(Seller.from(form));
    }

    public boolean isEmailExist(String email) {
        return sellerRepository.findByEmail(
                email.toLowerCase(Locale.ROOT)).isPresent();
    }

    @Transactional
    public void verifyEmail(String email, String code) {
        Seller seller = sellerRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(NOT_FOUND_USER));

//        // 인증 만료 시간을 현재 시간 + 10분으로 설정
//        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(10);
//        seller.setVerifyExpiredAt(expirationTime);

        if (seller.isVerify()) {
            throw new CustomException(ALREADY_VERIFY);
        } else if(!seller.getVerificationCode().equals(code)) {
            throw new CustomException(WRONG_VERIFICATION);
        } else if (seller.getVerifyExpiredAt().isBefore(LocalDateTime.now())) {
            throw new CustomException(EXPIRE_CODE);
        }

        seller.setVerify(true);
    }

    @Transactional
    public LocalDateTime changeSellerValidateEmail(Long sellerId, String verificationCode) {
        Optional<Seller> s = sellerRepository.findById(sellerId);

        if (s.isPresent()) {
            Seller sel = s.get();
            sel.setVerificationCode(verificationCode);
            sel.setVerifyExpiredAt(LocalDateTime.now().plusMinutes(15));
            return sel.getVerifyExpiredAt();
        }
        // TODO : 예외 처리
        throw new CustomException(NOT_FOUND_USER);
    }
}
