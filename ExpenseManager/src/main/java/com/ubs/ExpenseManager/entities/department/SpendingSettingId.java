package com.ubs.ExpenseManager.entities.department;

import com.ubs.ExpenseManager.entities.department.enums.SpendingType;
import com.ubs.ExpenseManager.entities.expense.enums.ExpenseCategory;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class SpendingSettingId implements Serializable {

    @Column(name = "department", length = 100)
    private String departmentName;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "expense_category")
    private ExpenseCategory category;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "spending_type")
    private SpendingType type;
}
