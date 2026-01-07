package com.ubs.ExpenseManager.usecases.currency.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
public class CurrencyRateResponse {
    
    private String date;
    
    private Map<String, BigDecimal> rates = new HashMap<>();

    @JsonAnySetter
    public void setCurrency(String key, Map<String, BigDecimal> currencyRates) {
        if (currencyRates != null) {
            this.rates.putAll(currencyRates);
        }
    }
    
}
