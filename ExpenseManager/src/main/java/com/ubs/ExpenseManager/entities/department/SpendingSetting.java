package com.ubs.ExpenseManager.entities.department;

import com.ubs.ExpenseManager.entities.department.enums.SpendingType;
import com.ubs.ExpenseManager.entities.expense.enums.ExpenseCategory;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "spending_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class SpendingSetting {

    @EmbeddedId
    private SpendingSettingId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department", referencedColumnName = "name", insertable = false, updatable = false)
    private Department department;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal budget;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Transient
    public String getDepartmentName() {
        return id != null ? id.getDepartmentName() : null;
    }

    @Transient
    public ExpenseCategory getCategory() {
        return id != null ? id.getCategory() : null;
    }

    @Transient
    public SpendingType getType() {
        return id != null ? id.getType() : null;
    }
    //NOTE: Consider adding column to track author of change.

    public SpendingSetting(SpendingSettingId id, BigDecimal budget) {
        this.id = id;
        this.budget = budget;
    }
}
