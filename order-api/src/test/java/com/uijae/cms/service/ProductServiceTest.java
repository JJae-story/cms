package com.uijae.cms.service;

import com.uijae.cms.domain.model.Product;
import com.uijae.cms.domain.product.AddProductForm;
import com.uijae.cms.domain.product.AddProductItemForm;
import com.uijae.cms.domain.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProductServiceTest {
    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void addProduct() {
        Long sellerId = 1L;

        AddProductForm form = makeProductForm("나이키 에어포스", "신발", 3);

        Product p = productService.addProduct(sellerId, form);

        Product result = productRepository.findWithProductItemsById(p.getId()).get();

        assertNotNull(result);
        assertEquals("나이키 에어포스", result.getName());
        assertEquals("신발", result.getDescription());
        assertEquals(3, result.getProductItems().size());
        assertEquals("나이키 에어포스1", result.getProductItems().get(0).getName());
        assertEquals(10000, result.getProductItems().get(0).getPrice());
        assertEquals(1, result.getProductItems().get(0).getCount());
    }

    private static AddProductForm makeProductForm(String name, String description, int itemCnt) {
        List<AddProductItemForm> itemForms = new ArrayList<>();

        for (int i = 1; i <= itemCnt; i++) {
            itemForms.add(makeProductItemForm(null, name + i));
        }

        return AddProductForm.builder()
                .name(name)
                .description(description)
                .items(itemForms)
                .build();
    }

    private static AddProductItemForm makeProductItemForm(Long productId, String name) {
        return AddProductItemForm.builder()
                .productId(productId)
                .name(name)
                .price(10000)
                .count(1)
                .build();
    }
}