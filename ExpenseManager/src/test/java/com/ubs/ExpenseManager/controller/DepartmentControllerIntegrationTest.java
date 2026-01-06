package com.ubs.ExpenseManager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubs.ExpenseManager.entities.department.Department;
import com.ubs.ExpenseManager.entities.department.enums.CurrencyCode;
import com.ubs.ExpenseManager.entities.department.repository.DepartmentRepository;

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

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;




@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test") // Add this line
public class DepartmentControllerIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpassword");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DepartmentRepository repository;

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("server.port", () -> "0");
    }

    @BeforeEach
    void setup() {
        repository.deleteAll();
        repository.save(new Department("RH", CurrencyCode.BRL));
        repository.save(new Department("HR", CurrencyCode.USD));
    }

    //POST RELATED TESTS
    @Test
    @DisplayName("CreateDepartment")
    void shouldCreateDepartment() throws Exception{
        Department new_department = new Department("Sales", CurrencyCode.EUR);
        String json = objectMapper.writeValueAsString(new_department);

        mockMvc.perform(post("/api/departments")
                .contentType(MediaType.APPLICATION_JSON)
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
                .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("CreateDuplicityConflict")
    void shouldReturnDuplicityError() throws Exception{
        Department duplicated_department = new Department("RH", CurrencyCode.EUR);
        String json = objectMapper.writeValueAsString(duplicated_department);

        mockMvc.perform(post("/api/departments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    //GET RELATED
    @Test
    @DisplayName("ReturnDepartmentByID")
    void shouldReturnDepartmentById() throws Exception {
        System.out.println("List item 1/2: Starting test");
        mockMvc.perform(get("/api/departments/RH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("RH"))
                .andExpect(jsonPath("$.currency").value("BRL"));
        System.out.println("List item 1/2: OK");

        System.out.println("List item 2/2: Starting test");
        mockMvc.perform(get("/api/departments/HR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("HR"))
                .andExpect(jsonPath("$.currency").value("USD"));
        System.out.println("List item 2/2: OK");
    }

    @Test
    @DisplayName("ReturnForNotFound")
    void shouldReturnNotFoundForInvalidId() throws Exception {
        mockMvc.perform(get("/api/departments/NonExistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("FindAllDepartments")
    void shouldReturnAllDepartments() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/departments"))
                                    .andExpect(status().isOk())
                                    .andDo(print())
                                    .andReturn();

        String responseBody = result.getResponse().getContentAsString();

        assertEquals( "[{\"name\":\"RH\",\"currency\":\"BRL\",\"monthlyBudget\":0.00},{\"name\":\"HR\",\"currency\":\"USD\",\"monthlyBudget\":0.00}]",responseBody, "Response does not match expected result, or not all departments were listed.");
    }

    @Test
    @DisplayName("FindAllInEmptyList")
    void shouldReturnEmptyList() throws Exception {
        repository.deleteAll();

        MvcResult result = mockMvc.perform(get("/api/departments"))
                                    .andExpect(status().isOk())
                                    .andDo(print())
                                    .andReturn();

        String responseBody = result.getResponse().getContentAsString();

        assertEquals( "[]",responseBody, "Response does not match expected result.");
    }

    //PUT RELATED
    @Test
    @DisplayName("UpdateDepartmentData")
    void shouldUpdateDepartment() throws Exception{
        String request = """
                {
                    "currency": "EUR",
                    "monthlyBudget": 10000
                }
                """;

        mockMvc.perform(put("/api/departments/RH")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                        .andDo(print())
                        .andExpect(status().is2xxSuccessful());

        System.out.println("\nGet with new data:");
        mockMvc.perform(get("/api/departments/RH"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.currency").value("EUR"));
    }

    //PATCH RELATED
    @Test
    @DisplayName("RenameDepartment")
    void shouldRenameDepartment() throws Exception{
        String request = """
                {
                "newName": "RecursosHumanos"
                }
                """;

        mockMvc.perform(patch("/api/departments/RH/name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                        .andDo(print())
                        .andExpect(status().is2xxSuccessful());

        System.out.println("\nGet with new name:");
        mockMvc.perform(get("/api/departments/RecursosHumanos"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.name").value("RecursosHumanos"));
    }

    //DELETE RELATED
    @Test
    @DisplayName("DeleteDepartment")
    void shouldDeleteDepartment() throws Exception{
        System.out.println("List item 1/2: Starting test");
        mockMvc.perform(delete("/api/departments/RH"))
                .andExpect(status().is2xxSuccessful());
        System.out.println("List item 1/2: OK");

        System.out.println("List item 2/2: Starting test");
        mockMvc.perform(delete("/api/departments/HR"))
                .andExpect(status().is2xxSuccessful());
        System.out.println("List item 2/2: OK");
    }

    @Test
    @DisplayName("DeleteNonExistentDepartment")
    void shouldNotDeleteNonExistentDepartment() throws Exception {
        repository.deleteAll();

        mockMvc.perform(delete("/api/departments/RH"))
                .andExpect(status().isNotFound())
                .andDo(print());
    }


}
