package com.example.daisukefoddlock.repository;

import com.example.daisukefoddlock.entity.Order;
import com.example.daisukefoddlock.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrder(Order order);
    Optional<Payment> findByMidtransTransactionId(String transactionId);
}
