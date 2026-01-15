package com.ubs.ExpenseManager.gateways.adapters;

import com.ubs.ExpenseManager.entities.department.enums.CurrencyCode;
import com.ubs.ExpenseManager.gateways.CurrencyExchangeGateway;
import com.ubs.ExpenseManager.gateways.dto.CurrencyApiAdapterResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class CurrencyApiAdapter implements CurrencyExchangeGateway {

    private static final String API_BASE_URL = "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies";
    
    private final RestTemplate restTemplate;

    @Override
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
            
            CurrencyApiAdapterResponse response = restTemplate.getForObject(url, CurrencyApiAdapterResponse.class);
            
            if (response == null || response.getRates() == null) {
                throw new RuntimeException("Failed to fetch exchange rates from API");
            }

            BigDecimal rate = response.getRates().get(toCurrency);
            
            if (rate == null) {
                throw new RuntimeException(
                    String.format("Exchange rate not found for conversion from %s to %s", from, to)
                );
            }

            log.info("Exchange rate from {} to {}: {} at {}", from, to, rate, java.time.LocalDateTime.now());
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
