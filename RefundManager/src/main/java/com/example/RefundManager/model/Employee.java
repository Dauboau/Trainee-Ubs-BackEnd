package com.example.RefundManager.model;

import com.example.RefundManager.model.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "employees")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id") // Nome da FK no banco de dados
    private Department department;

    @Column(nullable = false)
    private long name;

    @Column(nullable = false)
    private long email;

    @Column(nullable = false)
    private long passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}
