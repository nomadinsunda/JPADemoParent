package com.intheeast.jpabook;

import javax.persistence.*;

public class JpaLifecycleMain {

    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            // 1. 비영속(transient) 상태
            Member member = new Member(1L, "John");
            System.out.println("🟡 비영속: " + member);

            // 2. 영속 상태
            em.persist(member); // 이제부터 관리됨
            System.out.println("🟢 영속: persist() 호출 후");

            // 3. flush 없이 조회 (1차 캐시)
            Member find = em.find(Member.class, 1L);
            System.out.println("🔎 조회된 이름 = " + find.getName());

            // 4. 준영속 상태
            em.detach(member);  // 관리 중단
            member.setName("변경된 이름"); // 변경해도 반영되지 않음
            System.out.println("🔴 준영속: 이름 수정됨 (DB 반영 X)");

            // 5. 삭제 상태
            em.persist(member); // 다시 영속화 시켜야 remove 가능
            em.remove(member);
            System.out.println("⚫ 삭제 상태: remove() 호출됨");

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
            emf.close();
        }
    }
}
