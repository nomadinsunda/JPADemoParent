package com.intheeast.jpa;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ManyToManyBiDirectionalTest {

    private static final EntityManagerFactory emf = 
    		Persistence.createEntityManagerFactory("hello");

    public static void main(String[] args) {
        saveTest();             // 연관관계 저장 테스트
        queryTest();            // 양방향 탐색 테스트
//        deleteRelationTest();   // 관계 제거 및 삭제 테스트

        emf.close();
    }

    // ✅ 1. 저장 테스트
    public static void saveTest() {
        System.out.println("\n🟢 saveTest 시작");

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            
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
            Random random = new Random();

            for (int i = 1; i <= 20; i++) {
                Student student = new Student();
                student.setStudentNumber("2023" + String.format("%04d", i));
                student.setName("학생-" + i);
                student.setMajor("컴퓨터공학");
                student.setGrade((i % 4) + 1);
                student.setEmail("student" + i + "@test.com");
                student.setRegisteredAt(LocalDateTime.now());
                
                // 연관관계 설정 (양방향)
                // 랜덤하게 설정해야 함.                
                int courseCount = 2 + random.nextInt(2);

                for (int j = 0; j < courseCount; j++) {
                	int value = random.nextInt(5);  // 0 ~ 4
                	///////////////////////////////////////////
                    student.addCourse(courses.get(value));
                    ////////////////////////////////////////////
                }

                em.persist(student);
                students.add(student);
            } 

            tx.commit();
            System.out.println("✅ 저장 완료");
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    // ✅ 2. 재조회 및 양방향 탐색 테스트
    public static void queryTest() {
        System.out.println("\n🟢 queryTest 시작");

        EntityManager em = emf.createEntityManager();

        try {
//            List<Student> students = em.createQuery("select s from Student s", Student.class).getResultList();
//
//            System.out.println("*************************************************");
//            for (Student s : students) {
//                System.out.println("👨‍🎓 학생: " + s.getName());
//                                
//                // s.getCourses()
//                /*
//                 select
//        			courses0_.student_id as student_1_2_0_,
//        			courses0_.course_id as course_i2_2_0_,
//        			course1_.id as id1_0_1_,
//        			course1_.name as name2_0_1_ 
//    			 from
//        			student_course courses0_ 
//    			 inner join
//                    Course course1_ 
//            			on courses0_.course_id=course1_.id 
//    			  where
//        			courses0_.student_id=?
//                 */
//                System.out.println("##############################################");
//                for (Course c : s.getCourses()) {
//                    System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@2");
//                    System.out.println("   📘 수강과목: " + c.getCourseName());
//                }
//                System.out.println("##############################################");
//            }
            System.out.println("*************************************************");

            List<Course> courses = em.createQuery("select c from Course c", Course.class).getResultList();
            
            System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
            for (Course c : courses) {
                System.out.println("📘 과목: " + c.getCourseName());
                System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^6");
                
                // c.getStudents()
                /*
                 select
			        students0_.course_id as course_i2_2_0_,
			        students0_.student_id as student_1_2_0_,
			        student1_.id as id1_1_1_,
			        student1_.name as name2_1_1_ 
			    from
			        student_course students0_ 
			    inner join
			        Student student1_ 
			            on students0_.student_id=student1_.id 
			    where
			        students0_.course_id=?
                 */
                for (Student s : c.getStudents()) {
                    System.out.println("   👨‍🎓 수강생: " + s.getName());
                }
            }
            System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");

        } finally {
            em.close();
        }
    }

    // ✅ 3. 관계 삭제 테스트
    public static void deleteRelationTest() {
        System.out.println("\n🟢 deleteRelationTest 시작");

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Student student = em.createQuery("select s from Student s where s.name = '홍길동'", Student.class)
                                .getSingleResult();

            // 수강 과목 하나 제거
            
            ////////////////////////////////////////////////////////
            // 학생이 몇 개의 과목을 수강한지가 중요한 것이 아니라,
            // 단지 한 과목만 삭제하기 위해서 for looping을 할 필요가 없기 때문에!!!
            Course toRemove = student.getCourses().iterator().next();

            // 양방향 연관관계 해제
            student.getCourses().remove(toRemove);
            toRemove.getStudents().remove(student);

            tx.commit();
            System.out.println("🗑️ 연관관계 해제 완료: " + toRemove.getCourseName());
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
}
