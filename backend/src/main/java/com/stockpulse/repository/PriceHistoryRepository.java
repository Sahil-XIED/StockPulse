package com.stockpulse.repository;

import com.stockpulse.model.PriceHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

    @Query("SELECT p FROM PriceHistory p WHERE p.symbol = :sym ORDER BY p.recordedAt ASC")
    List<PriceHistory> findBySymbolOrderByRecordedAtAsc(@Param("sym") String symbol);

    @Query("SELECT p FROM PriceHistory p WHERE p.symbol = :sym ORDER BY p.recordedAt DESC")
    List<PriceHistory> findLatestBySymbol(@Param("sym") String symbol, Pageable pageable);
}