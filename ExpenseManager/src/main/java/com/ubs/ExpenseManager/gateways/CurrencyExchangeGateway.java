package com.ubs.ExpenseManager.gateways;

import com.ubs.ExpenseManager.entities.department.enums.CurrencyCode;
import java.math.BigDecimal;

public interface CurrencyExchangeGateway {
    /**
     * Retrieves the exchange rate between two currencies.
     * @param from Source currency
     * @param to Target currency
     * @return Exchange rate
     */
    BigDecimal getExchangeRate(CurrencyCode from, CurrencyCode to);
}
