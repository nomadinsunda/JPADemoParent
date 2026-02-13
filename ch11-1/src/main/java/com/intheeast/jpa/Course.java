package com.intheeast.jpa;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Course {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String courseCode;     // 예: CS101
    private String courseName;     // 예: 자료구조
    private String professorName;  // 담당 교수
    private int credit;            // 학점
    private int capacity;          // 수강 정원
    
    private LocalDateTime createdAt;

    @ManyToMany(mappedBy = "courses")
    private Set<Student> students = new HashSet<>();

    // getter, setter
}