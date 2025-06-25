package com.intheeast.jpabook;

import javax.persistence.*;

public class JpaLifecycleMain {
	
	public static void afterFirstEntityManagerClose(EntityManager em) {
		
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            
        	/*
        	 JPA find 메서드 동작 요약
			1. 현재 ID가 1L인 Member 엔티티는 detach 상태이다.
			2. → 즉, 영속성 컨텍스트에서 관리되지 않는 상태이다.
			3. find(Member.class, 1L)를 호출하면 JPA는 1차 캐시를 먼저 조회한다.
			4. 하지만 현재 1차 캐시는 비어 있다.
			5. 따라서 JPA는 데이터베이스를 조회하여
			6. ID가 1인 member 테이블의 row를 가져온다.
			7. 그 데이터를 기반으로 새로운 엔티티 인스턴스를 생성하고,
			8. 이를 1차 캐시에 저장한다.
			9. 그리고 그 엔티티 인스턴스의 참조를 반환한다.
        	 */
            Member member = em.find(Member.class, 1L);
            Long id = member.getId();
            
            tx.commit();
            
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
	}

    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        Member member = null;
        try {
            tx.begin();

            // 1. 비영속 상태 (Transient)
            member = new Member(1L, "지한");
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
            // QueryDSL : JPQL Builder...
            em.createQuery("select m from Member m", Member.class).getResultList();
                        
            // 5. 준영속 상태 (Detached)
            em.detach(member);  // 관리 중단 : 1차 캐시에서 삭제됨.
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
            //em.close();
            //emf.close();
        }
        
        afterFirstEntityManagerClose(em);
        
        // 1차 캐시에 id가 1인 Member 엔티티 클래스 인스턴스가 있음
        //em.merge(member);
        
        emf.close();
        
    }
}
