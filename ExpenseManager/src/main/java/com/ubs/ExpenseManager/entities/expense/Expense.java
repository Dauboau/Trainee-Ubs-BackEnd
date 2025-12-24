package com.ubs.ExpenseManager.entities.expense;

import com.ubs.ExpenseManager.config.UuidV7;
import com.ubs.ExpenseManager.entities.department.enums.CurrencyCode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.ubs.ExpenseManager.entities.employee.Employee;
import com.ubs.ExpenseManager.entities.expense.enums.DecisionType;
import com.ubs.ExpenseManager.entities.expense.enums.ExpenseCategory;
import com.ubs.ExpenseManager.entities.expense.enums.ExpenseStatus;

@Entity
@Table(name = "expenses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Expense {

    @Id
    @UuidV7
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private OffsetDateTime date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseCategory category;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "currency_code")
    private CurrencyCode currency;

    @Column(length = 510)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Employee manager;

    @Enumerated(EnumType.STRING)
    @Column(name = "manager_decision", columnDefinition = "decision_type")
    private DecisionType managerDecision;

    @Column(name = "manager_decision_date")
    private OffsetDateTime managerDecisionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "finance_id")
    private Employee finance;

    @Enumerated(EnumType.STRING)
    @Column(name = "finance_decision", columnDefinition = "decision_type")
    private DecisionType financeDecision;

    @Column(name = "finance_decision_date")
    private OffsetDateTime financeDecisionDate;

    @Column(nullable = false)
    private Boolean revision;

    @Column(name = "receipt_url", nullable = false)
    private String receiptUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "receipt_metadata", columnDefinition = "jsonb")
    private String receiptMetadata;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Transient
    public ExpenseStatus getStatus() {        
        if (managerDecision == DecisionType.REJECTED || financeDecision == DecisionType.REJECTED) {
            return ExpenseStatus.REJECTED;
        }
        
        if (managerDecision == DecisionType.APPROVED && financeDecision == null) {
            return ExpenseStatus.APPROVED_BY_MANAGER;
        }
        
        if (managerDecision == DecisionType.APPROVED && financeDecision == DecisionType.APPROVED) {
            return ExpenseStatus.APPROVED_BY_FINANCE;
        }
        
        return ExpenseStatus.PENDING;
    }
}
