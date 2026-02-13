package com.intheeast.jpa;

import lombok.*;

import jakarta.persistence.*;

@Entity
@Setter
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private int employeeCounts;

    public Department(String name, int employeeCounts) {
        this.name = name;
        this.employeeCounts = employeeCounts;
    }
}
