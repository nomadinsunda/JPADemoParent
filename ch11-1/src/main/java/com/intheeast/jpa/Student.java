package com.intheeast.jpa;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
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

    @ManyToMany // 디폴트로 Fetch는 FetchType.LAZY:연관 관계인 Course의 row도 같이 DB에서 얻어 오지 않음
    @JoinTable(name = "student_course",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id"))
    private Set<Course> courses = new HashSet<>();

    public void addCourse(Course course) {
        courses.add(course);
        course.getStudents().add(this); // 양방향 설정
    }

    // getter, setter
}