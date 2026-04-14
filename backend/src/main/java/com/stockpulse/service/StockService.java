package com.stockpulse.service;

import com.stockpulse.model.PriceHistory;
import com.stockpulse.model.Stock;
import com.stockpulse.repository.PriceHistoryRepository;
import com.stockpulse.repository.StockRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockService {

    private final StockRepository        stockRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final Random random = new Random();

    public List<Stock> getAllStocks() {
        return stockRepository.findAll();
    }

    public Optional<Stock> getBySymbol(String symbol) {
        return stockRepository.findBySymbol(symbol.toUpperCase());
    }

    public List<Stock> search(String query) {
        return stockRepository.search(query);
    }

    public List<PriceHistory> getHistory(String symbol) {
        return priceHistoryRepository.findBySymbolOrderByRecordedAtAsc(symbol.toUpperCase());
    }

    public List<Stock> getTopGainers() {
        return stockRepository.findTopGainers();
    }

    public List<Stock> getTopLosers() {
        return stockRepository.findTopLosers();
    }

    @Transactional
    public void simulatePriceUpdates() {
        List<Stock> stocks = stockRepository.findAll();
        for (Stock s : stocks) {
            double factor = 1.0 + (random.nextDouble() * 0.014 - 0.007);
            BigDecimal newPrice = s.getPrice()
                    .multiply(BigDecimal.valueOf(factor))
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal chg = newPrice.subtract(s.getPreviousClose())
                    .divide(s.getPreviousClose(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
            s.setPrice(newPrice);
            s.setChangePercent(chg);
            priceHistoryRepository.save(
                    PriceHistory.builder().symbol(s.getSymbol()).price(newPrice).build());
        }
        stockRepository.saveAll(stocks);
        log.debug("Price tick done for {} stocks", stocks.size());
    }

    @PostConstruct
    @Transactional
    public void seedStocks() {
        if (stockRepository.count() > 0) return;
        log.info("Seeding 25 NSE stocks...");
        List<Stock> seeds = List.of(
            mk("RELIANCE",  "Reliance Industries Ltd",   2847.50, "Oil & Gas",  "19.2L Cr", "NSE", "RL"),
            mk("TCS",       "Tata Consultancy Services", 3982.10, "IT",         "14.5L Cr", "NSE", "TC"),
            mk("HDFCBANK",  "HDFC Bank Ltd",             1748.35, "Banking",    "13.3L Cr", "NSE", "HD"),
            mk("INFY",      "Infosys Ltd",               1892.45, "IT",         "7.9L Cr",  "NSE", "IN"),
            mk("ICICIBANK", "ICICI Bank Ltd",            1284.70, "Banking",    "9.1L Cr",  "NSE", "IC"),
            mk("WIPRO",     "Wipro Ltd",                  547.25, "IT",         "2.9L Cr",  "NSE", "WI"),
            mk("LT",        "Larsen & Toubro Ltd",       3614.80, "Infra",      "5.1L Cr",  "NSE", "LT"),
            mk("MARUTI",    "Maruti Suzuki India",      12842.00, "Auto",       "3.9L Cr",  "NSE", "MS"),
            mk("BAJFINANCE","Bajaj Finance Ltd",         7248.00, "NBFC",       "4.4L Cr",  "NSE", "BF"),
            mk("SUNPHARMA", "Sun Pharmaceutical",        1847.00, "Pharma",     "4.4L Cr",  "NSE", "SP"),
            mk("TATAMOTORS","Tata Motors Ltd",            984.30, "Auto",       "3.6L Cr",  "NSE", "TM"),
            mk("AXISBANK",  "Axis Bank Ltd",             1142.60, "Banking",    "3.5L Cr",  "NSE", "AX"),
            mk("ONGC",      "Oil & Natural Gas Corp",    287.40,  "Oil & Gas",  "3.6L Cr",  "NSE", "ON"),
            mk("NTPC",      "NTPC Ltd",                  384.20,  "Power",      "3.7L Cr",  "NSE", "NT"),
            mk("COALINDIA", "Coal India Ltd",            478.60,  "Mining",     "2.9L Cr",  "NSE", "CI"),
            mk("HINDUNILVR","Hindustan Unilever",       2284.50,  "FMCG",      "5.4L Cr",  "NSE", "HU"),
            mk("TITAN",     "Titan Company Ltd",        3842.00,  "Consumer",   "3.4L Cr",  "NSE", "TI"),
            mk("ITC",       "ITC Ltd",                   482.15,  "FMCG",      "6.0L Cr",  "NSE", "IT"),
            mk("SBILIFE",   "SBI Life Insurance",       1687.00,  "Insurance",  "1.7L Cr",  "NSE", "SL"),
            mk("ADANIGREEN","Adani Green Energy",       1847.30,  "Renewable",  "2.9L Cr",  "NSE", "AG"),
            mk("POWERGRID", "Power Grid Corp",           312.40,  "Power",      "2.9L Cr",  "NSE", "PG"),
            mk("NESTLEIND", "Nestle India Ltd",         2487.00,  "FMCG",      "2.4L Cr",  "NSE", "NE"),
            mk("ZOMATO",    "Zomato Ltd",               248.90,   "Tech",       "2.2L Cr",  "NSE", "ZO"),
            mk("BAJAJFINSV","Bajaj Finserv Ltd",       1684.00,   "NBFC",       "2.7L Cr",  "NSE", "BS"),
            mk("HCLTECH",   "HCL Technologies Ltd",    1482.60,   "IT",         "4.0L Cr",  "NSE", "HC")
        );
        stockRepository.saveAll(seeds);
        log.info("Seeded {} stocks.", seeds.size());
    }

    private Stock mk(String sym, String name, double price,
                     String sector, String mc, String exchange, String icon) {
        BigDecimal p = BigDecimal.valueOf(price);
        return Stock.builder()
                .symbol(sym).companyName(name).price(p).previousClose(p)
                .changePercent(BigDecimal.ZERO)
                .volume(500000L + (long)(random.nextDouble() * 5000000))
                .sector(sector).marketCap(mc).exchange(exchange).iconCode(icon)
                .build();
    }
}