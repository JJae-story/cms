package com.uijae.cms.domain.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddProductItemForm {
    private Long productId;
    private String name;
    private Integer price;
    private Integer count;
}
