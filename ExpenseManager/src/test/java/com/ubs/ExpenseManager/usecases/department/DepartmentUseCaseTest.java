package com.ubs.ExpenseManager.usecases.department;

import com.ubs.ExpenseManager.entities.department.Department;
import com.ubs.ExpenseManager.entities.department.enums.CurrencyCode;
import com.ubs.ExpenseManager.entities.department.repository.DepartmentRepository;
import com.ubs.ExpenseManager.usecases.department.dto.CreateDepartmentRequest;
import com.ubs.ExpenseManager.usecases.department.dto.DepartmentResponse;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;
//import static org.junit.jupiter.api.Assertions.*;
//@DataJpaTest
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class DepartmentUseCaseTest {

//    @Mock
//    private EntityManager entityManager;


    @Mock
    private DepartmentRepository departmentRepository;


    @Test
    @DisplayName("Must create a department sucessfully in DB")
    void createDepartmentCase1() {
        System.out.println("Case 1");
        CreateDepartmentRequest request = new CreateDepartmentRequest("Test-Dpt", CurrencyCode.BRL);

        // Mock the create method to return 1 (indicating one row affected)
        when(departmentRepository.create(anyString(), anyString())).thenReturn(1);

        departmentRepository.create(request.name(), request.currency().name());

//        System.out.println(DepartmentResponse.fromCreate(request).currency().getClass().getSimpleName());
//        System.out.println(DepartmentResponse.fromCreate(request).currency());
        assertEquals("Test-Dpt", DepartmentResponse.fromCreate(request).name(), "Created department name does not match");
        assertEquals(CurrencyCode.BRL, DepartmentResponse.fromCreate(request).currency(), "Created departments currency does not match");
    }

    @Test
    @Transactional
    @DisplayName("Must list all departments sucessfully from DB")
    void findAll() {
        // placeholder
        CreateDepartmentRequest request1 = new CreateDepartmentRequest("Test-Dpt-brazil", CurrencyCode.BRL);
        CreateDepartmentRequest request2 = new CreateDepartmentRequest("Test-Dpt-EU", CurrencyCode.EUR);
        CreateDepartmentRequest request3 = new CreateDepartmentRequest("Test-Dpt-US", CurrencyCode.USD);

        createDepartments(request1);
        createDepartments(request2);
        createDepartments(request3);

        System.out.println("req2: "+DepartmentResponse.fromCreate(request2));

//        when(departmentRepository.findAll()).thenReturn(List.of());

        // get all departments
//        List<Department> listresult = departmentRepository.findAll();
//        System.out.println("listresult -> "+listresult);

        // Get all departments as a list
        List<DepartmentResponse> result = departmentRepository.findAll().stream().map(DepartmentResponse::fromEntity).toList();
        System.out.println("All Dpts\n" + result);

    }

    void createDepartments(CreateDepartmentRequest request){
//        CreateDepartmentRequest request = new CreateDepartmentRequest("Test-Dpt", CurrencyCode.BRL);
        departmentRepository.create(request.name(), request.currency().name());
//        departmentRepository.save(DepartmentResponse.fromCreate(request));
        System.out.println(DepartmentResponse.fromCreate(request));
    }


//
//    @Test
//    @DisplayName("Must get a department sucessfully from DB")
//    void findById() {
//    }
//
//    @Test
//    void update() {
//    }
//
//    @Test
//    void rename() {
//    }
//
//    @Test
//    void delete() {
//    }
}