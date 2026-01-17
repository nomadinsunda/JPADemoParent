package com.intheeast.jpa;

import javax.persistence.*;
import java.util.List;

public class OneToOneUniDirectionalTest {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            // 1. 데이터 준비 (Person 생성 및 저장)
            Person person = new Person("홍길동");
            em.persist(person); // Person이 먼저 영속화되어 ID(PK)가 생성되어야 함

            // 2. Passport 생성 및 Person 연결
            Passport passport = new Passport("P-123456789");
            passport.setPerson(person); // Passport(대상 테이블)에 외래 키 설정
            em.persist(passport);

            tx.commit(); // DB에 데이터 반영

            // ----------------------------------------------------
            // 3. 테스트를 위한 영속성 컨텍스트 초기화
            em.clear(); 
            System.out.println("\n=== [재조회 시작] ===");

            // Step 1: 특정 Person 조회 (Person 테이블만 Select)
            Person foundPerson = em.find(Person.class, person.getId());
            System.out.println("✅ 찾은 사람: " + foundPerson.getName());

            // Step 2: EntityManager와 JPQL을 사용하여 이 사람의 Passport 찾기
            // [분석] Person 엔티티에는 passport 필드가 없으므로, Passport 테이블을 직접 쿼리합니다.
            String jpql = "SELECT p FROM Passport p WHERE p.person.id = :personId";
            
            try {
                Passport foundPassport = em.createQuery(jpql, Passport.class)
                        .setParameter("personId", foundPerson.getId())
                        .getSingleResult();

                System.out.println("✅ 찾은 여권 번호: " + foundPassport.getNumber());
                System.out.println("✅ 여권 주인의 ID: " + foundPassport.getPerson().getId());
                
            } catch (NoResultException e) {
                System.out.println("❌ 해당 사용자는 여권 정보가 없습니다.");
            }

        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
            emf.close();
        }
    }
}