package com.uijae.cms.application;

import com.uijae.cms.domain.model.Product;
import com.uijae.cms.domain.model.ProductItem;
import com.uijae.cms.domain.product.AddProductCartForm;
import com.uijae.cms.domain.redis.Cart;
import com.uijae.cms.exception.CustomException;
import com.uijae.cms.service.CartService;
import com.uijae.cms.service.ProductSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

import static com.uijae.cms.exception.ErrorCode.NOT_FOUND_PRODUCT;
import static com.uijae.cms.exception.ErrorCode.PRODUCT_COUNT_INSUFFICIENT;

@Service
@RequiredArgsConstructor
public class CartApplication {
    private final ProductSearchService productSearchService;
    private final CartService cartService;

    public Cart addCart(Long customerId, AddProductCartForm form) {
        Product product = productSearchService.getByProductId(form.getId());

        if (product == null) {
            throw new CustomException(NOT_FOUND_PRODUCT);
        }

        Cart cart = cartService.getCart(customerId);

        if (cart != null && !addAble(cart, product, form)) {
            throw new CustomException(PRODUCT_COUNT_INSUFFICIENT);
        }

        return cartService.addCart(customerId, form);
    }

    private boolean addAble(Cart cart, Product product, AddProductCartForm form) {
        Cart.Product cartProduct = cart.getProducts().stream().filter(
                        p -> p.getId().equals(form.getId())).findFirst()
                .orElseThrow(() -> new CustomException(NOT_FOUND_PRODUCT));

        Map<Long, Integer> cartItemCountMap = cartProduct.getItems().stream()
                .collect(Collectors.toMap(Cart.ProductItem::getId, Cart.ProductItem::getCount));

        Map<Long, Integer> currentItemCountMap = product.getProductItems().stream()
                .collect(Collectors.toMap(ProductItem::getId, ProductItem::getCount));


        return form.getItems().stream().noneMatch(formItem -> {
            Integer cartCount = cartItemCountMap.get(cartProduct.getId());
            Integer currentCount = currentItemCountMap.get(cartProduct.getId());
            return formItem.getCount() + cartCount > currentCount;
        });
    }
}
