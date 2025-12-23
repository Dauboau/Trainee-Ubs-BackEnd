package com.ubs.ExpenseManager.config;

import com.github.f4b6a3.uuid.UuidCreator;
import java.util.UUID;

public class UuidGenerator {
    
    /**
     * Generates a time-ordered UUID v7.
     * UUIDs are sortable by creation time, improving database index performance.
     * 
     * @return A new UUID v7
     */
    public static UUID generateV7() {
        return UuidCreator.getTimeOrderedEpoch();
    }
}
