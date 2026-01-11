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
import com.ubs.ExpenseManager.usecases.expense.observer.ExpenseObserver;
import com.ubs.ExpenseManager.usecases.expense.strategies.settingStrategy.DailyValidationStrategy;
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
@DisplayName("DailyValidationStrategy Tests")
class DailyValidationStrategyTest {

    @Mock
    private AlertUseCase alertUseCase;

    @InjectMocks
    private DailyValidationStrategy strategy;

    @Captor
    private ArgumentCaptor<UUID> expenseIdCaptor;

    @Captor
    private ArgumentCaptor<AlertType> alertTypeCaptor;

    @Captor
    private ArgumentCaptor<String> messageCaptor;

    private Expense testExpense;
    private SpendingSetting dailySetting;
    private Department department;
    private Employee employee;

    @BeforeEach
    void setUp() {
        // Setup department
        department = new Department();
        department.setName("Engineering");
        department.setMonthlyBudget(new BigDecimal("10000"));

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

        // Setup spending setting
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
    @DisplayName("Should pass validation when expense is within daily budget and no prior expenses")
    void shouldPassValidation_WhenWithinBudget_NoPriorExpenses() {
        // Given
        List<Expense> approvedExpenses = List.of();
        BigDecimal amountConverted = new BigDecimal("100");

        // When
        strategy.validate(testExpense, dailySetting, approvedExpenses, amountConverted);

        // Then
        verify(alertUseCase, never()).create(any(), any(), any());
    }

    @Test
    @DisplayName("Should send alert when expense amount exceeds daily budget")
    void shouldSendAlert_WhenAmountExceedsDailyBudget() {
        // Given
        testExpense.setAmount(new BigDecimal("600"));
        BigDecimal amountConverted = new BigDecimal("600");
        List<Expense> approvedExpenses = List.of();

        // When
        strategy.validate(testExpense, dailySetting, approvedExpenses, amountConverted);

        // Then
        verify(alertUseCase, times(1)).create(
                expenseIdCaptor.capture(),
                alertTypeCaptor.capture(),
                messageCaptor.capture()
        );

        assertThat(expenseIdCaptor.getValue()).isEqualTo(testExpense.getId());
        assertThat(alertTypeCaptor.getValue()).isEqualTo(AlertType.CATEGORY_DAILY);
        assertThat(messageCaptor.getValue()).contains("exceeds the daily budget");
        assertThat(messageCaptor.getValue()).contains("600");
        assertThat(messageCaptor.getValue()).contains("TRAVEL");
    }

    @Test
    @DisplayName("Should send alert when expense exceeds remaining daily budget")
    void shouldSendAlert_WhenExceedsRemainingDailyBudget() {
        // Given
        Expense priorExpense = createExpenseSameDay(new BigDecimal("400"));
        List<Expense> approvedExpenses = List.of(priorExpense);
        BigDecimal amountConverted = new BigDecimal("150");
        testExpense.setAmount(new BigDecimal("150"));

        // When
        strategy.validate(testExpense, dailySetting, approvedExpenses, amountConverted);

        // Then
        verify(alertUseCase, times(1)).create(
                eq(testExpense.getId()),
                eq(AlertType.CATEGORY_DAILY),
                messageCaptor.capture()
        );

        assertThat(messageCaptor.getValue()).contains("exceeds the remaining daily budget");
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
        BigDecimal amountConverted = new BigDecimal("100");

        // When
        strategy.validate(testExpense, dailySetting, approvedExpenses, amountConverted);

        // Then
        verify(alertUseCase, never()).create(any(), any(), any());
    }

    @Test
    @DisplayName("Should handle multiple approved expenses on same day")
    void shouldHandleMultipleExpensesSameDay() {
        // Given
        Expense expense1 = createExpenseSameDay(new BigDecimal("200"));
        Expense expense2 = createExpenseSameDay(new BigDecimal("150"));

        List<Expense> approvedExpenses = List.of(expense1, expense2);
        testExpense.setAmount(new BigDecimal("200"));
        BigDecimal amountConverted = new BigDecimal("200");

        // When
        strategy.validate(testExpense, dailySetting, approvedExpenses, amountConverted);

        // Then
        verify(alertUseCase, times(1)).create(
                eq(testExpense.getId()),
                eq(AlertType.CATEGORY_DAILY),
                messageCaptor.capture()
        );

        assertThat(messageCaptor.getValue()).contains("exceeds the remaining daily budget");
    }

    @Test
    @DisplayName("Should handle exchange rate conversions correctly")
    void shouldHandleExchangeRateConversions() {
        // Given
        Expense priorExpense = createExpenseSameDay(new BigDecimal("100"));
        priorExpense.setExchangeRate(new BigDecimal("2.0")); // 100 * 2 = 200 converted

        List<Expense> approvedExpenses = List.of(priorExpense);
        testExpense.setAmount(new BigDecimal("350"));
        testExpense.setExchangeRate(BigDecimal.ONE);
        BigDecimal amountConverted = new BigDecimal("350");

        // When
        strategy.validate(testExpense, dailySetting, approvedExpenses, amountConverted);

        // Then
        verify(alertUseCase, times(1)).create(any(), any(), any());
    }

    @Test
    @DisplayName("Should send both alerts when both conditions are violated")
    void shouldSendBothAlerts_WhenBothConditionsViolated() {
        // Given
        testExpense.setAmount(new BigDecimal("600")); // Exceeds budget itself
        BigDecimal amountConverted = new BigDecimal("600");

        Expense priorExpense = createExpenseSameDay(new BigDecimal("100"));
        List<Expense> approvedExpenses = List.of(priorExpense);

        // When
        strategy.validate(testExpense, dailySetting, approvedExpenses, amountConverted);

        // Then
        verify(alertUseCase, times(2)).create(any(), any(), any());
    }

    @Test
    @DisplayName("Should handle expenses at different times on the same day")
    void shouldHandleExpensesAtDifferentTimesOnSameDay() {
        // Given
        Expense morningExpense = createExpense(
                new BigDecimal("200"),
                OffsetDateTime.of(2025, 1, 15, 8, 30, 0, 0, ZoneOffset.UTC)
        );
        Expense afternoonExpense = createExpense(
                new BigDecimal("150"),
                OffsetDateTime.of(2025, 1, 15, 14, 45, 0, 0, ZoneOffset.UTC)
        );

        List<Expense> approvedExpenses = List.of(morningExpense, afternoonExpense);
        testExpense.setAmount(new BigDecimal("200"));
        BigDecimal amountConverted = new BigDecimal("200");

        // When
        strategy.validate(testExpense, dailySetting, approvedExpenses, amountConverted);

        // Then
        verify(alertUseCase, times(1)).create(any(), any(), any());
    }

    @Test
    @DisplayName("Should pass validation when expense exactly equals remaining budget")
    void shouldPassValidation_WhenExactlyEqualsRemainingBudget() {
        // Given
        Expense priorExpense = createExpenseSameDay(new BigDecimal("400"));
        List<Expense> approvedExpenses = List.of(priorExpense);

        testExpense.setAmount(new BigDecimal("100"));
        BigDecimal amountConverted = new BigDecimal("100");

        // When
        strategy.validate(testExpense, dailySetting, approvedExpenses, amountConverted);

        // Then
        verify(alertUseCase, never()).create(any(), any(), any());
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