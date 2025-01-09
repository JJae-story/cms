package com.uijae.cms.service;

import com.uijae.cms.domain.model.Product;
import com.uijae.cms.domain.model.ProductItem;
import com.uijae.cms.domain.product.AddProductForm;
import com.uijae.cms.domain.product.UpdateProductForm;
import com.uijae.cms.domain.product.UpdateProductItemForm;
import com.uijae.cms.domain.repository.ProductRepository;
import com.uijae.cms.exception.CustomException;
import com.uijae.cms.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.uijae.cms.exception.ErrorCode.NOT_FOUND_ITEM;
import static com.uijae.cms.exception.ErrorCode.NOT_FOUND_PRODUCT;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    @Transactional
    public Product addProduct(Long sellerId, AddProductForm form) {
        return productRepository.save(Product.of(sellerId, form));
    }

    @Transactional
    public Product updateProduct(Long sellerId, UpdateProductForm form) {
        Product product = productRepository.findBySellerIdAndId(sellerId, form.getId())
                .orElseThrow(() -> new CustomException(NOT_FOUND_PRODUCT));

        product.setName(form.getName());
        product.setDescription(form.getDescription());

        for (UpdateProductItemForm itemForm : form.getItems()) {
            ProductItem item = product.getProductItems().stream()
                    .filter(pi -> pi.getId().equals(itemForm.getId()))
                    .findFirst().orElseThrow(() -> new CustomException(NOT_FOUND_ITEM));

            item.setName(itemForm.getName());
            item.setPrice(itemForm.getPrice());
            item.setCount(itemForm.getCount());
        }
        return product;
    }
}
