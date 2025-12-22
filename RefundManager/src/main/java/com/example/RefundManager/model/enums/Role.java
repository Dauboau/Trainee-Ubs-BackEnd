package com.example.RefundManager.model.enums;

public enum Role {
    EMPLOYEE("ROLE_EMPLOYEE"),
    MANAGER("ROLE_MANAGER"),
    FINANCE("ROLE_FINANCE");

    private String value;

    Role(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
