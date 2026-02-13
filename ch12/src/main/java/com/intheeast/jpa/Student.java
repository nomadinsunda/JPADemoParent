package com.intheeast.jpa;


import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.*;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "student")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String studentNumber;  // 학번
    private String name;
    private String major;          // 전공
    private int grade;             // 학년
    private String email;

    private LocalDateTime registeredAt;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    private List<Enrollment> enrollments = new ArrayList<>();

    // getter, setter 생략
}


//@Entity
//public class Student {
//
//    @Id @GeneratedValue
//    private Long id;
//
//    private String name;
//
//    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Enrollment> enrollments = new ArrayList<>();
//
//    public Student() {}
//
//    public Student(String name) {
//        this.name = name;
//    }
//
//    public void enroll(Course course, LocalDate date, Double score) {
//        Enrollment enrollment = new Enrollment(this, course, date, score);
//        enrollments.add(enrollment);
//        course.getEnrollments().add(enrollment);
//    }
//
//    public String getName() { return name; }
//    public List<Enrollment> getEnrollments() { return enrollments; }
//}
