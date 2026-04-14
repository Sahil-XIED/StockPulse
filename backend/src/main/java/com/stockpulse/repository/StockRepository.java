package com.stockpulse.repository;

import com.stockpulse.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    Optional<Stock> findBySymbol(String symbol);

    List<Stock> findBySectorIgnoreCase(String sector);

    @Query("SELECT s FROM Stock s WHERE UPPER(s.symbol) LIKE UPPER(CONCAT('%',:q,'%')) " +
           "OR LOWER(s.companyName) LIKE LOWER(CONCAT('%',:q,'%'))")
    List<Stock> search(@Param("q") String query);

    @Query("SELECT s FROM Stock s ORDER BY s.changePercent DESC")
    List<Stock> findTopGainers();

    @Query("SELECT s FROM Stock s ORDER BY s.changePercent ASC")
    List<Stock> findTopLosers();
}