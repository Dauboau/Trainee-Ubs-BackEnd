package com.ubs.ExpenseManager.entities.department.repository;

import com.ubs.ExpenseManager.entities.department.Department;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, String> {
    Optional<Department> findByName(String name);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE departments SET name = :newName WHERE name = :oldName", nativeQuery = true)
    int renameDepartment(String oldName, String newName);
}
