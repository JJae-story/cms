package com.uijae.cms.application;

import com.uijae.cms.client.UserClient;
import com.uijae.cms.client.user.ChangeBalanceForm;
import com.uijae.cms.client.user.CustomerDto;
import com.uijae.cms.domain.model.ProductItem;
import com.uijae.cms.domain.redis.Cart;
import com.uijae.cms.exception.CustomException;
import com.uijae.cms.service.ProductItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.IntStream;

import static com.uijae.cms.exception.ErrorCode.ORDER_FAIL_CHECK_CART;
import static com.uijae.cms.exception.ErrorCode.ORDER_FAIL_NO_MONEY;

@Service
@RequiredArgsConstructor
public class OrderServiceApplication {
    // 결제를 위해 필요한 것
    /**
     * 1. 물건들이 전부 주문 가능한 상태인지 확인
     * 2. 가격 변동이 있었는지에 대해 확인
     * 3. 고객의 돈이 충분한지 확인
     * 4. 결제 & 상품의 재고 관리
     */
    private final CartApplication cartApplication;
    private final UserClient userClient;
    private final ProductItemService productItemService;

    @Transactional
    public void order(String token, Cart cart) {
        // 1. 주문 시 기존 장바구니 버림
        // 2. 선택 주문 -> 사지 않은 아이템은 살려두기

        Cart orderCart = cartApplication.refreshCart(cart);

        if (orderCart.getMessages().size() > 0) {
            // error messages
            throw new CustomException(ORDER_FAIL_CHECK_CART);
        }

        CustomerDto customerDto = userClient.getCustomerInfo(token).getBody();

        int totalPrice = getTotalPrice(cart);

        if (customerDto.getBalance() < totalPrice) {
            throw new CustomException(ORDER_FAIL_NO_MONEY);
        }

        // + 롤백 계획에 대해서도 생각해야함

        userClient.changeBalance(token, ChangeBalanceForm.builder()
                .from("USER")
                .message("Order")
                .money(-totalPrice)
                .build());

        for (Cart.Product product : orderCart.getProducts()) {
            for (Cart.ProductItem cartItem : product.getItems()) {
                ProductItem productItem = productItemService.getProductItem(cartItem.getId());
                productItem.setCount(productItem.getCount() - cartItem.getCount());
            }
        }
    }

    private Integer getTotalPrice(Cart cart) {
        return cart.getProducts().stream()
                .flatMapToInt(product -> product.getItems().stream()
                        .flatMapToInt(productItem -> IntStream.of(productItem.getPrice() * productItem.getCount())))
                .sum();
    }
}
