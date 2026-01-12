package com.ubs.ExpenseManager.entities.department.repository;

import com.ubs.ExpenseManager.entities.department.Department;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, String> {
    boolean existsByName(String name);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE departments SET name = :newName WHERE name = :oldName", nativeQuery = true)
    int rename(String oldName, String newName);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "DELETE FROM departments WHERE name = :name", nativeQuery = true)
    int deleteByName(String name);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
        INSERT INTO departments (name, currency) 
        VALUES (:name, (:currency)::currency_code)
        """, nativeQuery = true)
    int create(String name, String currency);
}
