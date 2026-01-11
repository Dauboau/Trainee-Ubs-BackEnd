package com.ubs.ExpenseManager.usecases.expense;

import com.ubs.ExpenseManager.entities.alert.Alert;
import com.ubs.ExpenseManager.entities.alert.enums.AlertType;
import com.ubs.ExpenseManager.entities.alert.repository.AlertRepository;
import com.ubs.ExpenseManager.entities.department.Department;
import com.ubs.ExpenseManager.entities.department.SpendingSetting;
import com.ubs.ExpenseManager.entities.department.SpendingSettingId;
import com.ubs.ExpenseManager.entities.department.enums.CurrencyCode;
import com.ubs.ExpenseManager.entities.department.enums.SpendingType;
import com.ubs.ExpenseManager.entities.department.repository.DepartmentRepository;
import com.ubs.ExpenseManager.entities.department.repository.SpendingSettingRepository;
import com.ubs.ExpenseManager.entities.employee.Employee;
import com.ubs.ExpenseManager.entities.employee.enums.Role;
import com.ubs.ExpenseManager.entities.employee.repository.EmployeeRepository;
import com.ubs.ExpenseManager.entities.expense.enums.ExpenseCategory;
import com.ubs.ExpenseManager.gateways.CurrencyExchangeGateway;
import com.ubs.ExpenseManager.gateways.ImageStorageGateway;
import com.ubs.ExpenseManager.usecases.expense.dto.ExpenseRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ExpenseUseCaseIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpassword");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("FIREBASE_CREDENTIALS_BASE64", () -> "dummy");
    }
    @Autowired
    private ExpenseUseCase expenseUseCase;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private SpendingSettingRepository spendingSettingRepository;

    @Autowired
    private AlertRepository alertRepository;

    @MockBean
    private CurrencyExchangeGateway currencyExchangeGateway;

    @MockBean
    private ImageStorageGateway imageStorageGateway;

    private Employee employee;
    private Department department;

    @BeforeEach
    void setUp() {
        alertRepository.deleteAll();
        spendingSettingRepository.deleteAll();
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();

        department = new Department();
        department.setName("IT");
        department.setMonthlyBudget(new BigDecimal("10000"));
        department.setCurrency(CurrencyCode.BRL);
        departmentRepository.save(department);

        employee = new Employee();
        employee.setRole(Role.EMPLOYEE);
        employee.setDepartment(department);
        employee.setName("John Doe");
        employee.setEmail("john.doe@example.com");
        employee.setPassword("password");
        employeeRepository.save(employee);

        when(currencyExchangeGateway.getExchangeRate(any(), any())).thenReturn(BigDecimal.ONE);
        when(imageStorageGateway.uploadImage(any(), any())).thenReturn("http://image.url");
    }

    @Test
    void testCreateExpenseExceedsDailyLimit() {
        // Arrange
        SpendingSetting dailySetting = new SpendingSetting();
        dailySetting.setId(new SpendingSettingId(department.getName(), ExpenseCategory.MEAL, SpendingType.DAILY));
        dailySetting.setDepartment(department);
        dailySetting.setBudget(new BigDecimal("50"));
        spendingSettingRepository.save(dailySetting);

        MockMultipartFile mockFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[0]);
        ExpenseRequest request = new ExpenseRequest(
                employee.getId(),
                department.getName(),
                "Lunch",
                new BigDecimal("60"),
                CurrencyCode.BRL,
                ExpenseCategory.MEAL,
                OffsetDateTime.now(),
                mockFile
        );

        // Act
        expenseUseCase.create(request);

        // Assert
        List<Alert> alerts = alertRepository.findAll();
        assertFalse(alerts.isEmpty());
        assertEquals(AlertType.CATEGORY_DAILY, alerts.get(0).getType());
    }

    @Test
    void testCreateExpenseExceedsMonthlyLimit() {
        // Arrange
        SpendingSetting monthlySetting = new SpendingSetting();
        monthlySetting.setId(new SpendingSettingId(department.getName(), ExpenseCategory.TRAVEL, SpendingType.MONTHLY));
        monthlySetting.setDepartment(department);
        monthlySetting.setBudget(new BigDecimal("500"));
        spendingSettingRepository.save(monthlySetting);

        MockMultipartFile mockFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[0]);
        ExpenseRequest request = new ExpenseRequest(
                employee.getId(),
                department.getName(),
                "Business Trip",
                new BigDecimal("600"),
                CurrencyCode.BRL,
                ExpenseCategory.TRAVEL,
                OffsetDateTime.now(),
                mockFile
        );

        // Act
        expenseUseCase.create(request);

        // Assert
        List<Alert> alerts = alertRepository.findAll();
        assertFalse(alerts.isEmpty());
        assertEquals(AlertType.DEPARTMENT_MONTHLY, alerts.get(0).getType());
    }

    @Test
    void testCreateExpenseWithinLimits() {
        // Arrange
        SpendingSetting dailySetting = new SpendingSetting();
        dailySetting.setId(new SpendingSettingId(department.getName(), ExpenseCategory.MEAL, SpendingType.DAILY));
        dailySetting.setDepartment(department);
        dailySetting.setBudget(new BigDecimal("100"));
        spendingSettingRepository.save(dailySetting);

        SpendingSetting monthlySetting = new SpendingSetting();
        monthlySetting.setId(new SpendingSettingId(department.getName(), ExpenseCategory.MEAL, SpendingType.MONTHLY));
        monthlySetting.setDepartment(department);
        monthlySetting.setBudget(new BigDecimal("1000"));
        spendingSettingRepository.save(monthlySetting);

        MockMultipartFile mockFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[0]);
        ExpenseRequest request = new ExpenseRequest(
                employee.getId(),
                department.getName(),
                "Lunch",
                new BigDecimal("50"),
                CurrencyCode.BRL,
                ExpenseCategory.MEAL,
                OffsetDateTime.now(),
                mockFile
        );

        // Act
        expenseUseCase.create(request);

        // Assert
        List<Alert> alerts = alertRepository.findAll();
        assertEquals(0, alerts.size());
    }
}
