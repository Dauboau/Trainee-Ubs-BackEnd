package com.ubs.ExpenseManager.usecases.currency;

import com.ubs.ExpenseManager.entities.department.enums.CurrencyCode;
import com.ubs.ExpenseManager.usecases.currency.dto.CurrencyRateResponse;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Slf4j
public class CurrencyConverter {

    private static final String API_BASE_URL = "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies";
    
    private final RestTemplate restTemplate;

    /**
     * Converts an amount from one currency to another currency.
     *
     * @param amount Amount to be converted
     * @param from Source currency
     * @param to Target currency
     * @return Converted amount in the target currency
     */
    public CurrencyConverter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public BigDecimal convert(BigDecimal amount, CurrencyCode from, CurrencyCode to) {
        if (amount == null || from == null || to == null) {
            throw new IllegalArgumentException("Amount, from currency and to currency cannot be null");
        }

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }

        if (from == to) {
            return amount;
        }

        BigDecimal rate = getExchangeRate(from, to);
        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Retrieves the exchange rate between two currencies.
     *
     * @param from Source currency
     * @param to Target currency
     * @return Exchange rate
     */
    @Cacheable(value = "ExchangeRatesCache", key = "#from + '_' + #to")
    public BigDecimal getExchangeRate(CurrencyCode from, CurrencyCode to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("From currency and to currency cannot be null");
        }

        if (from == to) {
            return BigDecimal.ONE;
        }

        try {
            String fromCurrency = from.name().toLowerCase();
            String toCurrency = to.name().toLowerCase();
            
            String url = String.format("%s/%s.json", API_BASE_URL, fromCurrency);
            
            CurrencyRateResponse response = restTemplate.getForObject(url, CurrencyRateResponse.class);
            
            if (response == null || response.getRates() == null) {
                throw new RuntimeException("Failed to fetch exchange rates from API");
            }

            BigDecimal rate = response.getRates().get(toCurrency);
            
            if (rate == null) {
                throw new RuntimeException(
                    String.format("Exchange rate not found for conversion from %s to %s", from, to)
                );
            }

            log.info("Exchange rate from {} to {}: {}", from, to, rate);
            return rate.setScale(8, RoundingMode.HALF_UP);
            
        } catch (Exception e) {
            log.error("Error fetching exchange rate from {} to {}: {}", from, to, e.getMessage());
            throw new RuntimeException(
                String.format("Failed to get exchange rate from %s to %s: %s", from, to, e.getMessage()),
                e
            );
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    @CacheEvict(value = "ExchangeRatesCache", allEntries = true)
    private void clearCacheHourly() {
        log.info("ExchangeRatesCache cleared at {}", java.time.LocalDateTime.now());
    }
}
