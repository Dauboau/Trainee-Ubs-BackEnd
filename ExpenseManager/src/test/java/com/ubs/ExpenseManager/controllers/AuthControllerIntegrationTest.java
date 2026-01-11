package com.ubs.ExpenseManager.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.ubs.ExpenseManager.entities.department.repository.DepartmentRepository;
import com.ubs.ExpenseManager.entities.employee.Employee;
import com.ubs.ExpenseManager.entities.employee.enums.Role;
import com.ubs.ExpenseManager.entities.employee.repository.EmployeeRepository;
import com.ubs.ExpenseManager.usecases.auth.dto.AuthenticationRequest;
import com.ubs.ExpenseManager.usecases.employee.EmployeeUseCase;
import com.ubs.ExpenseManager.usecases.employee.dto.EmployeeRequest;
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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.UnsupportedEncodingException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

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
    EmployeeUseCase employeeUseCase;

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("server.port", () -> "0");
    }

    public String getAdminUserLoginRequest() throws Exception {
        //
        Employee originalManager = employeeRepository.findById(UUID.fromString("22222222-2222-2222-2222-222222222222"))
                .orElseThrow(() -> new RuntimeException("Original manager not found!"));

        Optional<Employee> existing = employeeRepository.findByEmail("carlos@ubs.com");

        if (existing.isEmpty()) {
            EmployeeRequest loginEmployee = new EmployeeRequest("Carlos", "carlos@ubs.com", originalManager.getId(), "123", "SYSTEM", "QA expert", Role.ADMIN);
            employeeUseCase.create(loginEmployee);
        }

        AuthenticationRequest loginRequest = new AuthenticationRequest("carlos@ubs.com", "123");
        return objectMapper.writeValueAsString(loginRequest);

//        return String loginJson = objectMapper.writeValueAsString(loginRequest);
//
//        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(loginJson))
//                .andExpect(status().is2xxSuccessful())
//                .andReturn();
//
//        String loginResponse = loginResult.getResponse().getContentAsString();
//
//        return JsonPath.read(loginResponse, "$.token");
    }

    @Test
    @DisplayName("SuccessfulLogin")
    public void shouldAuthenticate() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getAdminUserLoginRequest()))
                .andExpect(status().is2xxSuccessful())
                .andDo(print())
                .andReturn();

//        String loginResponse = loginResult.getResponse().getContentAsString();

//        JsonPath.read(loginResponse, "$.token");
    }

    @Test
    @DisplayName("NonExistentUserLogin")
    public void shouldNotAuthenticateNonExistantUser() throws Exception {
        AuthenticationRequest loginRequest = new AuthenticationRequest("nonExistant@ubs.com", "nonExistant");
        String invalidLoginJson = objectMapper.writeValueAsString(loginRequest);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidLoginJson))
                .andExpect(status().isUnauthorized())
                .andDo(print())
                .andReturn();
    }

    @Test
    @DisplayName("WrongCredentialsLogin")
    public void shouldNotAuthenticateUnmatchingCredentials() throws Exception {
        // Calling function to guarantee creation of ADMIN user
        String correctCredentials = getAdminUserLoginRequest();

        System.out.println("List item 1/2: Starting test");
        AuthenticationRequest wrongPasswordLoginRequest = new AuthenticationRequest("carlos@ubs.com", "WrongPassword");
        String wrongPasswordLoginJson = objectMapper.writeValueAsString(wrongPasswordLoginRequest);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(wrongPasswordLoginJson))
                .andExpect(status().isUnauthorized())
                .andDo(print())
                .andReturn();
        System.out.println("List item 1/2: OK");

        System.out.println("List item 2/2: Starting test");
        AuthenticationRequest wrongEmailLoginRequest = new AuthenticationRequest("wrongemail@ubs.com", "123");
        String wrongEmailLoginJson = objectMapper.writeValueAsString(wrongPasswordLoginRequest);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(wrongEmailLoginJson))
                .andExpect(status().isUnauthorized())
                .andDo(print())
                .andReturn();
        System.out.println("List item 2/2: OK");
    }

    @Test
    @DisplayName("InvalidLoginRequest")
    public void shouldNotAuthenticateInvalidRequest() throws Exception {
        System.out.println("List item 1/2: Starting test");
        AuthenticationRequest emptyPasswordLoginRequest = new AuthenticationRequest("carlos@ubs.com", "");
        String wrongPasswordLoginJson = objectMapper.writeValueAsString(emptyPasswordLoginRequest);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(wrongPasswordLoginJson))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();
        System.out.println("List item 1/2: OK");

        System.out.println("List item 2/2: Starting test");
        AuthenticationRequest EmptyEmailLoginRequest = new AuthenticationRequest("", "123");
        String wrongEmailLoginJson = objectMapper.writeValueAsString(EmptyEmailLoginRequest);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(wrongEmailLoginJson))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();
        System.out.println("List item 2/2: OK");
    }

    @Test
    @DisplayName("EmptyLoginRequest")
    public void shouldNotAuthenticateEmptyRequest() throws Exception {
        AuthenticationRequest wrongPasswordLoginRequest = new AuthenticationRequest("", "");
        String wrongPasswordLoginJson = objectMapper.writeValueAsString(wrongPasswordLoginRequest);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(wrongPasswordLoginJson))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();
    }

}