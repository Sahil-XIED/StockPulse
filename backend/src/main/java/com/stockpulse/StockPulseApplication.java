package com.stockpulse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * StockPulse — Main Application Entry Point
 *
 * How to run in Eclipse:
 *   Right-click this file → Run As → Java Application
 *
 * Server: http://localhost:8080
 * Test:   http://localhost:8080/api/stocks
 */
@SpringBootApplication(scanBasePackages = "com.stockpulse")
@EnableScheduling
public class StockPulseApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockPulseApplication.class, args);

        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.println("║   StockPulse Backend Started! 🚀     ║");
        System.out.println("║   API  → http://localhost:8080/api   ║");
        System.out.println("║   Test → http://localhost:8080/api/stocks ║");
        System.out.println("╚══════════════════════════════════════╝\n");
    }
}