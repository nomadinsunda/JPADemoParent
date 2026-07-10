package com.intheeast.jpabook;

import jakarta.persistence.*;

public class EntityLifecycleDemo {
	public static void insertEntities(EntityManagerFactory emf) {
		EntityManager em = emf.createEntityManager();
		// Persistence Context
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            // 🔹 INSERT 테스트
            Member member = new Member(1L, "지한");
            Member member2 = new Member(2L, "영한");
            em.persist(member);  // 영속 상태(엔티티매니저가 이 엔티티 클래스 인스턴스를 관리)     
            em.persist(member2);

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close(); // 엔티티매니저 삭제!!!
        }
	}
	
	public static void selectEntities(EntityManagerFactory emf) {
		EntityManager em = emf.createEntityManager();
		// Persistence Context
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin(); 
            // em.find를 호출하면, 엔티티매니저가 PK값이 1인 엔티티 클래스 인스턴스가
            // 1차 캐시에 있는지 없는지를 먼저 확인.
            // -> 없다면 Select Query를 만들고
            // -> 있다면 1차 캐시에 있는 인스턴스를 find 메서드가 리턴함.
            Member m1 = em.find(Member.class, 1L); // → @PostLoad 실행됨
            Member m2 = em.find(Member.class, 2L);
            
            Member m1_1 = em.find(Member.class, 1L);
            Member m2_1 = em.find(Member.class, 1L);

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
            
        }
	}
	
	public static void updateEntities(EntityManagerFactory emf) {
		EntityManager em = emf.createEntityManager();
		// Persistence Context
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin(); 
            // em.find를 호출하면, 엔티티매니저가 PK값이 1인 엔티티 클래스 인스턴스가
            // 1차 캐시에 있는지 없는지를 먼저 확인.
            // -> 없다면 Select Query를 만들고
            // -> 있다면 1차 캐시에 있는 인스턴스를 find 메서드가 리턴함.
            Member m1 = em.find(Member.class, 1L); // → @PostLoad 실행됨
            // 엔티티 인스턴스 업데이트!
            m1.setName("영숙"); // 변경을 감지(Dirty Checking)
                               // Update Action(Update 쿼리)을 큐잉!

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
            
        }
	}
	
	public static void deleteEntities(EntityManagerFactory emf) {
		EntityManager em = emf.createEntityManager();
		// Persistence Context
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin(); 
            // em.find를 호출하면, 엔티티매니저가 PK값이 1인 엔티티 클래스 인스턴스가
            // 1차 캐시에 있는지 없는지를 먼저 확인.
            // -> 없다면 Select Query를 만들고
            // -> 있다면 1차 캐시에 있는 인스턴스를 find 메서드가 리턴함.
            Member m1 = em.find(Member.class, 1L); // → @PostLoad 실행됨
            em.remove(m1);

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
            
        }
	}



    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        insertEntities(emf);
        selectEntities(emf);
        updateEntities(emf);
        deleteEntities(emf);
        
        emf.close();
    }
}
