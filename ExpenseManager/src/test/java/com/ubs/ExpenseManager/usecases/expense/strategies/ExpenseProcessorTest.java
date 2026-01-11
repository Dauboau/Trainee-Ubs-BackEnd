package com.ubs.ExpenseManager.usecases.expense.strategies;

import com.ubs.ExpenseManager.entities.alert.Alert;
import com.ubs.ExpenseManager.entities.alert.enums.AlertType;
import com.ubs.ExpenseManager.entities.department.Department;
import com.ubs.ExpenseManager.entities.department.SpendingSetting;
import com.ubs.ExpenseManager.entities.department.SpendingSettingId;
import com.ubs.ExpenseManager.entities.department.enums.CurrencyCode;
import com.ubs.ExpenseManager.entities.department.enums.SpendingType;
import com.ubs.ExpenseManager.entities.department.repository.SpendingSettingRepository;
import com.ubs.ExpenseManager.entities.employee.Employee;
import com.ubs.ExpenseManager.entities.expense.Expense;
import com.ubs.ExpenseManager.entities.expense.enums.DecisionType;
import com.ubs.ExpenseManager.entities.expense.enums.ExpenseCategory;
import com.ubs.ExpenseManager.entities.expense.repository.ExpenseRepository;
import com.ubs.ExpenseManager.usecases.expense.ExpenseProcessor;
import com.ubs.ExpenseManager.usecases.expense.strategies.DailyValidationStrategy;
import com.ubs.ExpenseManager.usecases.expense.strategies.MonthlyValidationStrategy;
import com.ubs.ExpenseManager.usecases.expense.strategies.SpendingValidationFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExpenseProcessor Integration Tests")
class ExpenseProcessorTest {

    @Mock
    private SpendingSettingRepository spendingSettingRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private SpendingValidationFactory validationFactory;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ExpenseProcessor expenseProcessor;

    @Captor
    private ArgumentCaptor<List<Alert>> alertsCaptor;

    private Department department;
    private Employee employee;
    private Expense testExpense;
    private SpendingSetting dailySetting;
    private SpendingSetting monthlySetting;
    private DailyValidationStrategy dailyStrategy;
    private MonthlyValidationStrategy monthlyStrategy;

    @BeforeEach
    void setUp() {
        dailyStrategy = new DailyValidationStrategy();
        monthlyStrategy = new MonthlyValidationStrategy();

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
        testExpense.setAmount(new BigDecimal("500"));
        testExpense.setCurrency(CurrencyCode.USD);
        testExpense.setExchangeRate(BigDecimal.ONE);
        testExpense.setDate(OffsetDateTime.of(2025, 1, 15, 10, 0, 0, 0, ZoneOffset.UTC));
        testExpense.setCategory(ExpenseCategory.TRAVEL);
        testExpense.setReceiptUrl("https://receipts.example.com/receipt123.pdf");

        // Setup daily setting
        SpendingSettingId dailyId = new SpendingSettingId();
        dailyId.setDepartmentName("Engineering");
        dailyId.setCategory(ExpenseCategory.TRAVEL);
        dailyId.setType(SpendingType.DAILY);

        dailySetting = new SpendingSetting();
        dailySetting.setId(dailyId);
        dailySetting.setBudget(new BigDecimal("1000"));

        // Setup monthly setting
        SpendingSettingId monthlyId = new SpendingSettingId();
        monthlyId.setDepartmentName("Engineering");
        monthlyId.setCategory(ExpenseCategory.TRAVEL);
        monthlyId.setType(SpendingType.MONTHLY);

        monthlySetting = new SpendingSetting();
        monthlySetting.setId(monthlyId);
        monthlySetting.setBudget(new BigDecimal("15000"));
    }

    @Test
    @DisplayName("Should publish alert when category has no settings configured")
    void shouldPublishAlert_WhenCategoryNotConfigured() {
        // Given
        when(spendingSettingRepository.findByIdDepartmentNameAndIdCategory(
                "Engineering", ExpenseCategory.TRAVEL))
                .thenReturn(List.of());

        // When
        expenseProcessor.process(testExpense);

        // Then
        verify(eventPublisher, times(1)).publishEvent(alertsCaptor.capture());
        List<Alert> alerts = alertsCaptor.getValue();

        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getExpense().getId()).isEqualTo(testExpense.getId());
        assertThat(alerts.get(0).getMessage()).contains("TRAVEL");
        assertThat(alerts.get(0).getMessage()).contains("not configured");
    }

    @Test
    @DisplayName("Should publish alert when settings are null")
    void shouldPublishAlert_WhenSettingsAreNull() {
        // Given
        when(spendingSettingRepository.findByIdDepartmentNameAndIdCategory(
                "Engineering", ExpenseCategory.TRAVEL))
                .thenReturn(null);

        // When
        expenseProcessor.process(testExpense);

        // Then
        verify(eventPublisher, times(1)).publishEvent(alertsCaptor.capture());
        List<Alert> alerts = alertsCaptor.getValue();

        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getMessage()).contains("not configured");
    }

    @Test
    @DisplayName("Should validate with only DAILY strategy when only DAILY setting exists")
    void shouldValidateOnlyDaily_WhenOnlyDailySettingExists() {
        // Given
        when(spendingSettingRepository.findByIdDepartmentNameAndIdCategory(
                "Engineering", ExpenseCategory.TRAVEL))
                .thenReturn(List.of(dailySetting));

        when(expenseRepository.findByDepartmentNameAndFinanceDecisionAndFinanceDecisionDateBetween(
                eq("Engineering"), eq(DecisionType.APPROVED), any(), any()))
                .thenReturn(List.of());

        when(validationFactory.getStrategy(SpendingType.DAILY)).thenReturn(dailyStrategy);

        // When
        expenseProcessor.process(testExpense);

        // Then
        verify(validationFactory, times(1)).getStrategy(SpendingType.DAILY);
        verify(validationFactory, never()).getStrategy(SpendingType.MONTHLY);
    }

    @Test
    @DisplayName("Should validate with only MONTHLY strategy when only MONTHLY setting exists")
    void shouldValidateOnlyMonthly_WhenOnlyMonthlySettingExists() {
        // Given
        when(spendingSettingRepository.findByIdDepartmentNameAndIdCategory(
                "Engineering", ExpenseCategory.TRAVEL))
                .thenReturn(List.of(monthlySetting));

        when(expenseRepository.findByDepartmentNameAndFinanceDecisionAndFinanceDecisionDateBetween(
                eq("Engineering"), eq(DecisionType.APPROVED), any(), any()))
                .thenReturn(List.of());

        when(validationFactory.getStrategy(SpendingType.MONTHLY)).thenReturn(monthlyStrategy);

        // When
        expenseProcessor.process(testExpense);

        // Then
        verify(validationFactory, never()).getStrategy(SpendingType.DAILY);
        verify(validationFactory, times(1)).getStrategy(SpendingType.MONTHLY);
    }

    @Test
    @DisplayName("Should validate with both strategies when both DAILY and MONTHLY settings exist")
    void shouldValidateBothStrategies_WhenBothSettingsExist() {
        // Given
        when(spendingSettingRepository.findByIdDepartmentNameAndIdCategory(
                "Engineering", ExpenseCategory.TRAVEL))
                .thenReturn(List.of(dailySetting, monthlySetting));

        when(expenseRepository.findByDepartmentNameAndFinanceDecisionAndFinanceDecisionDateBetween(
                eq("Engineering"), eq(DecisionType.APPROVED), any(), any()))
                .thenReturn(List.of());

        when(validationFactory.getStrategy(SpendingType.DAILY)).thenReturn(dailyStrategy);
        when(validationFactory.getStrategy(SpendingType.MONTHLY)).thenReturn(monthlyStrategy);

        // When
        expenseProcessor.process(testExpense);

        // Then
        verify(validationFactory, times(1)).getStrategy(SpendingType.DAILY);
        verify(validationFactory, times(1)).getStrategy(SpendingType.MONTHLY);
    }

    @Test
    @DisplayName("Should publish alerts when daily budget is exceeded")
    void shouldPublishAlerts_WhenDailyBudgetExceeded() {
        // Given
        testExpense.setAmount(new BigDecimal("2000")); // Exceeds daily budget (1000)

        when(spendingSettingRepository.findByIdDepartmentNameAndIdCategory(
                "Engineering", ExpenseCategory.TRAVEL))
                .thenReturn(List.of(dailySetting));

        when(expenseRepository.findByDepartmentNameAndFinanceDecisionAndFinanceDecisionDateBetween(
                eq("Engineering"), eq(DecisionType.APPROVED), any(), any()))
                .thenReturn(List.of());

        when(validationFactory.getStrategy(SpendingType.DAILY)).thenReturn(dailyStrategy);

        // When
        expenseProcessor.process(testExpense);

        // Then
        verify(eventPublisher, times(1)).publishEvent(alertsCaptor.capture());
        List<Alert> alerts = alertsCaptor.getValue();

        assertThat(alerts).hasSizeGreaterThanOrEqualTo(1);
        assertThat(alerts.get(0).getMessage()).contains("exceeds");
    }

    @Test
    @DisplayName("Should publish department budget alert when department budget is exceeded")
    void shouldPublishDepartmentBudgetAlert_WhenExceeded() {
        // Given
        department.setMonthlyBudget(new BigDecimal("1000"));

        Expense approvedExpense = createApprovedExpense(new BigDecimal("800"), 10);

        when(spendingSettingRepository.findByIdDepartmentNameAndIdCategory(
                "Engineering", ExpenseCategory.TRAVEL))
                .thenReturn(List.of(dailySetting));

        when(expenseRepository.findByDepartmentNameAndFinanceDecisionAndFinanceDecisionDateBetween(
                eq("Engineering"), eq(DecisionType.APPROVED), any(), any()))
                .thenReturn(List.of(approvedExpense));

        when(validationFactory.getStrategy(SpendingType.DAILY)).thenReturn(dailyStrategy);

        testExpense.setAmount(new BigDecimal("300")); // 800 + 300 = 1100 > 1000

        // When
        expenseProcessor.process(testExpense);

        // Then
        verify(eventPublisher, times(1)).publishEvent(alertsCaptor.capture());
        List<Alert> alerts = alertsCaptor.getValue();

        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getMessage()).contains("exceeds the available department budget");
    }

    @Test
    @DisplayName("Should not publish alerts when all validations pass")
    void shouldNotPublishAlerts_WhenAllValidationsPass() {
        // Given
        when(spendingSettingRepository.findByIdDepartmentNameAndIdCategory(
                "Engineering", ExpenseCategory.TRAVEL))
                .thenReturn(List.of(dailySetting, monthlySetting));

        when(expenseRepository.findByDepartmentNameAndFinanceDecisionAndFinanceDecisionDateBetween(
                eq("Engineering"), eq(DecisionType.APPROVED), any(), any()))
                .thenReturn(List.of());

        when(validationFactory.getStrategy(SpendingType.DAILY)).thenReturn(dailyStrategy);
        when(validationFactory.getStrategy(SpendingType.MONTHLY)).thenReturn(monthlyStrategy);

        testExpense.setAmount(new BigDecimal("500")); // Within all budgets

        // When
        expenseProcessor.process(testExpense);

        // Then
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Should handle exchange rates when validating department budget")
    void shouldHandleExchangeRates_WhenValidatingDepartmentBudget() {
        // Given
        department.setMonthlyBudget(new BigDecimal("1000"));

        Expense approvedExpense = createApprovedExpense(new BigDecimal("100"), 10);
        approvedExpense.setExchangeRate(new BigDecimal("5.0")); // 100 * 5 = 500

        when(spendingSettingRepository.findByIdDepartmentNameAndIdCategory(
                "Engineering", ExpenseCategory.TRAVEL))
                .thenReturn(List.of(dailySetting));

        when(expenseRepository.findByDepartmentNameAndFinanceDecisionAndFinanceDecisionDateBetween(
                eq("Engineering"), eq(DecisionType.APPROVED), any(), any()))
                .thenReturn(List.of(approvedExpense));

        when(validationFactory.getStrategy(SpendingType.DAILY)).thenReturn(dailyStrategy);

        testExpense.setAmount(new BigDecimal("600"));
        testExpense.setExchangeRate(BigDecimal.ONE); // 500 + 600 = 1100 > 1000

        // When
        expenseProcessor.process(testExpense);

        // Then
        verify(eventPublisher, times(1)).publishEvent(alertsCaptor.capture());
        List<Alert> alerts = alertsCaptor.getValue();

        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getMessage()).contains("department budget");
    }

    @Test
    @DisplayName("Should query expenses for correct month interval")
    void shouldQueryExpenses_ForCorrectMonthInterval() {
        // Given
        when(spendingSettingRepository.findByIdDepartmentNameAndIdCategory(
                "Engineering", ExpenseCategory.TRAVEL))
                .thenReturn(List.of(dailySetting));

        when(expenseRepository.findByDepartmentNameAndFinanceDecisionAndFinanceDecisionDateBetween(
                eq("Engineering"), eq(DecisionType.APPROVED), any(), any()))
                .thenReturn(List.of());

        when(validationFactory.getStrategy(SpendingType.DAILY)).thenReturn(dailyStrategy);

        ArgumentCaptor<OffsetDateTime> beginCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);
        ArgumentCaptor<OffsetDateTime> endCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);

        // When
        expenseProcessor.process(testExpense);

        // Then
        verify(expenseRepository).findByDepartmentNameAndFinanceDecisionAndFinanceDecisionDateBetween(
                eq("Engineering"),
                eq(DecisionType.APPROVED),
                beginCaptor.capture(),
                endCaptor.capture()
        );

        // Expense date is 2025-01-15
        assertThat(beginCaptor.getValue().toLocalDate().getDayOfMonth()).isEqualTo(1);
        assertThat(endCaptor.getValue().toLocalDate().getDayOfMonth()).isEqualTo(31);
        assertThat(beginCaptor.getValue().toLocalDate().getMonthValue()).isEqualTo(1);
        assertThat(endCaptor.getValue().toLocalDate().getMonthValue()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should publish combined alerts from daily validation and department budget")
    void shouldPublishCombinedAlerts_FromDailyAndDepartment() {
        // Given
        department.setMonthlyBudget(new BigDecimal("2000"));
        testExpense.setAmount(new BigDecimal("2000")); // Exceeds daily budget

        Expense approvedExpense = createApprovedExpense(new BigDecimal("1000"), 10);

        when(spendingSettingRepository.findByIdDepartmentNameAndIdCategory(
                "Engineering", ExpenseCategory.TRAVEL))
                .thenReturn(List.of(dailySetting));

        when(expenseRepository.findByDepartmentNameAndFinanceDecisionAndFinanceDecisionDateBetween(
                eq("Engineering"), eq(DecisionType.APPROVED), any(), any()))
                .thenReturn(List.of(approvedExpense));

        when(validationFactory.getStrategy(SpendingType.DAILY)).thenReturn(dailyStrategy);

        // When
        expenseProcessor.process(testExpense);

        // Then
        verify(eventPublisher, times(1)).publishEvent(alertsCaptor.capture());
        List<Alert> alerts = alertsCaptor.getValue();

        // Should have alerts from both daily validation AND department budget
        assertThat(alerts.size()).isGreaterThanOrEqualTo(2);
        assertThat(alerts).anyMatch(a -> a.getMessage().contains("daily budget"));
        assertThat(alerts).anyMatch(a -> a.getMessage().contains("department budget"));
    }

    @Test
    @DisplayName("Should handle null strategy from factory gracefully")
    void shouldHandleNullStrategy_Gracefully() {
        // Given
        when(spendingSettingRepository.findByIdDepartmentNameAndIdCategory(
                "Engineering", ExpenseCategory.TRAVEL))
                .thenReturn(List.of(dailySetting));

        when(expenseRepository.findByDepartmentNameAndFinanceDecisionAndFinanceDecisionDateBetween(
                eq("Engineering"), eq(DecisionType.APPROVED), any(), any()))
                .thenReturn(List.of());

        when(validationFactory.getStrategy(SpendingType.DAILY)).thenReturn(null);

        // When
        expenseProcessor.process(testExpense);

        // Then - Should not throw exception, should complete successfully
        verify(eventPublisher, never()).publishEvent(any());
    }

    // Helper method
    private Expense createApprovedExpense(BigDecimal amount, int dayOfMonth) {
        Expense expense = new Expense();
        expense.setId(UUID.randomUUID());
        expense.setEmployee(employee);
        expense.setDepartment(department);
        expense.setAmount(amount);
        expense.setCurrency(CurrencyCode.USD);
        expense.setExchangeRate(BigDecimal.ONE);
        expense.setDate(OffsetDateTime.of(2025, 1, dayOfMonth, 10, 0, 0, 0, ZoneOffset.UTC));
        expense.setCategory(ExpenseCategory.TRAVEL);
        expense.setFinanceDecision(DecisionType.APPROVED);
        expense.setFinanceDecisionDate(OffsetDateTime.now(ZoneOffset.UTC));
        expense.setReceiptUrl("https://receipts.example.com/receipt-" + UUID.randomUUID() + ".pdf");
        return expense;
    }
}