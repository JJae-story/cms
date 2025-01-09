package com.uijae.cms.service.customer;

import com.uijae.cms.domain.customer.ChangeBalanceForm;
import com.uijae.cms.domain.model.CustomerBalanceHistory;
import com.uijae.cms.domain.repository.CustomerBalanceHistoryRepository;
import com.uijae.cms.domain.repository.CustomerRepository;
import com.uijae.cms.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.uijae.cms.exception.ErrorCode.NOT_ENOUGH_BALANCE;
import static com.uijae.cms.exception.ErrorCode.NOT_FOUND_USER;

@Service
@RequiredArgsConstructor
public class CustomerBalanceService {

    private final CustomerBalanceHistoryRepository customerBalanceHistoryRepository;
    private final CustomerRepository customerRepository;

    @Transactional(noRollbackFor = {CustomException.class})
    public CustomerBalanceHistory changeBalance(Long customerId, ChangeBalanceForm from) throws CustomException {
        CustomerBalanceHistory customerBalanceHistory =
                customerBalanceHistoryRepository.findFirstByCustomer_IdOrderByIdDesc(customerId)
                        .orElse(CustomerBalanceHistory.builder()
                                .changeMoney(0)
                                .currentMoney(0)
                                .customer(customerRepository.findById(customerId)
                                        .orElseThrow(() -> new CustomException(NOT_FOUND_USER)))
                                .build());

        if (customerBalanceHistory.getCurrentMoney() + from.getMoney() < 0) {
            throw new CustomException(NOT_ENOUGH_BALANCE);
        }

        customerBalanceHistory = CustomerBalanceHistory.builder()
                .changeMoney(from.getMoney())
                .currentMoney(customerBalanceHistory.getCurrentMoney() + from.getMoney())
                .description(from.getMessage())
                .fromMessage(from.getFrom())
                .customer(customerBalanceHistory.getCustomer())
                .build();

        customerBalanceHistory.getCustomer().setBalance(customerBalanceHistory.getCurrentMoney());

        return customerBalanceHistoryRepository.save(customerBalanceHistory);
    }
}
