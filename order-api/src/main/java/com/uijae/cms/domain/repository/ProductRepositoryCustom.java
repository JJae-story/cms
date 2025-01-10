package com.uijae.cms.domain.repository;

import com.uijae.cms.domain.model.Product;

import java.util.List;

public interface ProductRepositoryCustom {
    List<Product> searchByName(String name);
}
