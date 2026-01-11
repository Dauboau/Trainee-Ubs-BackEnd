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

@DisplayName("MonthlyValidationStrategy Tests")
class MonthlyValidationStrategyTest {

    private MonthlyValidationStrategy strategy;
    private Expense testExpense;
    private SpendingSetting monthlySetting;
    private Department department;
    private Employee employee;

    @BeforeEach
    void setUp() {
        strategy = new MonthlyValidationStrategy();

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
        testExpense.setAmount(new BigDecimal("1000"));
        testExpense.setCurrency(CurrencyCode.USD);
        testExpense.setExchangeRate(BigDecimal.ONE);
        testExpense.setDate(OffsetDateTime.of(2025, 1, 15, 10, 0, 0, 0, ZoneOffset.UTC));
        testExpense.setCategory(ExpenseCategory.TRAVEL);
        testExpense.setReceiptUrl("https://receipts.example.com/receipt123.pdf");

        // Setup monthly setting
        SpendingSettingId settingId = new SpendingSettingId();
        settingId.setDepartmentName("Engineering");
        settingId.setCategory(ExpenseCategory.TRAVEL);
        settingId.setType(SpendingType.MONTHLY);

        monthlySetting = new SpendingSetting();
        monthlySetting.setId(settingId);
        monthlySetting.setBudget(new BigDecimal("10000"));
    }

    @Test
    @DisplayName("Should return MONTHLY as the strategy type")
    void shouldReturnMonthlyType() {
        assertThat(strategy.getType()).isEqualTo(SpendingType.MONTHLY);
    }

    @Test
    @DisplayName("Should not add alerts when expense is within monthly budget and no prior expenses")
    void shouldNotAddAlerts_WhenWithinBudget_NoPriorExpenses() {
        // Given
        List<Expense> approvedExpenses = List.of();
        List<Alert> alerts = new ArrayList<>();
        BigDecimal amountConverted = new BigDecimal("1000");

        // When
        strategy.validate(testExpense, monthlySetting, approvedExpenses, amountConverted, alerts);

        // Then
        assertThat(alerts).isEmpty();
    }

    @Test
    @DisplayName("Should add alert when expense amount itself exceeds monthly budget")
    void shouldAddAlert_WhenAmountExceedsMonthlyBudget() {
        // Given
        testExpense.setAmount(new BigDecimal("12000"));
        BigDecimal amountConverted = new BigDecimal("12000");
        List<Expense> approvedExpenses = List.of();
        List<Alert> alerts = new ArrayList<>();

        // When
        strategy.validate(testExpense, monthlySetting, approvedExpenses, amountConverted, alerts);

        // Then
        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getType()).isEqualTo(AlertType.CATEGORY_MONTHLY);
        assertThat(alerts.get(0).getMessage()).contains("exceeds the monthly budget");
        assertThat(alerts.get(0).getMessage()).contains("12000");
        assertThat(alerts.get(0).getMessage()).contains("TRAVEL");
    }

    @Test
    @DisplayName("Should add alert when projected total exceeds monthly budget")
    void shouldAddAlert_WhenProjectedTotalExceedsMonthlyBudget() {
        // Given
        Expense priorExpense1 = createExpenseInMonth(new BigDecimal("6000"), 5);
        Expense priorExpense2 = createExpenseInMonth(new BigDecimal("3000"), 10);
        List<Expense> approvedExpenses = List.of(priorExpense1, priorExpense2);
        List<Alert> alerts = new ArrayList<>();

        testExpense.setAmount(new BigDecimal("2000"));
        BigDecimal amountConverted = new BigDecimal("2000");
        // Total: 6000 + 3000 + 2000 = 11000 > 10000

        // When
        strategy.validate(testExpense, monthlySetting, approvedExpenses, amountConverted, alerts);

        // Then
        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getType()).isEqualTo(AlertType.CATEGORY_MONTHLY);
        assertThat(alerts.get(0).getMessage()).contains("exceeds the available monthly budget");
    }

    @Test
    @DisplayName("Should only consider expenses from the same category")
    void shouldOnlyConsiderExpensesFromSameCategory() {
        // Given
        Expense travelExpense = createExpenseInMonth(new BigDecimal("5000"), 5);
        travelExpense.setCategory(ExpenseCategory.TRAVEL);

        Expense mealExpense = createExpenseInMonth(new BigDecimal("4000"), 10);
        mealExpense.setCategory(ExpenseCategory.MEAL); // Different category

        List<Expense> approvedExpenses = List.of(travelExpense, mealExpense);
        List<Alert> alerts = new ArrayList<>();

        testExpense.setAmount(new BigDecimal("4000"));
        testExpense.setCategory(ExpenseCategory.TRAVEL);
        BigDecimal amountConverted = new BigDecimal("4000");
        // Only TRAVEL: 5000 + 4000 = 9000 < 10000

        // When
        strategy.validate(testExpense, monthlySetting, approvedExpenses, amountConverted, alerts);

        // Then
        assertThat(alerts).isEmpty();
    }

    @Test
    @DisplayName("Should consider all expenses in the month regardless of day")
    void shouldConsiderAllExpensesInMonth() {
        // Given
        Expense expense1 = createExpenseInMonth(new BigDecimal("2000"), 1);
        Expense expense2 = createExpenseInMonth(new BigDecimal("3000"), 15);
        Expense expense3 = createExpenseInMonth(new BigDecimal("2000"), 31);
        List<Expense> approvedExpenses = List.of(expense1, expense2, expense3);
        List<Alert> alerts = new ArrayList<>();

        testExpense.setAmount(new BigDecimal("4000"));
        BigDecimal amountConverted = new BigDecimal("4000");
        // Total: 2000 + 3000 + 2000 + 4000 = 11000 > 10000

        // When
        strategy.validate(testExpense, monthlySetting, approvedExpenses, amountConverted, alerts);

        // Then
        assertThat(alerts).hasSize(1);
    }

    @Test
    @DisplayName("Should handle exchange rates correctly")
    void shouldHandleExchangeRatesCorrectly() {
        // Given
        Expense priorExpense = createExpenseInMonth(new BigDecimal("2000"), 10);
        priorExpense.setExchangeRate(new BigDecimal("2.5")); // 2000 * 2.5 = 5000
        List<Expense> approvedExpenses = List.of(priorExpense);
        List<Alert> alerts = new ArrayList<>();

        testExpense.setAmount(new BigDecimal("6000"));
        testExpense.setExchangeRate(BigDecimal.ONE);
        BigDecimal amountConverted = new BigDecimal("6000");
        // Total: 5000 + 6000 = 11000 > 10000

        // When
        strategy.validate(testExpense, monthlySetting, approvedExpenses, amountConverted, alerts);

        // Then
        assertThat(alerts).hasSize(1);
    }

    @Test
    @DisplayName("Should add both alerts when both conditions are violated")
    void shouldAddBothAlerts_WhenBothConditionsViolated() {
        // Given
        testExpense.setAmount(new BigDecimal("12000")); // Exceeds budget itself
        BigDecimal amountConverted = new BigDecimal("12000");

        Expense priorExpense = createExpenseInMonth(new BigDecimal("1000"), 5);
        List<Expense> approvedExpenses = List.of(priorExpense);
        List<Alert> alerts = new ArrayList<>();

        // When
        strategy.validate(testExpense, monthlySetting, approvedExpenses, amountConverted, alerts);

        // Then
        assertThat(alerts).hasSize(2);
        assertThat(alerts).allMatch(alert -> alert.getType() == AlertType.CATEGORY_MONTHLY);
    }

    @Test
    @DisplayName("Should not add alerts when expense exactly equals remaining budget")
    void shouldNotAddAlerts_WhenExactlyEqualsRemainingBudget() {
        // Given
        Expense priorExpense = createExpenseInMonth(new BigDecimal("7000"), 5);
        List<Expense> approvedExpenses = List.of(priorExpense);
        List<Alert> alerts = new ArrayList<>();

        testExpense.setAmount(new BigDecimal("3000"));
        BigDecimal amountConverted = new BigDecimal("3000");
        // Total: 7000 + 3000 = 10000 (exactly the budget)

        // When
        strategy.validate(testExpense, monthlySetting, approvedExpenses, amountConverted, alerts);

        // Then
        assertThat(alerts).isEmpty();
    }

    @Test
    @DisplayName("Should calculate remaining budget correctly in alert message")
    void shouldCalculateRemainingBudgetCorrectly() {
        // Given
        Expense priorExpense = createExpenseInMonth(new BigDecimal("6000"), 5);
        List<Expense> approvedExpenses = List.of(priorExpense);
        List<Alert> alerts = new ArrayList<>();

        testExpense.setAmount(new BigDecimal("5000"));
        BigDecimal amountConverted = new BigDecimal("5000");

        // When
        strategy.validate(testExpense, monthlySetting, approvedExpenses, amountConverted, alerts);

        // Then
        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getMessage()).contains("4000"); // Remaining: 10000 - 6000
    }

    // Helper methods
    private Expense createExpenseInMonth(BigDecimal amount, int dayOfMonth) {
        Expense expense = new Expense();
        expense.setId(UUID.randomUUID());
        expense.setEmployee(employee);
        expense.setDepartment(department);
        expense.setAmount(amount);
        expense.setCurrency(CurrencyCode.USD);
        expense.setExchangeRate(BigDecimal.ONE);
        expense.setDate(OffsetDateTime.of(2025, 1, dayOfMonth, 10, 0, 0, 0, ZoneOffset.UTC));
        expense.setCategory(ExpenseCategory.TRAVEL);
        expense.setReceiptUrl("https://receipts.example.com/receipt-" + UUID.randomUUID() + ".pdf");
        return expense;
    }
}