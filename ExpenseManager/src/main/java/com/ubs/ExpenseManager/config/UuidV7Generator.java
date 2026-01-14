package com.ubs.ExpenseManager.config;

import com.github.f4b6a3.uuid.UuidCreator;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

public class UuidV7Generator implements IdentifierGenerator {
    
    /**
     * Generates a time-ordered UUID v7.
     * UUIDs are sortable by creation time, improving database index performance.
     * 
     * @return A new UUID v7
     */
    @Override
    public Object generate(SharedSessionContractImplementor session, Object object) {
        return UuidCreator.getTimeOrderedEpoch();
    }
}
