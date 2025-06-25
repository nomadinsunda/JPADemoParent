package com.intheeast.jpabook;

import javax.persistence.*;

public class HelloJpa {

    public static void main(String[] args) {
        // 1. EntityManagerFactory 생성 (persistence.xml에 정의된 이름 사용)
        EntityManagerFactory emf = 
        		Persistence.createEntityManagerFactory("hello");

        // 2. EntityManager 생성
        EntityManager em = emf.createEntityManager();

        // 3. 트랜잭션 획득
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();  // 트랜잭션 시작

            // === 저장 ===
            Member member = new Member();
            member.setId(1L);
            member.setName("John");
            em.persist(member);  // INSERT 발생

            // === 조회 ===
            Member findMember = em.find(Member.class, 1L);
            System.out.println("조회된 이름: " + findMember.getName());

            tx.commit(); // 트랜잭션 커밋
        } catch (Exception e) {
            tx.rollback();  // 오류 발생 시 롤백
        } finally {
            em.close();  // EntityManager 종료
        }

        emf.close();  // EntityManagerFactory 종료
    }
}
