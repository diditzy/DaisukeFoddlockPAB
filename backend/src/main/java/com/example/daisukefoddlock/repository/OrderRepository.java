package com.example.daisukefoddlock.repository;

import com.example.daisukefoddlock.entity.Order;
import com.example.daisukefoddlock.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomer(User customer);
    Optional<Order> findByExternalOrderId(String externalOrderId);
}
