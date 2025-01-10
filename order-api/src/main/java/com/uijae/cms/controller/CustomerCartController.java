package com.uijae.cms.controller;

import com.uijae.cms.application.CartApplication;
import com.uijae.cms.domain.config.JwtAuthenticationProvider;
import com.uijae.cms.domain.product.AddProductCartForm;
import com.uijae.cms.domain.redis.Cart;
import com.uijae.cms.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customer/cart")
@RequiredArgsConstructor
public class CustomerCartController {
    private final CartApplication cartApplication;
    private final JwtAuthenticationProvider provider;

    @PostMapping
    public ResponseEntity<Cart> addCart(
            @RequestHeader(name = "X-AUTH-TOKEN") String token,
            @RequestBody AddProductCartForm form) {
        return ResponseEntity.ok(cartApplication.addCart(provider.getUserVo(token).getId(), form));
    }
}
