package com.stockpulse.exception;

public class StockPulseException extends RuntimeException {

    public StockPulseException(String message) {
        super(message);
    }

    public StockPulseException(String message, Throwable cause) {
        super(message, cause);
    }
}