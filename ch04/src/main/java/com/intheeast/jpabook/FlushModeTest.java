package com.intheeast.jpabook;

import jakarta.persistence.*;

public class FlushModeTest {
 public static void main(String[] args) {
     EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
     EntityManager em = emf.createEntityManager();
     EntityTransaction tx = em.getTransaction();

     try {
         tx.begin();

         // JPQL로 생성된 SQL 쿼리는 Action Queue에 저장되지 않고,
         // 즉시 데이터베이스에게 실행됨.
         // Flushing : Flushing은 트랜잭션 커밋 시 발생합니다.
         //  - Action Queue에 있는 SQL 쿼리를 실행 : flush()  
         //
         // Provider(하이버네이트)는 다른 시간에도 플러싱을 수행할 수 있지만, 
         // 반드시 그렇게 할 필요는 없습니다.
//         em.setFlushMode(FlushModeType.COMMIT); // 디폴트는 FlushModeType.AUTO
//                                                // :JPQL 실행 시 Flushing이 발생합니다.

         Member member = new Member(200L, "진영");
         em.persist(member); // Action Queue에 insert 쿼리가 저장

         // insert 
         // JPQL 실행 → flush 자동 발생 안함 (COMMIT 모드이기 때문)
         em.createQuery("select m from Member m", Member.class)
         	.getResultList(); // INSERT SQL 안 나감

         System.out.println("🚨 COMMIT 모드에서는 flush 자동 발생 안 함");

         // commit()은 트랜잭션 내에서 발생한 DB 변경 사항(INSERT, UPDATE, DELETE 등)을 
         // 영구 반영(Commit)하는 작업.
         // 조회(SELECT)는 commit의 대상이 아님.
         tx.commit(); // 이 시점에서만 INSERT SQL 실행됨
     } catch (Exception e) {
         tx.rollback();
     } finally {
         em.close();
         emf.close();
     }
 }
}
