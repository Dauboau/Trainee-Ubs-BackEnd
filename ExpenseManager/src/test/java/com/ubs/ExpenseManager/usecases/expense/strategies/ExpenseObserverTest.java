package com.ubs.ExpenseManager.usecases.expense.strategies;

import com.ubs.ExpenseManager.entities.alert.enums.AlertType;
import com.ubs.ExpenseManager.entities.department.Department;
import com.ubs.ExpenseManager.entities.department.SpendingSetting;
import com.ubs.ExpenseManager.entities.department.SpendingSettingId;
import com.ubs.ExpenseManager.entities.department.enums.CurrencyCode;
import com.ubs.ExpenseManager.entities.department.enums.SpendingType;
import com.ubs.ExpenseManager.entities.employee.Employee;
import com.ubs.ExpenseManager.entities.expense.Expense;
import com.ubs.ExpenseManager.entities.expense.enums.ExpenseCategory;
import com.ubs.ExpenseManager.usecases.alert.AlertUseCase;
import com.ubs.ExpenseManager.usecases.expense.strategies.settingStrategy.MonthlyValidationStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MonthlyValidationStrategy Tests")
class MonthlyValidationStrategyTest {

    @Mock
    private AlertUseCase alertUseCase;

    @InjectMocks
    private MonthlyValidationStrategy strategy;

    @Captor
    private ArgumentCaptor<UUID> expenseIdCaptor;

    @Captor
    private ArgumentCaptor<AlertType> alertTypeCaptor;

    @Captor
    private ArgumentCaptor<String> messageCaptor;

    private Expense testExpense;
    private SpendingSetting monthlySetting;
    private Department department;
    private Employee employee;

    @BeforeEach
    void setUp() {
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
    @DisplayName("Should pass validation when expense is within monthly budget and no prior expenses")
    void shouldPassValidation_WhenWithinBudget_NoPriorExpenses() {
        // Given
        List<Expense> approvedExpenses = List.of();
        BigDecimal amountConverted = new BigDecimal("1000");

        // When
        strategy.validate(testExpense, monthlySetting, approvedExpenses, amountConverted);

        // Then
        verify(alertUseCase, never()).create(any(), any(), any());
    }

    @Test
    @DisplayName("Should send alert when expense amount itself exceeds monthly budget")
    void shouldSendAlert_WhenAmountExceedsMonthlyBudget() {
        // Given
        testExpense.setAmount(new BigDecimal("12000"));
        BigDecimal amountConverted = new BigDecimal("12000");
        List<Expense> approvedExpenses = List.of();

        // When
        strategy.validate(testExpense, monthlySetting, approvedExpenses, amountConverted);

        // Then
        verify(alertUseCase, times(2)).create(
                expenseIdCaptor.capture(),
                alertTypeCaptor.capture(),
                messageCaptor.capture()
        );

        assertThat(expenseIdCaptor.getValue()).isEqualTo(testExpense.getId());
        assertThat(alertTypeCaptor.getValue()).isEqualTo(AlertType.CATEGORY_DAILY);
        assertThat(messageCaptor.getValue()).contains("exceeds the budget");
        assertThat(messageCaptor.getValue()).contains("12000");
        assertThat(messageCaptor.getValue()).contains("TRAVEL");
    }

    @Test
    @DisplayName("Should send alert when projected total exceeds monthly budget")
    void shouldSendAlert_WhenProjectedTotalExceedsMonthlyBudget() {
        // Given
        Expense priorExpense1 = createExpenseInMonth(new BigDecimal("6000"), 5);
        Expense priorExpense2 = createExpenseInMonth(new BigDecimal("3000"), 10);
        List<Expense> approvedExpenses = List.of(priorExpense1, priorExpense2);

        testExpense.setAmount(new BigDecimal("2000"));
        BigDecimal amountConverted = new BigDecimal("2000");
        // Total: 6000 + 3000 + 2000 = 11000 > 10000

        // When
        strategy.validate(testExpense, monthlySetting, approvedExpenses, amountConverted);

        // Then
        verify(alertUseCase, times(1)).create(
                eq(testExpense.getId()),
                eq(AlertType.CATEGORY_DAILY),
                messageCaptor.capture()
        );

        assertThat(messageCaptor.getValue()).contains("exceeds the available monthly budget");
        assertThat(messageCaptor.getValue()).contains("2000");
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

        testExpense.setAmount(new BigDecimal("4000"));
        testExpense.setCategory(ExpenseCategory.TRAVEL);
        BigDecimal amountConverted = new BigDecimal("4000");
        // Only TRAVEL: 5000 + 4000 = 9000 < 10000

        // When
        strategy.validate(testExpense, monthlySetting, approvedExpenses, amountConverted);

        // Then
        verify(alertUseCase, never()).create(any(), any(), any());
    }

    @Test
    @DisplayName("Should consider all expenses in the month regardless of day")
    void shouldConsiderAllExpensesInMonth() {
        // Given
        Expense expense1 = createExpenseInMonth(new BigDecimal("2000"), 1);  // First day
        Expense expense2 = createExpenseInMonth(new BigDecimal("3000"), 15); // Middle
        Expense expense3 = createExpenseInMonth(new BigDecimal("2000"), 31); // Last day

        List<Expense> approvedExpenses = List.of(expense1, expense2, expense3);

        testExpense.setAmount(new BigDecimal("4000"));
        BigDecimal amountConverted = new BigDecimal("4000");
        // Total: 2000 + 3000 + 2000 + 4000 = 11000 > 10000

        // When
        strategy.validate(testExpense, monthlySetting, approvedExpenses, amountConverted);

        // Then
        verify(alertUseCase, times(1)).create(any(), any(), any());
    }

    @Test
    @DisplayName("Should handle exchange rates correctly")
    void shouldHandleExchangeRatesCorrectly() {
        // Given
        Expense priorExpense = createExpenseInMonth(new BigDecimal("2000"), 10);
        priorExpense.setExchangeRate(new BigDecimal("2.5")); // 2000 * 2.5 = 5000
        List<Expense> approvedExpenses = List.of(priorExpense);

        testExpense.setAmount(new BigDecimal("6000"));
        testExpense.setExchangeRate(BigDecimal.ONE);
        BigDecimal amountConverted = new BigDecimal("6000");
        // Total: 5000 + 6000 = 11000 > 10000

        // When
        strategy.validate(testExpense, monthlySetting, approvedExpenses, amountConverted);

        // Then
        verify(alertUseCase, times(1)).create(any(), any(), any());
    }

    @Test
    @DisplayName("Should send both alerts when both conditions are violated")
    void shouldSendBothAlerts_WhenBothConditionsViolated() {
        // Given
        testExpense.setAmount(new BigDecimal("12000")); // Exceeds budget itself
        BigDecimal amountConverted = new BigDecimal("12000");

        Expense priorExpense = createExpenseInMonth(new BigDecimal("1000"), 5);
        List<Expense> approvedExpenses = List.of(priorExpense);
        // Projected: 1000 + 12000 = 13000 > 10000

        // When
        strategy.validate(testExpense, monthlySetting, approvedExpenses, amountConverted);

        // Then
        verify(alertUseCase, times(2)).create(any(), any(), any());
    }

    @Test
    @DisplayName("Should pass validation when expense exactly equals remaining budget")
    void shouldPassValidation_WhenExactlyEqualsRemainingBudget() {
        // Given
        Expense priorExpense = createExpenseInMonth(new BigDecimal("7000"), 5);
        List<Expense> approvedExpenses = List.of(priorExpense);

        testExpense.setAmount(new BigDecimal("3000"));
        BigDecimal amountConverted = new BigDecimal("3000");
        // Total: 7000 + 3000 = 10000 (exactly the budget)

        // When
        strategy.validate(testExpense, monthlySetting, approvedExpenses, amountConverted);

        // Then
        verify(alertUseCase, never()).create(any(), any(), any());
    }

    @Test
    @DisplayName("Should calculate remaining budget correctly in alert message")
    void shouldCalculateRemainingBudgetCorrectly() {
        // Given
        Expense priorExpense = createExpenseInMonth(new BigDecimal("6000"), 5);
        List<Expense> approvedExpenses = List.of(priorExpense);

        testExpense.setAmount(new BigDecimal("5000"));
        BigDecimal amountConverted = new BigDecimal("5000");
        // Remaining: 10000 - 6000 = 4000
        // Projected: 6000 + 5000 = 11000 > 10000

        // When
        strategy.validate(testExpense, monthlySetting, approvedExpenses, amountConverted);

        // Then
        verify(alertUseCase, times(1)).create(
                any(),
                any(),
                messageCaptor.capture()
        );

        assertThat(messageCaptor.getValue()).contains("4000"); // Remaining budget
    }

    @Test
    @DisplayName("Should handle empty approved expenses list")
    void shouldHandleEmptyApprovedExpenses() {
        // Given
        List<Expense> approvedExpenses = List.of();
        testExpense.setAmount(new BigDecimal("5000"));
        BigDecimal amountConverted = new BigDecimal("5000");

        // When
        strategy.validate(testExpense, monthlySetting, approvedExpenses, amountConverted);

        // Then
        verify(alertUseCase, never()).create(any(), any(), any());
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