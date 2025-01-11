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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

        if (!addAble(cart, product, form)) {
            throw new CustomException(PRODUCT_COUNT_INSUFFICIENT);
        }

        return cartService.addCart(customerId, form);
    }

    public Cart updateCart(Long customerId, Cart cart) {
        cartService.putCart(customerId, cart);
        return getCart(customerId);
    }

    public Cart getCart(Long customerId) {
        Cart cart = refreshCart(cartService.getCart(customerId));
        cartService.putCart(cart.getCustomerId(), cart);

        Cart returnCart = new Cart();
        returnCart.setCustomerId(customerId);
        returnCart.setProducts(cart.getProducts());
        returnCart.setMessages(cart.getMessages());

        cart.setMessages(new ArrayList<>());

        cartService.putCart(customerId, cart);

        return returnCart;
    }

    public void clearCart(Long customerId) {
        cartService.putCart(customerId, null);
    }

    protected Cart refreshCart(Cart cart) {
        // 1. 상품 or 상품 아이템의 정보, 가격, 수량이 변경되었는지 체크 후 그에 맞는 메시지 제공
        // 2. 상품 수량, 가격을 임의로 변경
        Map<Long, Product> productMap = productSearchService.getListByProductIds(
                cart.getProducts().stream().map(Cart.Product::getId).toList())
                .stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        for (int i = 0; i < cart.getProducts().size(); i++) {
            Cart.Product cartProduct = cart.getProducts().get(i);

            Product p = productMap.get(cartProduct.getId());

            if (p == null) {
                cart.getProducts().remove(cartProduct);
                i--;
                cart.addMessage(cartProduct.getName() + "상품이 삭제되었습니다.");
                continue;
            }

            Map<Long, ProductItem> productItemMap = p.getProductItems().stream()
                    .collect(Collectors.toMap(ProductItem::getId, productItem -> productItem));

            List<String> tmpMessages = new ArrayList<>();

            for (int j = 0; j < cartProduct.getItems().size(); j++) {
                Cart.ProductItem item = cartProduct.getItems().get(i);

                ProductItem pi = productItemMap.get(item.getId());

                if (pi == null) {
                    cartProduct.getItems().remove(item);
                    j--;
                    tmpMessages.add(item.getName() + "옵션이 삭제되었습니다.");
                    continue;
                }

                boolean isPriceChanged = false, isCountNotEnough = false;

                if (!item.getPrice().equals(pi.getPrice())) {
                    isPriceChanged = true;
                    item.setPrice(pi.getPrice());
                }
                if (item.getCount() > pi.getCount()) {
                    isCountNotEnough = true;
                    item.setCount(pi.getCount());
                }

                if (isPriceChanged && isCountNotEnough) {
                    // error message 1
                    tmpMessages.add(item.getName() + "가격 변동, 수량이 부족하여 구매 가능한 최대치로 변경되었습니다.");
                } else if (isPriceChanged) {
                    // error message 2
                    tmpMessages.add(item.getName() + "가격이 변동되었습니다.");
                } else if (isCountNotEnough) {
                    // error message 3
                    tmpMessages.add(item.getName() + "수량이 부족하여 구매 가능한 최대치로 변경되었습니다.");
                }
            }
            if (cartProduct.getItems().size() == 0) {
                cart.getProducts().remove(cartProduct);
                i--;
                cart.addMessage(cartProduct.getName() + "상품의 옵션이 모두 없어져 구매가 불가능합니다.");
            } else if (tmpMessages.size() > 0) {
                StringBuilder sb = new StringBuilder();

                sb.append(cartProduct.getName() + " 상품의 변동 사항 : ");
                for (String message : tmpMessages) {
                    sb.append(message);
                    sb.append(", ");
                }
                cart.addMessage(sb.toString());
            }
        }
        return cart;
    }

    private boolean addAble(Cart cart, Product product, AddProductCartForm form) {
        Cart.Product cartProduct = cart.getProducts().stream().filter(
                        p -> p.getId().equals(form.getId())).findFirst()
                .orElse(Cart.Product.builder().id(product.getId()).items(Collections.emptyList()).build());
//                .orElseThrow(() -> new CustomException(NOT_FOUND_PRODUCT));

        Map<Long, Integer> cartItemCountMap = cartProduct.getItems().stream()
                .collect(Collectors.toMap(Cart.ProductItem::getId, Cart.ProductItem::getCount));

        Map<Long, Integer> currentItemCountMap = product.getProductItems().stream()
                .collect(Collectors.toMap(ProductItem::getId, ProductItem::getCount));


        return form.getItems().stream().noneMatch(formItem -> {
            Integer cartCount = cartItemCountMap.get(cartProduct.getId());

            if (cartCount == null) {
                cartCount = 0;
            }

            Integer currentCount = currentItemCountMap.get(cartProduct.getId());
            return formItem.getCount() + cartCount > currentCount;
        });
    }
}
