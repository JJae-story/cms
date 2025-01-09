package com.uijae.cms.controller;

import com.uijae.cms.domain.config.JwtAuthenticationProvider;
import com.uijae.cms.domain.model.Product;
import com.uijae.cms.domain.product.AddProductForm;
import com.uijae.cms.domain.product.AddProductItemForm;
import com.uijae.cms.domain.product.ProductDto;
import com.uijae.cms.domain.product.ProductItemDto;
import com.uijae.cms.service.ProductItemService;
import com.uijae.cms.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/seller/product")
@RequiredArgsConstructor
public class SellerProductController {

    private final ProductService productService;
    private final ProductItemService productItemService;
    private final JwtAuthenticationProvider provider;

    @PostMapping
    public ResponseEntity<ProductDto> addProduct(@RequestHeader(name = "X-AUTH-TOKEN") String token,
                                           @RequestBody AddProductForm form) {

        return ResponseEntity.ok(ProductDto.from(
                productService.addProduct(provider.getUserVo(token)
                        .getId(), form)));
    }

    @PostMapping("/item")
    public ResponseEntity<ProductDto> addProductItem(@RequestHeader(name = "X-AUTH-TOKEN") String token,
                                                     @RequestBody AddProductItemForm form) {

        return ResponseEntity.ok(ProductDto.from(
                productItemService.addProductItem(provider.getUserVo(token)
                        .getId(), form)));
    }
}
