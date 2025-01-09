package com.uijae.cms.service.customer;

import com.uijae.cms.domain.model.Customer;
import com.uijae.cms.domain.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;

    public Optional<Customer> findByIdAndEmail(Long id, String email) {
        return customerRepository.findByIdAndEmail(id, email);
    }

    public Optional<Customer> findValidCustomer(String email, String password) {
        return customerRepository.findByEmailAndPasswordAndVerifyIsTrue(email, password);
    }
}
