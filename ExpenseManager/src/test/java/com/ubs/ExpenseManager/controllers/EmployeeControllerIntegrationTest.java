package com.ubs.ExpenseManager.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.ubs.ExpenseManager.entities.department.Department;
import com.ubs.ExpenseManager.entities.department.enums.CurrencyCode;
import com.ubs.ExpenseManager.entities.department.repository.DepartmentRepository;
import com.ubs.ExpenseManager.entities.employee.Employee;
import com.ubs.ExpenseManager.entities.employee.enums.Role;
import com.ubs.ExpenseManager.entities.employee.repository.EmployeeRepository;
import com.ubs.ExpenseManager.usecases.auth.dto.AuthenticationRequest;
import com.ubs.ExpenseManager.usecases.employee.EmployeeUseCase;
import com.ubs.ExpenseManager.usecases.employee.dto.CreateEmployeeRequest;
import com.ubs.ExpenseManager.usecases.employee.dto.UpdateEmployeeRequest;
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
import java.util.Optional;
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
public class EmployeeControllerIntegrationTest {

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

    @Autowired
    private EmployeeUseCase employeeUseCase;

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("server.port", () -> "0");
    }

    String getTestToken() throws Exception{
        Employee originalManager = employeeRepository.findById(UUID.fromString("22222222-2222-2222-2222-222222222222"))
                .orElseThrow(() -> new RuntimeException("Original manager not found!"));

        Optional<Employee> existing = employeeRepository.findByEmail("carlos@ubs.com");

        if (existing.isEmpty()) {
            CreateEmployeeRequest loginEmployee = new CreateEmployeeRequest("Carlos", "carlos@ubs.com", originalManager.getId(), "123", "SYSTEM", "QA expert", Role.ADMIN);
            employeeUseCase.create(loginEmployee);
        }

        AuthenticationRequest loginRequest = new AuthenticationRequest("carlos@ubs.com", "123");
        String loginJson = objectMapper.writeValueAsString(loginRequest);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        String loginResponse = loginResult.getResponse().getContentAsString();

        return JsonPath.read(loginResponse, "$.token");
    }

    //POST RELATED TESTS
    @Test
    @Transactional
    @DisplayName("CreateEmployee")
    void shouldCreateEmployee() throws Exception {
        Department department = departmentRepository.save(new Department("HR", CurrencyCode.USD));

        Employee manager = employeeRepository.findById(UUID.fromString("22222222-2222-2222-2222-222222222222"))
                .orElseThrow(() -> new RuntimeException("Manager não encontrado"));

        CreateEmployeeRequest request = new CreateEmployeeRequest("Bobert", "bob@ubs.com", manager.getId(), "123", department.getName(), "Analista", Role.EMPLOYEE);
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+getTestToken())
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
        CreateEmployeeRequest noPositionRequest = new CreateEmployeeRequest("Bobert", "bob@ubs.com", manager.getId(), "123", department.getName(), "", Role.EMPLOYEE);
        String noPositioonJson = objectMapper.writeValueAsString(noPositionRequest);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+getTestToken())
                        .content(noPositioonJson))
                .andExpect(status().isBadRequest());

        // No password
        CreateEmployeeRequest noPasswordRequest = new CreateEmployeeRequest("Bobert", "bob@ubs.com", manager.getId(), "", department.getName(), "Intern", Role.EMPLOYEE);
        String noPasswordJson = objectMapper.writeValueAsString(noPasswordRequest);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+getTestToken())
                        .content(noPasswordJson))
                .andExpect(status().isBadRequest());

        // Empty constructor
        CreateEmployeeRequest emptyConstructor = new CreateEmployeeRequest("Bobert", "bob@ubs.com", manager.getId(), "", department.getName(), "Intern", Role.EMPLOYEE);
        String emptyConstructorJson = objectMapper.writeValueAsString(emptyConstructor);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+getTestToken())
                        .content(emptyConstructorJson))
                .andExpect(status().isBadRequest());

        // No name
        CreateEmployeeRequest noNameRequest = new CreateEmployeeRequest("", "bob@ubs.com", manager.getId(), "123", department.getName(), "Intern", Role.EMPLOYEE);
        String noNameJson = objectMapper.writeValueAsString(noNameRequest);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+getTestToken())
                        .content(noNameJson))
                .andExpect(status().isBadRequest());

        // No email
        CreateEmployeeRequest noEmailRequest = new CreateEmployeeRequest("Bobert", "", manager.getId(), "123", department.getName(), "Intern", Role.EMPLOYEE);
        String noEmailJson = objectMapper.writeValueAsString(noEmailRequest);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+getTestToken())
                        .content(noEmailJson))
                .andDo(print())
                .andExpect(status().isBadRequest());

    }


    //PUT RELATED TESTS
    @Test
    @Transactional
    @DisplayName("UpdateEmployees")
    void shouldUpdateEmployee() throws Exception{
        Employee Manager = employeeRepository.findById(UUID.fromString("22222222-2222-2222-2222-222222222222"))
                .orElseThrow(() -> new RuntimeException("Original manager not found!"));

        // Addition of Employee
        CreateEmployeeRequest request = new CreateEmployeeRequest("Saulo", "SalesEmployee@ubs.com", Manager.getId(), "123", "SYSTEM", "Intern", Role.EMPLOYEE);
        String json = objectMapper.writeValueAsString(request);
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer "+getTestToken())
                .content(json));

        Employee newEmployee = employeeRepository.findByEmail("SalesEmployee@ubs.com")
                .orElseThrow(() -> new RuntimeException("New manager not found!"));

        // Update employee
        UpdateEmployeeRequest UpdateRequest = new UpdateEmployeeRequest(newEmployee.getName(), "SalesManager@ubs.com", newEmployee.getManager().getId(), newEmployee.getDepartment().getName(), "Manager", newEmployee.getActive());
        String updateJson = objectMapper.writeValueAsString(UpdateRequest);

        mockMvc.perform(put("/api/employees/"+newEmployee.getId())
                        .header("Authorization", "Bearer "+getTestToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @Transactional
    @DisplayName("UpdateEmployeeToSameState")
    void shouldNotUpdateEmployee() throws Exception{
        Employee Manager = employeeRepository.findById(UUID.fromString("22222222-2222-2222-2222-222222222222"))
                .orElseThrow(() -> new RuntimeException("Original manager not found!"));

        // Addition of Employee
        CreateEmployeeRequest request = new CreateEmployeeRequest("Saulo", "SalesEmployee@ubs.com", Manager.getId(), "123", "SYSTEM", "Intern", Role.EMPLOYEE);
        String json = objectMapper.writeValueAsString(request);
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer "+getTestToken())
                .content(json));

        Employee newEmployee = employeeRepository.findByEmail("SalesEmployee@ubs.com")
                .orElseThrow(() -> new RuntimeException("New manager not found!"));

        // Update employee
        UpdateEmployeeRequest UpdateRequestForSame = new UpdateEmployeeRequest(newEmployee.getName(), newEmployee.getEmail(), newEmployee.getManager().getId(), newEmployee.getDepartment().getName(), newEmployee.getPosition(), newEmployee.getActive());
        String updateJson = objectMapper.writeValueAsString(UpdateRequestForSame);

        mockMvc.perform(put("/api/employees/"+newEmployee.getId())
                        .header("Authorization", "Bearer "+getTestToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // GET RELATED TESTS
    @Test
    @DisplayName("FindAllEmployees")
    void shouldReturnAllEmployees() throws Exception {
        mockMvc.perform(get("/api/employees")
                        .header("Authorization", "Bearer "+getTestToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[*].name",
                        containsInAnyOrder("Sergio ADM", "Silvio Mann", "Carlos")))
                .andReturn();

    }


    @Test
    @DisplayName("FindEmployeeById")
    void shouldReturnEmployeeById() throws Exception {
        System.out.println("List item 1/2: Starting test");
        mockMvc.perform(get("/api/employees/22222222-2222-2222-2222-222222222222")
                        .header("Authorization", "Bearer "+getTestToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("22222222-2222-2222-2222-222222222222"))
                .andExpect(jsonPath("$.name").value("Silvio Mann"))
                .andReturn();
        System.out.println("List item 1/2: OK");

        System.out.println("List item 2/2: Starting test");
        mockMvc.perform(get("/api/employees/11111111-1111-1111-1111-111111111111")
                        .header("Authorization", "Bearer "+getTestToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("11111111-1111-1111-1111-111111111111"))
                .andExpect(jsonPath("$.name").value("Sergio ADM"))
                .andReturn();
        System.out.println("List item 2/2: OK");
    }

    @Test
    @DisplayName("ReturnNotFoundEmployeeId")
    void shouldReturnNonExistentEmployee() throws Exception {
        mockMvc.perform(get("/api/employees/22222222-2111-1111-1111-222222222222")
                        .header("Authorization", "Bearer "+getTestToken()))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    @DisplayName("ReturnNotFoundEmployeeId")
    void shouldReturnInvalidEmployeeId() throws Exception {
        mockMvc.perform(get("/api/employees/Invalid-UUID-1111-1111-222222222222")
                        .header("Authorization", "Bearer "+getTestToken()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();
    }


    @Test
    @DisplayName("FindAllManagers")
    void shouldReturnAllManagers() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/employees/managers")
                        .header("Authorization", "Bearer "+getTestToken()))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();

        assertEquals( "[{\"id\":\"22222222-2222-2222-2222-222222222222\",\"name\":\"Silvio Mann\",\"email\":\"manager@empresa.com\",\"departmentName\":\"SYSTEM\",\"role\":\"MANAGER\",\"active\":true}]", responseBody, "Response does not match expected result, or not all managers were listed.");
    }

    // PATCH RELATED TESTS
    @Test
    @Transactional
    @DisplayName("DeactivateAndActivateEmployee")
    void shouldActivateAndDeactivateEmployeeById() throws Exception {
        // Create employee
        Department department = departmentRepository.save(new Department("HR", CurrencyCode.USD));

        Employee manager = employeeRepository.findById(UUID.fromString("22222222-2222-2222-2222-222222222222"))
                .orElseThrow(() -> new RuntimeException("Manager não encontrado"));

        CreateEmployeeRequest request = new CreateEmployeeRequest("John NoAccess", "jna@ubs.com", manager.getId(), "123", department.getName(), "Analista", Role.EMPLOYEE);
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+getTestToken())
                        .content(json))
                .andDo(print())
                .andExpect(status().is2xxSuccessful());

        // Deactivate employee
        Employee newEmployee = employeeRepository.findByEmail("jna@ubs.com")
                .orElseThrow(() -> new RuntimeException("Employee not found!"));

        mockMvc.perform(patch("/api/employees/"+newEmployee.getId()+"/deactivate")
                        .header("Authorization", "Bearer "+getTestToken()))
                .andExpect(status().is2xxSuccessful());

        Employee deactivatedEmployee = employeeRepository.findByEmail("jna@ubs.com")
                .orElseThrow(() -> new RuntimeException("Employee not found!"));

        assertEquals(false, deactivatedEmployee.getActive(), "Employee was not deactivated.");

        // Activate employee
        mockMvc.perform(patch("/api/employees/"+deactivatedEmployee.getId()+"/activate")
                        .header("Authorization", "Bearer "+getTestToken()))
                .andExpect(status().is2xxSuccessful());

        assertEquals(true, deactivatedEmployee.getActive(), "Employee was not activated.");
    }

    @Test
    @Transactional
    @DisplayName("ActivateAnActiveEmployee")
    void shouldNotActivateAnActiveEmployee() throws Exception {
        // Create employee
        Department department = departmentRepository.save(new Department("HR", CurrencyCode.USD));

        Employee manager = employeeRepository.findById(UUID.fromString("22222222-2222-2222-2222-222222222222"))
                .orElseThrow(() -> new RuntimeException("Manager não encontrado"));

        CreateEmployeeRequest request = new CreateEmployeeRequest("John NoAccess", "jna@ubs.com", manager.getId(), "123", department.getName(), "Analista", Role.EMPLOYEE);
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+getTestToken())
                        .content(json))
                .andDo(print())
                .andExpect(status().is2xxSuccessful());

        // Deactivate employee
        Employee newEmployee = employeeRepository.findByEmail("jna@ubs.com")
                .orElseThrow(() -> new RuntimeException("Employee not found!"));

        mockMvc.perform(patch("/api/employees/"+newEmployee.getId()+"/activate")
                        .header("Authorization", "Bearer "+getTestToken()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    @DisplayName("DeactivateAnDeactiveEmployee")
    void shouldNotDeactivateAnDeactiveEmployee() throws Exception {
        // Create employee
        Department department = departmentRepository.save(new Department("HR", CurrencyCode.USD));

        Employee manager = employeeRepository.findById(UUID.fromString("22222222-2222-2222-2222-222222222222"))
                .orElseThrow(() -> new RuntimeException("Manager não encontrado"));

        CreateEmployeeRequest request = new CreateEmployeeRequest("John NoAccess", "jna@ubs.com", manager.getId(), "123", department.getName(), "Analista", Role.EMPLOYEE);
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + getTestToken())
                        .content(json))
                .andDo(print())
                .andExpect(status().is2xxSuccessful());

        // Deactivate employee
        Employee newEmployee = employeeRepository.findByEmail("jna@ubs.com")
                .orElseThrow(() -> new RuntimeException("Employee not found!"));

        mockMvc.perform(patch("/api/employees/" + newEmployee.getId() + "/deactivate")
                        .header("Authorization", "Bearer " + getTestToken()))
                .andExpect(status().is2xxSuccessful());

        Employee deactivatedEmployee = employeeRepository.findByEmail("jna@ubs.com")
                .orElseThrow(() -> new RuntimeException("Employee not found!"));

        mockMvc.perform(patch("/api/employees/" + deactivatedEmployee.getId() + "/deactivate")
                        .header("Authorization", "Bearer " + getTestToken()))
                .andExpect(status().isBadRequest());
    }
}