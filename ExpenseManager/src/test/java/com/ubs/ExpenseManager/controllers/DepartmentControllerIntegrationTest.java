package com.ubs.ExpenseManager.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import com.ubs.ExpenseManager.usecases.employee.dto.EmployeeRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.UnsupportedEncodingException;
import java.util.Optional;
import java.util.UUID;


@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
public class DepartmentControllerIntegrationTest {

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
    private DepartmentRepository repository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    EmployeeUseCase employeeUseCase;

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
            EmployeeRequest loginEmployee = new EmployeeRequest("Carlos", "carlos@ubs.com", originalManager.getId(), "123", "SYSTEM", "QA expert", Role.ADMIN);
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
    @DisplayName("CreateDepartment")
    void shouldCreateDepartment() throws Exception{

        Department new_department = new Department("Sales", CurrencyCode.EUR);
        String json = objectMapper.writeValueAsString(new_department);
        mockMvc.perform(post("/api/departments")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer "+getTestToken())
                .content(json))
                .andDo(print())
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @DisplayName("CreateInvalidFormat")
    void shouldNotCreateEmptyDepartment() throws Exception{

        Department ghost_department = new Department();
        String json = objectMapper.writeValueAsString(ghost_department);

        mockMvc.perform(post("/api/departments")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer "+getTestToken())
                .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    @DisplayName("CreateDuplicityConflict")
    void shouldReturnDuplicityError() throws Exception{

        repository.save(new Department("RH", CurrencyCode.BRL));

        Department duplicated_department = new Department("RH", CurrencyCode.EUR);
        String json = objectMapper.writeValueAsString(duplicated_department);

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+getTestToken())
                        .content(json))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    //GET RELATED
    @Test
    @Transactional
    @DisplayName("ReturnDepartmentByID")
    void shouldReturnDepartmentById() throws Exception {

        repository.save(new Department("RH", CurrencyCode.BRL));
        repository.save(new Department("HR", CurrencyCode.USD));

        System.out.println("List item 1/2: Starting test");
        mockMvc.perform(get("/api/departments/RH")
                        .header("Authorization", "Bearer "+getTestToken()))
                .andExpect(jsonPath("$.name").value("RH"))
                .andExpect(jsonPath("$.currency").value("BRL"))
                .andExpect(status().isOk());

        System.out.println("List item 1/2: OK");

        System.out.println("List item 2/2: Starting test");
        mockMvc.perform(get("/api/departments/HR")
                        .header("Authorization", "Bearer "+getTestToken()))
                .andExpect(jsonPath("$.name").value("HR"))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(status().isOk());
        System.out.println("List item 2/2: OK");
    }

    @Test
    @DisplayName("ReturnForNotFound")
    void shouldReturnNotFoundForInvalidId() throws Exception {
        mockMvc.perform(get("/api/departments/NonExistent")
                        .header("Authorization", "Bearer "+getTestToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("FindAllDepartments")
    void shouldReturnAllDepartments() throws Exception {

        repository.save(new Department("RH", CurrencyCode.BRL));
        repository.save(new Department("HR", CurrencyCode.USD));

        MvcResult result = mockMvc.perform(get("/api/departments")
                .header("Authorization", "Bearer "+getTestToken()))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        assertEquals( "[{\"name\":\"SYSTEM\",\"currency\":\"BRL\",\"monthlyBudget\":50000.00},{\"name\":\"RH\",\"currency\":\"BRL\",\"monthlyBudget\":0.00},{\"name\":\"HR\",\"currency\":\"USD\",\"monthlyBudget\":0.00}]",responseBody, "Response does not match expected result, or not all departments were listed.");
    }

    //PUT RELATED
    @Test
    @Transactional
    @DisplayName("UpdateDepartmentData")
    void shouldUpdateDepartment() throws Exception{

        repository.save(new Department("RH", CurrencyCode.BRL));

        String request = """
                {
                    "currency": "EUR",
                    "monthlyBudget": 10000
                }
                """;

        mockMvc.perform(put("/api/departments/RH")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+getTestToken())
                        .content(request))
                        .andDo(print())
                        .andExpect(status().is2xxSuccessful());

        System.out.println("\nGet with new data:");
        mockMvc.perform(get("/api/departments/RH")
                        .header("Authorization", "Bearer "+getTestToken()))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.currency").value("EUR"));
    }

    //PATCH RELATED
    @Test
    @Transactional
    @DisplayName("RenameDepartment")
    void shouldRenameDepartment() throws Exception{

        repository.save(new Department("RH", CurrencyCode.BRL));

        String request = """
                {
                "newName": "RecursosHumanos"
                }
                """;

        mockMvc.perform(patch("/api/departments/RH/name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+getTestToken())
                        .content(request))
                        .andDo(print())
                        .andExpect(status().is2xxSuccessful());

        System.out.println("\nGet with new name:");
        mockMvc.perform(get("/api/departments/RecursosHumanos")
                        .header("Authorization", "Bearer "+getTestToken()))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.name").value("RecursosHumanos"));
    }

    //DELETE RELATED
    @Test
    @Transactional
    @DisplayName("DeleteDepartment")
    void shouldDeleteDepartment() throws Exception{

        repository.save(new Department("RH", CurrencyCode.BRL));
        repository.save(new Department("HR", CurrencyCode.USD));

        System.out.println("List item 1/2: Starting test");
        mockMvc.perform(delete("/api/departments/RH").header("Authorization", "Bearer "+getTestToken()))
                .andExpect(status().is2xxSuccessful());
        System.out.println("List item 1/2: OK");

        System.out.println("List item 2/2: Starting test");
        mockMvc.perform(delete("/api/departments/HR").header("Authorization", "Bearer "+getTestToken()))
                .andExpect(status().is2xxSuccessful());
        System.out.println("List item 2/2: OK");
    }

    @Test
    @DisplayName("DeleteNonExistentDepartment")
    void shouldNotDeleteNonExistentDepartment() throws Exception {

        System.out.println("TOken: "+getTestToken());
        mockMvc.perform(delete("/api/departments/RH").header("Authorization", "Bearer "+getTestToken()))
                .andExpect(status().isNotFound())
                .andDo(print());
    }


}
