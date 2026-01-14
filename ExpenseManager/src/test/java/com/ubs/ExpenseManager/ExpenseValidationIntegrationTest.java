package com.ubs.ExpenseManager;

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
import com.ubs.ExpenseManager.usecases.alert.AlertUseCase;
import com.ubs.ExpenseManager.usecases.expense.observer.AlertsCreatedEvent;
import com.ubs.ExpenseManager.usecases.expense.observer.ExpenseObserver;
import com.ubs.ExpenseManager.usecases.expense.strategies.DailyValidationStrategy;
import com.ubs.ExpenseManager.usecases.expense.strategies.MonthlyValidationStrategy;
import com.ubs.ExpenseManager.usecases.expense.strategies.SpendingValidationFactory;
import com.google.firebase.FirebaseApp;
import org.junit.jupiter.api.BeforeEach;
import com.google.cloud.storage.Bucket;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@Testcontainers
@DisplayName("Expense Validation Integration Tests")
class ExpenseValidationIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("expense_test_db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        // Enable Flyway for migrations
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
        registry.add("spring.flyway.baseline-on-migrate", () -> "true");

        // Set Hibernate to validate (not create) since Flyway handles schema
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");

        // Mock Firebase credentials for tests (empty base64 string)
        registry.add("FIREBASE_CREDENTIALS_BASE64", () -> "e30="); // Base64 encoded "{}"
        registry.add("FIREBASE_BUCKET", () -> "test-bucket");
        registry.add("JWT_SECRET", () -> "dGVzdC1zZWNyZXQtdGVzdC1zZWNyZXQtdGVzdC1zZWNyZXQtdGVzdC1zZWNyZXQ=");
        registry.add("JWT_EXPIRATION_TIME", () -> "3600000");
        registry.add("FIREBASE_BUCKET_ENV", () -> "test");
    }

    @Autowired
    private SpendingValidationFactory validationFactory;

    @Autowired
    private ExpenseObserver expenseObserver;

    @MockitoBean
    private AlertUseCase alertUseCase;

    @MockitoBean
    private FirebaseApp firebaseApp; // Mock Firebase to avoid initialization issues

    @MockitoBean
    private Bucket bucket;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private Department department;
    private Employee employee;
    private Expense expense;
    private SpendingSetting dailySetting;
    private SpendingSetting monthlySetting;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setName("Engineering");

        employee = new Employee();
        employee.setId(UUID.randomUUID());
        employee.setName("John Doe");
        employee.setEmail("john.doe@example.com");
        employee.setDepartment(department);

        expense = new Expense();
        expense.setId(UUID.randomUUID());
        expense.setEmployee(employee);
        expense.setDepartment(department);
        expense.setDate(OffsetDateTime.now());
        expense.setCategory(ExpenseCategory.MEAL);
        expense.setAmount(new BigDecimal("100.00"));
        expense.setCurrency(CurrencyCode.USD);
        expense.setExchangeRate(BigDecimal.ONE);

        // Create daily setting with composite key
        SpendingSettingId dailySettingId = new SpendingSettingId(
                "Engineering",
                ExpenseCategory.MEAL,
                SpendingType.DAILY
        );
        dailySetting = new SpendingSetting(dailySettingId, new BigDecimal("150.00"));

        // Create monthly setting with composite key
        SpendingSettingId monthlySettingId = new SpendingSettingId(
                "Engineering",
                ExpenseCategory.MEAL,
                SpendingType.MONTHLY
        );
        monthlySetting = new SpendingSetting(monthlySettingId, new BigDecimal("1000.00"));
    }

    @Test
    @DisplayName("Should validate daily expense within budget successfully")
    void shouldValidateDailyExpenseWithinBudget() {
        AlertsCreatedEvent event = new AlertsCreatedEvent();
        List<Expense> approvedExpenses = new ArrayList<>();

        validationFactory.getStrategy(SpendingType.DAILY)
                .validate(expense, dailySetting, approvedExpenses, new BigDecimal("100.00"), event);

        assertThat(event.getAlerts()).isEmpty();
    }

    @Test
    @DisplayName("Should create alert when daily expense exceeds budget")
    void shouldCreateAlertWhenDailyExpenseExceedsBudget() {
        expense.setAmount(new BigDecimal("200.00"));
        AlertsCreatedEvent event = new AlertsCreatedEvent();
        List<Expense> approvedExpenses = new ArrayList<>();

        validationFactory.getStrategy(SpendingType.DAILY)
                .validate(expense, dailySetting, approvedExpenses, new BigDecimal("200.00"), event);

        assertThat(event.getAlerts()).hasSize(1);
        assertThat(event.getAlerts().get(0).getType()).isEqualTo(AlertType.CATEGORY_DAILY);
    }

    @Test
    @DisplayName("Should validate monthly expense within budget successfully")
    void shouldValidateMonthlyExpenseWithinBudget() {
        AlertsCreatedEvent event = new AlertsCreatedEvent();
        List<Expense> approvedExpenses = new ArrayList<>();

        validationFactory.getStrategy(SpendingType.MONTHLY)
                .validate(expense, monthlySetting, approvedExpenses, new BigDecimal("100.00"), event);

        assertThat(event.getAlerts()).isEmpty();
    }

    @Test
    @DisplayName("Should create alert when monthly expense exceeds budget")
    void shouldCreateAlertWhenMonthlyExpenseExceedsBudget() {
        expense.setAmount(new BigDecimal("1500.00"));
        AlertsCreatedEvent event = new AlertsCreatedEvent();
        List<Expense> approvedExpenses = new ArrayList<>();

        validationFactory.getStrategy(SpendingType.MONTHLY)
                .validate(expense, monthlySetting, approvedExpenses, new BigDecimal("1500.00"), event);

        assertThat(event.getAlerts()).hasSize(1);
        assertThat(event.getAlerts().get(0).getType()).isEqualTo(AlertType.CATEGORY_MONTHLY);
    }

    @Test
    @DisplayName("Should process alerts through ExpenseObserver")
    void shouldProcessAlertsThroughObserver() {
        AlertsCreatedEvent event = new AlertsCreatedEvent();
        Alert alert = new Alert(expense, AlertType.CATEGORY_DAILY, "Test alert");
        event.add(alert);

        expenseObserver.handleAlerts(event);

        verify(alertUseCase, times(1)).create(
                eq(expense.getId()),
                eq(AlertType.CATEGORY_DAILY),
                eq("Test alert")
        );
    }

    @Test
    @DisplayName("Should handle complete validation flow with event publishing")
    void shouldHandleCompleteValidationFlow() {
        expense.setAmount(new BigDecimal("200.00"));
        AlertsCreatedEvent event = new AlertsCreatedEvent();
        List<Expense> approvedExpenses = new ArrayList<>();

        // Validate and create alerts
        validationFactory.getStrategy(SpendingType.DAILY)
                .validate(expense, dailySetting, approvedExpenses, new BigDecimal("200.00"), event);

        assertThat(event.getAlerts()).hasSize(1);

        // Publish event and verify observer handles it
        eventPublisher.publishEvent(event);

        // Give event time to process (in real scenario, this would be synchronous)
        verify(alertUseCase, timeout(1000).times(1))
                .create(any(UUID.class), eq(AlertType.CATEGORY_DAILY), anyString());
    }

    @Test
    @DisplayName("Should handle multiple validations in sequence")
    void shouldHandleMultipleValidationsInSequence() {
        List<Expense> approvedExpenses = new ArrayList<>();

        // First expense - within budget
        Expense expense1 = new Expense();
        expense1.setId(UUID.randomUUID());
        expense1.setEmployee(employee);
        expense1.setDepartment(department);
        expense1.setDate(OffsetDateTime.now());
        expense1.setCategory(ExpenseCategory.MEAL);
        expense1.setAmount(new BigDecimal("50.00"));
        expense1.setCurrency(CurrencyCode.USD);
        expense1.setExchangeRate(BigDecimal.ONE);

        AlertsCreatedEvent event1 = new AlertsCreatedEvent();
        validationFactory.getStrategy(SpendingType.DAILY)
                .validate(expense1, dailySetting, approvedExpenses, new BigDecimal("50.00"), event1);

        assertThat(event1.getAlerts()).isEmpty();
        approvedExpenses.add(expense1);

        // Second expense - should exceed remaining budget
        Expense expense2 = new Expense();
        expense2.setId(UUID.randomUUID());
        expense2.setEmployee(employee);
        expense2.setDepartment(department);
        expense2.setDate(OffsetDateTime.now());
        expense2.setCategory(ExpenseCategory.MEAL);
        expense2.setAmount(new BigDecimal("120.00"));
        expense2.setCurrency(CurrencyCode.USD);
        expense2.setExchangeRate(BigDecimal.ONE);

        AlertsCreatedEvent event2 = new AlertsCreatedEvent();
        validationFactory.getStrategy(SpendingType.DAILY)
                .validate(expense2, dailySetting, approvedExpenses, new BigDecimal("120.00"), event2);

        assertThat(event2.getAlerts()).hasSize(1);
        assertThat(event2.getAlerts().get(0).getMessage()).contains("Daily Budget Exhausted");
    }

    @Test
    @DisplayName("Should handle validation with exchange rates")
    void shouldHandleValidationWithExchangeRates() {
        Expense eurExpense = new Expense();
        eurExpense.setId(UUID.randomUUID());
        eurExpense.setEmployee(employee);
        eurExpense.setDepartment(department);
        eurExpense.setDate(OffsetDateTime.now());
        eurExpense.setCategory(ExpenseCategory.MEAL);
        eurExpense.setAmount(new BigDecimal("80.00"));
        eurExpense.setCurrency(CurrencyCode.EUR);
        eurExpense.setExchangeRate(new BigDecimal("1.1")); // EUR to USD

        AlertsCreatedEvent event = new AlertsCreatedEvent();
        List<Expense> approvedExpenses = new ArrayList<>();

        // 80 EUR * 1.1 = 88 USD, which is within 150 USD budget
        validationFactory.getStrategy(SpendingType.DAILY)
                .validate(eurExpense, dailySetting, approvedExpenses, new BigDecimal("88.00"), event);

        assertThat(event.getAlerts()).isEmpty();
    }

    @Test
    @DisplayName("Should get correct strategy from factory")
    void shouldGetCorrectStrategyFromFactory() {
        var dailyStrategy = validationFactory.getStrategy(SpendingType.DAILY);
        var monthlyStrategy = validationFactory.getStrategy(SpendingType.MONTHLY);

        assertThat(dailyStrategy).isInstanceOf(DailyValidationStrategy.class);
        assertThat(monthlyStrategy).isInstanceOf(MonthlyValidationStrategy.class);
    }
}