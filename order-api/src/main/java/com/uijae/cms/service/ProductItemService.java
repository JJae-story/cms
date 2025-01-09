package com.uijae.cms.service;

import com.uijae.cms.domain.model.Product;
import com.uijae.cms.domain.model.ProductItem;
import com.uijae.cms.domain.product.AddProductItemForm;
import com.uijae.cms.domain.repository.ProductItemRepository;
import com.uijae.cms.domain.repository.ProductRepository;
import com.uijae.cms.exception.CustomException;
import com.uijae.cms.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.uijae.cms.exception.ErrorCode.NOT_FOUND_PRODUCT;
import static com.uijae.cms.exception.ErrorCode.SAME_ITEM_NAME;

@Service
@RequiredArgsConstructor
public class ProductItemService {
    private final ProductItemRepository productItemRepository;
    private final ProductRepository productRepository;

    @Transactional
    public Product addProductItem(Long sellerId, AddProductItemForm form) {
        Product product = productRepository.findBySellerIdAndId(sellerId, form.getProductId())
                .orElseThrow(() -> new CustomException(NOT_FOUND_PRODUCT));

        if (product.getProductItems().stream().anyMatch(
                item -> item.getName().equals(form.getName()))) {
            throw new CustomException(SAME_ITEM_NAME);
        }

        ProductItem productItem = ProductItem.of(sellerId, form);
        product.getProductItems().add(productItem);

        return product;
    }
}
