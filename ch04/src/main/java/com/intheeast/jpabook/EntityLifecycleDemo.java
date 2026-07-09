package com.intheeast.jpabook;

import javax.persistence.*;

public class EntityLifecycleDemo {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            // 🔹 INSERT 테스트
            Member member = new Member(1L, "지한");
            Member member2 = new Member(2L, "지한");
            em.persist(member);

            // persist 메서드에게 전달된 엔티티 클래스 인스턴스는 이제부터, Pesistence Context에서
            // 엔티티 매니지에 의해서 관리가 된다.
            // 🔹 UPDATE 테스트: JPA는 엔티티 클래스의 인스턴스에 대한 변경을 자동으로 추적(Dirty Checking)
            member.setName("변경된 지한");

            // 🔹 SELECT 테스트 (1차 캐시 제거 후 PostLoad 테스트)
            em.flush();
            em.clear(); // 1차 캐시 전체를 클리어!!!,
            em.find(Member.class, 1L); // → @PostLoad 실행됨

            // 🔹 DELETE 테스트
            Member deleteTarget = em.find(Member.class, 1L);
            deleteTarget = em.find(Member.class, 1L);
            em.remove(deleteTarget);

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
            emf.close();
        }
    }
}
