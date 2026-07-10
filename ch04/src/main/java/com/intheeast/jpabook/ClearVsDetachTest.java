package com.intheeast.jpabook;

import java.util.List;

import jakarta.persistence.*;

public class ClearVsDetachTest {
	
	public static void insertAndFlushEntities(EntityManagerFactory emf) {
		EntityManager em = emf.createEntityManager();
		// Persistence Context
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            // 1. 엔티티 생성 및 영속
            Member m1 = new Member(1L, "철수");
            Member m2 = new Member(2L, "영희");
            Member m3 = new Member(3L, "동영");

            em.persist(m1);
            em.persist(m2);
            em.persist(m3);
            //em.flush(); // INSERT SQL 쿼리가 DB에게 전달됨!
                        // : 그러나 commit을 하지 않으면 해당 테이블에 적용되지 않음
            /*
                영속성 컨텍스트
				↓				
				INSERT
				UPDATE
				DELETE				
				↓				
				Database
				
				**flush는 commit이 아니다.				
             */
            
            List<Member> members =
            	    em.createQuery("select u from Member u", Member.class)
            	      .getResultList();

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close(); // 엔티티매니저 삭제!!!
        }
	}
	
	public static void detachEntities(EntityManagerFactory emf) {
		EntityManager em = emf.createEntityManager();
		// Persistence Context
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            
            Member m1 = em.find(Member.class, 1L);

            // detach - 개별 엔티티만 준영속화
            em.detach(m1); // m1은 준영속 상태
            m1.setName("변경된 철수 (준영속 상태에서 변경)"); // DB 반영 안 됨
            
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close(); // 엔티티매니저 삭제!!!
        }
	}
	
	public static void clearPersistenceContext(EntityManagerFactory emf) {
		EntityManager em = emf.createEntityManager();
		// Persistence Context
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            
            Member m1 = em.find(Member.class, 1L);
            Member m2 = em.find(Member.class, 2L);
            Member m3 = em.find(Member.class, 3L);

           // 3. clear - 전체 영속성 컨텍스트 초기화
            em.clear(); // m1, m2, m3도 준영속 상태로 전환됨
            m1.setName("변경된 철수 (준영속 상태에서 변경)"); // DB 반영 안 됨
            m2.setName("변경된 영희 (준영속 상태에서 변경)"); // DB 반영 안 됨
            m3.setName("변경된 동영 (준영속 상태에서 변경)"); // DB 반영 안 됨
            
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close(); // 엔티티매니저 삭제!!!
        }
	}
	
	public static void mergePersistenceContext(EntityManagerFactory emf) {
		EntityManager em = emf.createEntityManager();
		// Persistence Context
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            
            Member m1 = em.find(Member.class, 1L);
            Member m2 = em.find(Member.class, 2L);
            Member m3 = em.find(Member.class, 3L);    
                        
            em.detach(m1);
            em.detach(m2);
            em.detach(m3);
            
            m1.setName("변경된 철수 (준영속 상태에서 변경)"); // DB 반영 안 됨
            m2.setName("변경된 영희 (준영속 상태에서 변경)"); // DB 반영 안 됨
            m3.setName("변경된 동영 (준영속 상태에서 변경)"); // DB 반영 안 됨
            
            em.flush();
            
            //merge - 준영속 상태를 다시 영속화 (새로운 영속 인스턴스 반환)
            // select 쿼리로 DB로 부터 받아온 데이터가 1차 캐시에 저장?
            // : 아니면, 1차 캐시에 저장되기 전에 변경 사항이 적용?
            Member mergedM1 = em.merge(m1); // 변경사항이 복사되어 영속 상태로 전환됨
            Member mergedM2 = em.merge(m2);
            Member mergedM3 = em.merge(m3);            
            
            System.out.println("🟢 mergedM1 = " + mergedM1);
            System.out.println("🟢 mergedM2 = " + mergedM2);
            System.out.println("🟢 mergedM3 = " + mergedM3);

            
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close(); // 엔티티매니저 삭제!!!
        }
	}

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

        insertAndFlushEntities(emf);
        detachEntities(emf);
        clearPersistenceContext(emf);
        mergePersistenceContext(emf);
    }
}
