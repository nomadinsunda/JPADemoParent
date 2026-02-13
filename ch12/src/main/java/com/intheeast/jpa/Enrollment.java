package com.intheeast.jpa;

import jakarta.persistence.*;
import java.time.LocalDate;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "enrollment")
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime enrolledAt;   // 수강 신청 일시

    @Enumerated(EnumType.STRING)
    private EnrollmentStatus status;    // 수강 상태

    private String semester;            // 예: 2026-1

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    // getter, setter 생략
}


