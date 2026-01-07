package com.ubs.ExpenseManager.entities.employee.repository;

import com.ubs.ExpenseManager.entities.employee.Employee;
import com.ubs.ExpenseManager.entities.employee.enums.Role;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    Optional<Employee> findByEmail(String email);
    List<Employee> findAllByActiveTrue();
    Optional<Employee> findByIdAndActiveTrue(UUID id);
    Optional<Employee> findByIdAndRoleAndActiveTrue(UUID id, Role role);
    boolean existsByManagerId(UUID managerId);
    List<Employee> findAllByManagerId(UUID managerId);
    List<Employee> findAllByActiveTrueAndRole(Role role);
}
