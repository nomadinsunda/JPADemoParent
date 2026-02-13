package com.intheeast.jpa;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EnrollmentAssociationTest {

//    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

    public static void main(String[] args) {

        EntityManagerFactory emf =
                Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            // =========================
            // 1️ Course 5개 생성
            // =========================
            List<Course> courses = new ArrayList<>();

            for (int i = 1; i <= 5; i++) {
                Course course = new Course();
                course.setCourseCode("CS10" + i);
                course.setCourseName("전공과목-" + i);
                course.setProfessorName("교수-" + i);
                course.setCredit(3);
                course.setCapacity(50);
                course.setCreatedAt(LocalDateTime.now());

                em.persist(course);
                courses.add(course);
            }

            // =========================
            // 2️ Student 20명 생성
            // =========================
            List<Student> students = new ArrayList<>();

            for (int i = 1; i <= 20; i++) {
                Student student = new Student();
                student.setStudentNumber("2023" + String.format("%04d", i));
                student.setName("학생-" + i);
                student.setMajor("컴퓨터공학");
                student.setGrade((i % 4) + 1);
                student.setEmail("student" + i + "@test.com");
                student.setRegisteredAt(LocalDateTime.now());

                em.persist(student);
                students.add(student);
            }

            // =========================
            // 3️ Enrollment 대량 생성
            // =========================
            Random random = new Random();

            int enrollmentCount = 0;

            for (Student student : students) {

                // 각 학생당 2~3개 과목 수강
                int courseCount = 2 + random.nextInt(2);

                for (int i = 0; i < courseCount; i++) {
                    Course randomCourse =
                            courses.get(random.nextInt(courses.size()));

                    Enrollment enrollment = new Enrollment();
                    enrollment.setStudent(student);
                    enrollment.setCourse(randomCourse);
                    enrollment.setEnrolledAt(LocalDateTime.now());
                    enrollment.setSemester("2026-1");
                    enrollment.setStatus(EnrollmentStatus.ENROLLED);

                    em.persist(enrollment);
                    enrollmentCount++;
                }
            }

            tx.commit();

            System.out.println("==== 저장 완료 ====");
            System.out.println("Course 개수: " + courses.size());
            System.out.println("Student 개수: " + students.size());
            System.out.println("Enrollment 개수: " + enrollmentCount);

            // =========================
            // 4️ 조회 테스트
            // =========================
            System.out.println("\n==== 조회 테스트 ====");

            List<Course> foundCourses =
                    em.createQuery("select c from Course c", Course.class)
                      .getResultList();

            for (Course course : foundCourses) {
                System.out.println("과목: " + course.getCourseName()
                        + " | 수강 인원: "
                        + course.getEnrollments().size());
            }

        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
            emf.close();
        }
    }
}
