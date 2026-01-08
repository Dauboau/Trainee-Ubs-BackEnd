package com.ubs.ExpenseManager.gateways.dto;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import lombok.Data;

@Data
public class CurrencyApiAdapterResponse {

    private String date;
    
    private Map<String, BigDecimal> rates = new HashMap<>();

    @JsonAnySetter
    public void setCurrency(String key, Map<String, BigDecimal> currencyRates) {
        if (currencyRates != null) {
            this.rates.putAll(currencyRates);
        }
    }
    
}
