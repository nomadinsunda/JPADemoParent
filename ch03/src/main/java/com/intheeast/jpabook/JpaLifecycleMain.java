package com.intheeast.jpabook;

import javax.persistence.*;

public class JpaLifecycleMain {

    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            // 1. 비영속 상태 (Transient)
            Member member = new Member(1L, "지한");
            System.out.println("🟡 비영속 상태: " + member);

            // 2. 영속 상태 (Managed)
            em.persist(member);
            System.out.println("🟢 영속 상태: persist() 호출");

            // 2-1. Flush 전 → DB에는 INSERT 안 됨
            System.out.println("🧪 flush 전: 아직 DB에 INSERT되지 않음");

            // 2-2. flush 강제 호출
            em.flush();
            System.out.println("🚀 flush() 호출 → DB 반영됨 (INSERT)");

            // 3. Dirty Checking (변경 감지)
            member.setName("변경된 지한");  // 변경 감지 대상
            System.out.println("✏️ 엔티티 값 변경: Dirty Checking 대상 설정");

            // 4. JPQL 실행 → flush 자동 발생
            System.out.println("🧪 JPQL 실행 → flush() 자동 호출 + SQL 동기화");
            // JPQL(Java Persistence query language) statement 실행
            em.createQuery("select m from Member m", Member.class).getResultList();
                        
            // 5. 준영속 상태 (Detached)
            em.detach(member);  // 관리 중단
            member.setName("무시될 이름");
            System.out.println("🔴 준영속 상태 → 이름 변경 무시됨");

            // 6. 삭제 상태
            Member member2 = new Member(2L, "삭제될 유저");
            em.persist(member2);
            em.remove(member2);
            System.out.println("⚫ 삭제 상태 → remove() 호출");

            tx.commit(); // flush + commit
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
            emf.close();
        }
    }
}
