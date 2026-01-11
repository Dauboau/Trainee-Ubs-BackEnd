package com.ubs.ExpenseManager.usecases.expense.strategies;

import com.ubs.ExpenseManager.entities.alert.Alert;
import com.ubs.ExpenseManager.entities.alert.enums.AlertType;
import com.ubs.ExpenseManager.entities.department.Department;
import com.ubs.ExpenseManager.entities.department.SpendingSetting;
import com.ubs.ExpenseManager.entities.department.SpendingSettingId;
import com.ubs.ExpenseManager.entities.department.enums.CurrencyCode;
import com.ubs.ExpenseManager.entities.department.enums.SpendingType;
import com.ubs.ExpenseManager.entities.employee.Employee;
import com.ubs.ExpenseManager.entities.expense.Expense;
import com.ubs.ExpenseManager.entities.expense.enums.ExpenseCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DailyValidationStrategy Tests")
class DailyValidationStrategyTest {

    private DailyValidationStrategy strategy;
    private Expense testExpense;
    private SpendingSetting dailySetting;
    private Department department;
    private Employee employee;

    @BeforeEach
    void setUp() {
        strategy = new DailyValidationStrategy();

        // Setup department
        department = new Department();
        department.setName("Engineering");
        department.setMonthlyBudget(new BigDecimal("50000"));

        // Setup employee
        employee = new Employee();
        employee.setId(UUID.randomUUID());
        employee.setName("John Doe");

        // Setup test expense
        testExpense = new Expense();
        testExpense.setId(UUID.randomUUID());
        testExpense.setEmployee(employee);
        testExpense.setDepartment(department);
        testExpense.setAmount(new BigDecimal("100"));
        testExpense.setCurrency(CurrencyCode.USD);
        testExpense.setExchangeRate(BigDecimal.ONE);
        testExpense.setDate(OffsetDateTime.of(2025, 1, 15, 10, 0, 0, 0, ZoneOffset.UTC));
        testExpense.setCategory(ExpenseCategory.TRAVEL);
        testExpense.setReceiptUrl("https://receipts.example.com/receipt123.pdf");

        // Setup daily setting
        SpendingSettingId settingId = new SpendingSettingId();
        settingId.setDepartmentName("Engineering");
        settingId.setCategory(ExpenseCategory.TRAVEL);
        settingId.setType(SpendingType.DAILY);

        dailySetting = new SpendingSetting();
        dailySetting.setId(settingId);
        dailySetting.setBudget(new BigDecimal("500"));
    }

    @Test
    @DisplayName("Should return DAILY as the strategy type")
    void shouldReturnDailyType() {
        assertThat(strategy.getType()).isEqualTo(SpendingType.DAILY);
    }

    @Test
    @DisplayName("Should not add alerts when expense is within daily budget and no prior expenses")
    void shouldNotAddAlerts_WhenWithinBudget_NoPriorExpenses() {
        // Given
        List<Expense> approvedExpenses = List.of();
        List<Alert> alerts = new ArrayList<>();
        BigDecimal amountConverted = new BigDecimal("100");

        // When
        strategy.validate(testExpense, dailySetting, approvedExpenses, amountConverted, alerts);

        // Then
        assertThat(alerts).isEmpty();
    }

    @Test
    @DisplayName("Should add alert when expense amount exceeds daily budget")
    void shouldAddAlert_WhenAmountExceedsDailyBudget() {
        // Given
        testExpense.setAmount(new BigDecimal("600"));
        BigDecimal amountConverted = new BigDecimal("600");
        List<Expense> approvedExpenses = List.of();
        List<Alert> alerts = new ArrayList<>();

        // When
        strategy.validate(testExpense, dailySetting, approvedExpenses, amountConverted, alerts);

        // Then
        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getType()).isEqualTo(AlertType.CATEGORY_DAILY);
        assertThat(alerts.get(0).getMessage()).contains("exceeds the daily budget");
        assertThat(alerts.get(0).getMessage()).contains("600");
        assertThat(alerts.get(0).getMessage()).contains("TRAVEL");
    }

    @Test
    @DisplayName("Should add alert when expense exceeds remaining daily budget")
    void shouldAddAlert_WhenExceedsRemainingDailyBudget() {
        // Given
        Expense priorExpense = createExpenseSameDay(new BigDecimal("400"));
        List<Expense> approvedExpenses = List.of(priorExpense);
        List<Alert> alerts = new ArrayList<>();

        testExpense.setAmount(new BigDecimal("150"));
        BigDecimal amountConverted = new BigDecimal("150");

        // When
        strategy.validate(testExpense, dailySetting, approvedExpenses, amountConverted, alerts);

        // Then
        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getType()).isEqualTo(AlertType.CATEGORY_DAILY);
        assertThat(alerts.get(0).getMessage()).contains("exceeds the remaining daily budget");
    }

    @Test
    @DisplayName("Should only consider expenses from the same day")
    void shouldOnlyConsiderExpensesFromSameDay() {
        // Given
        Expense expenseYesterday = createExpense(
                new BigDecimal("400"),
                OffsetDateTime.of(2025, 1, 14, 10, 0, 0, 0, ZoneOffset.UTC)
        );
        List<Expense> approvedExpenses = List.of(expenseYesterday);
        List<Alert> alerts = new ArrayList<>();
        BigDecimal amountConverted = new BigDecimal("100");

        // When
        strategy.validate(testExpense, dailySetting, approvedExpenses, amountConverted, alerts);

        // Then
        assertThat(alerts).isEmpty();
    }

    @Test
    @DisplayName("Should handle multiple approved expenses on same day")
    void shouldHandleMultipleExpensesSameDay() {
        // Given
        Expense expense1 = createExpenseSameDay(new BigDecimal("200"));
        Expense expense2 = createExpenseSameDay(new BigDecimal("150"));
        List<Expense> approvedExpenses = List.of(expense1, expense2);
        List<Alert> alerts = new ArrayList<>();

        testExpense.setAmount(new BigDecimal("200"));
        BigDecimal amountConverted = new BigDecimal("200");

        // When
        strategy.validate(testExpense, dailySetting, approvedExpenses, amountConverted, alerts);

        // Then
        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getMessage()).contains("exceeds the remaining daily budget");
    }

    @Test
    @DisplayName("Should handle exchange rate conversions correctly")
    void shouldHandleExchangeRateConversions() {
        // Given
        Expense priorExpense = createExpenseSameDay(new BigDecimal("100"));
        priorExpense.setExchangeRate(new BigDecimal("2.0")); // 100 * 2 = 200 converted
        List<Expense> approvedExpenses = List.of(priorExpense);
        List<Alert> alerts = new ArrayList<>();

        testExpense.setAmount(new BigDecimal("350"));
        testExpense.setExchangeRate(BigDecimal.ONE);
        BigDecimal amountConverted = new BigDecimal("350");

        // When
        strategy.validate(testExpense, dailySetting, approvedExpenses, amountConverted, alerts);

        // Then
        assertThat(alerts).hasSize(1);
    }

    @Test
    @DisplayName("Should add both alerts when both conditions are violated")
    void shouldAddBothAlerts_WhenBothConditionsViolated() {
        // Given
        testExpense.setAmount(new BigDecimal("600")); // Exceeds budget itself
        BigDecimal amountConverted = new BigDecimal("600");

        Expense priorExpense = createExpenseSameDay(new BigDecimal("100"));
        List<Expense> approvedExpenses = List.of(priorExpense);
        List<Alert> alerts = new ArrayList<>();

        // When
        strategy.validate(testExpense, dailySetting, approvedExpenses, amountConverted, alerts);

        // Then
        assertThat(alerts).hasSize(2);
        assertThat(alerts).allMatch(alert -> alert.getType() == AlertType.CATEGORY_DAILY);
    }

    @Test
    @DisplayName("Should not add alerts when expense exactly equals remaining budget")
    void shouldNotAddAlerts_WhenExactlyEqualsRemainingBudget() {
        // Given
        Expense priorExpense = createExpenseSameDay(new BigDecimal("400"));
        List<Expense> approvedExpenses = List.of(priorExpense);
        List<Alert> alerts = new ArrayList<>();

        testExpense.setAmount(new BigDecimal("100"));
        BigDecimal amountConverted = new BigDecimal("100");

        // When
        strategy.validate(testExpense, dailySetting, approvedExpenses, amountConverted, alerts);

        // Then
        assertThat(alerts).isEmpty();
    }

    // Helper methods
    private Expense createExpenseSameDay(BigDecimal amount) {
        return createExpense(amount, OffsetDateTime.of(2025, 1, 15, 10, 0, 0, 0, ZoneOffset.UTC));
    }

    private Expense createExpense(BigDecimal amount, OffsetDateTime date) {
        Expense expense = new Expense();
        expense.setId(UUID.randomUUID());
        expense.setEmployee(employee);
        expense.setDepartment(department);
        expense.setAmount(amount);
        expense.setCurrency(CurrencyCode.USD);
        expense.setExchangeRate(BigDecimal.ONE);
        expense.setDate(date);
        expense.setCategory(ExpenseCategory.TRAVEL);
        expense.setReceiptUrl("https://receipts.example.com/receipt-" + UUID.randomUUID() + ".pdf");
        return expense;
    }
}