package com.uijae.cms.domain.repository;

import com.uijae.cms.domain.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByIdAndEmail(Long id, String email);
    Optional<Customer> findByEmailAndPasswordAndVerifyIsTrue(String email, String password);
    Optional<Customer> findByEmail(String email);
}
