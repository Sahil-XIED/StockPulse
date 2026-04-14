package com.stockpulse.repository;

import com.stockpulse.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserIdOrderByTimestampDesc(Long userId);

    List<Order> findBySymbolOrderByTimestampDesc(String symbol);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :uid")
    long countByUserId(@Param("uid") Long userId);
}