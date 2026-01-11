package com.ubs.ExpenseManager.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubs.ExpenseManager.entities.department.Department;
import com.ubs.ExpenseManager.entities.department.enums.CurrencyCode;
import com.ubs.ExpenseManager.entities.department.repository.DepartmentRepository;
import com.ubs.ExpenseManager.entities.employee.Employee;
import com.ubs.ExpenseManager.entities.employee.enums.Role;
import com.ubs.ExpenseManager.entities.employee.repository.EmployeeRepository;
import com.ubs.ExpenseManager.usecases.auth.dto.AuthenticationRequest;
import com.ubs.ExpenseManager.usecases.employee.EmployeeUseCase;
import com.ubs.ExpenseManager.usecases.employee.dto.EmployeeRequest;
import com.ubs.ExpenseManager.usecases.employee.dto.ManagerReallocationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class EmployeeControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpassword");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("server.port", () -> "0");
    }

    //POST RELATED TESTS
    @Test
    @Transactional
    @DisplayName("CreateEmployee")
    void shouldCreateEmployee() throws Exception {
        Department department = departmentRepository.save(new Department("HR", CurrencyCode.USD));

        Employee manager = employeeRepository.findById(UUID.fromString("22222222-2222-2222-2222-222222222222"))
                .orElseThrow(() -> new RuntimeException("Manager não encontrado"));

        EmployeeRequest request = new EmployeeRequest("Bobert", "bob@ubs.com", manager.getId(), "123", department.getName(), "Analista", Role.EMPLOYEE);
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().is2xxSuccessful());

    }

    @Test
    @DisplayName("CreateInvalidFormat")
    void shouldNotCreateInvalidEmployee() throws Exception {
        Department department = departmentRepository.save(new Department("HR", CurrencyCode.USD));

        Employee manager = employeeRepository.findById(UUID.fromString("22222222-2222-2222-2222-222222222222"))
                .orElseThrow(() -> new RuntimeException("Manager não encontrado"));

        // No position
        EmployeeRequest noPositionRequest = new EmployeeRequest("Bobert", "bob@ubs.com", manager.getId(), "123", department.getName(), "", Role.EMPLOYEE);
        String noPositioonJson = objectMapper.writeValueAsString(noPositionRequest);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(noPositioonJson))
                .andExpect(status().isBadRequest());

        // No password
        EmployeeRequest noPasswordRequest = new EmployeeRequest("Bobert", "bob@ubs.com", manager.getId(), "", department.getName(), "Intern", Role.EMPLOYEE);
        String noPasswordJson = objectMapper.writeValueAsString(noPasswordRequest);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(noPasswordJson))
                .andExpect(status().isBadRequest());

        // Empty constructor
        EmployeeRequest emptyConstructor = new EmployeeRequest("Bobert", "bob@ubs.com", manager.getId(), "", department.getName(), "Intern", Role.EMPLOYEE);
        String emptyConstructorJson = objectMapper.writeValueAsString(emptyConstructor);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emptyConstructorJson))
                .andExpect(status().isBadRequest());

        // No name
        EmployeeRequest noNameRequest = new EmployeeRequest("", "bob@ubs.com", manager.getId(), "123", department.getName(), "Intern", Role.EMPLOYEE);
        String noNameJson = objectMapper.writeValueAsString(noNameRequest);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(noNameJson))
                .andExpect(status().isBadRequest());

        // No email
        EmployeeRequest noEmailRequest = new EmployeeRequest("Bobert", "", manager.getId(), "123", department.getName(), "Intern", Role.EMPLOYEE);
        String noEmailJson = objectMapper.writeValueAsString(noEmailRequest);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(noEmailJson))
                .andDo(print())
                .andExpect(status().isBadRequest());

    }


    @Test
    @Transactional
    @DisplayName("ReallocateEmployees")
    void shouldChangeManager() throws Exception{
        Employee originalManager = employeeRepository.findById(UUID.fromString("22222222-2222-2222-2222-222222222222"))
                .orElseThrow(() -> new RuntimeException("Original manager not found!"));

        // Addition of Employee
        EmployeeRequest request = new EmployeeRequest("Saulo", "SalesManager@ubs.com", originalManager.getId(), "123", "SYSTEM", "Manager", Role.MANAGER);
        String json = objectMapper.writeValueAsString(request);
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        Employee newManager = employeeRepository.findByEmail("SalesManager@ubs.com")
                .orElseThrow(() -> new RuntimeException("New manager not found!"));

        // Change employees manager
        ManagerReallocationRequest reallocationRequest = new ManagerReallocationRequest(originalManager.getId(), newManager.getId());
        String reallocationJson = objectMapper.writeValueAsString(reallocationRequest);

        System.out.println("Before reallocating:");
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andReturn();

        List<Employee> subordinatesOfOriginalBefore = employeeRepository.findAllByManagerId(reallocationRequest.currentManagerId());
        List<Employee> subordinatesOfNewBefore = employeeRepository.findAllByManagerId(reallocationRequest.newManagerId());
        System.out.println("Subordinates of original manager:");
        for (Employee subordinate : subordinatesOfOriginalBefore) {
            System.out.println("Name: "+subordinate.getName());
        }
        System.out.println("Subordinates of new manager:");
        for (Employee subordinate : subordinatesOfNewBefore) {
            System.out.println("Name: "+subordinate.getName());
        }

        // Reallocate request
        mockMvc.perform(post("/api/employees/reallocate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reallocationJson))
                .andExpect(status().is2xxSuccessful());

        System.out.println("After reallocation");
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andReturn();

        List<Employee> subordinatesOfOriginal = employeeRepository.findAllByManagerId(reallocationRequest.currentManagerId());
        List<Employee> subordinatesOfNew = employeeRepository.findAllByManagerId(reallocationRequest.newManagerId());

        System.out.println("Subordinates of original manager:");
        for (Employee subordinate : subordinatesOfOriginal) {
            System.out.println("Name: "+subordinate.getName());
        }
        System.out.println("Subordinates of new manager:");
        for (Employee subordinate : subordinatesOfNew) {
            System.out.println("Name: "+subordinate.getName());
        }

        assertThat(subordinatesOfNew)
                .extracting(Employee::getName)
                .containsExactlyInAnyOrder(
                        "Saulo",
                        "Silvio Mann"
                );
    }

    // GET RELATED TESTS
    @Test
    @DisplayName("FindAllEmployees")
    void shouldReturnAllEmployees() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].name",
                        containsInAnyOrder("Sergio ADM", "Silvio Mann")))
                .andReturn();

    }


    @Test
    @DisplayName("FindEmployeeById")
    void shouldReturnEmployeeById() throws Exception {
        System.out.println("List item 1/2: Starting test");
        mockMvc.perform(get("/api/employees/22222222-2222-2222-2222-222222222222"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("22222222-2222-2222-2222-222222222222"))
                .andExpect(jsonPath("$.name").value("Silvio Mann"))
                .andReturn();
        System.out.println("List item 1/2: OK");

        System.out.println("List item 2/2: Starting test");
        mockMvc.perform(get("/api/employees/11111111-1111-1111-1111-111111111111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("11111111-1111-1111-1111-111111111111"))
                .andExpect(jsonPath("$.name").value("Sergio ADM"))
                .andReturn();
        System.out.println("List item 2/2: OK");
    }

    @Test
    @DisplayName("ReturnNotFoundEmployeeId")
    void shouldReturnNonExistentEmployee() throws Exception {
        mockMvc.perform(get("/api/employees/22222222-2111-1111-1111-222222222222"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    @DisplayName("ReturnNotFoundEmployeeId")
    void shouldReturnInvalidEmployeeId() throws Exception {
        mockMvc.perform(get("/api/employees/Invalid-UUID-1111-1111-222222222222"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();
    }


    @Test
    @DisplayName("FindAllManagers")
    void shouldReturnAllManagers() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/employees/managers"))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        System.out.println("REQ: "+responseBody);

        assertEquals( "[{\"id\":\"22222222-2222-2222-2222-222222222222\",\"name\":\"Silvio Mann\",\"email\":\"manager@empresa.com\",\"departmentName\":\"SYSTEM\",\"role\":\"MANAGER\"}]", responseBody, "Response does not match expected result, or not all managers were listed.");
    }

    // DELETE RELATED TESTS
    @Test
    @Transactional
    @DisplayName("DeleteEmployeeById")
    void shouldDeleteEmployeeById() throws Exception {
        mockMvc.perform(delete("/api/employees/11111111-1111-1111-1111-111111111111"))
                .andDo(print())
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @DisplayName("DeleteNonExistentEmployeeId")
    void shouldNotDeleteNonExistentEmployeeById() throws Exception {
        mockMvc.perform(delete("/api/employees/22222222-2111-1111-1111-222222222222"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}