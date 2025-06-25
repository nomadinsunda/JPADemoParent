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
            em.persist(member);

            // 🔹 UPDATE 테스트
            member.setName("변경된 지한");

            // 🔹 SELECT 테스트 (1차 캐시 제거 후 PostLoad 테스트)
            em.flush();
            em.clear();
            em.find(Member.class, 1L); // → @PostLoad 실행됨

            // 🔹 DELETE 테스트
            Member deleteTarget = em.find(Member.class, 1L);
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
