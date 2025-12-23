package com.ubs.ExpenseManager.entities.employee.enums;

public enum Role {
    EMPLOYEE("ROLE_EMPLOYEE"),
    MANAGER("ROLE_MANAGER"),
    FINANCE("ROLE_FINANCE");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
