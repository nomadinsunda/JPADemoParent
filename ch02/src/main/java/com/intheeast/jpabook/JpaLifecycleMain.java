package com.intheeast.jpabook;

import jakarta.persistence.*;

public class JpaLifecycleMain {
	// Entity Manager가 관리한다.
	// Persistence Context
	//   -1차 캐시: 엔티티 클래스 인스턴스들을 관리하고 있는 역할)
	//   -Transactional Write Behind Queue queue!!!
	public static void addPersons(EntityManager em) {
		
		EntityTransaction et = em.getTransaction();
		et.begin();
		
		try {		
			// 각각의 Person[Entity 클래스] 인스턴스는 person 테이블의 단일 row들과 매핑되어야 함...
			// : 물론 persist 메서드를 통해 영속화가 되어야 함
			Person p1 = new Person(1L, "kim", "0100000000");
			Person p2 = new Person(2L, "kim", "0100000000");
			Person p3 = new Person(3L, "kim", "0100000000");
			Person p4 = new Person(4L, "kim", "0100000000");
			Person p5 = new Person(5L, "kim", "0100000000");
			Person p6 = new Person(6L, "kim", "0100000000");
			Person p7 = new Person(7L, "kim", "0100000000");
			Person p8 = new Person(8L, "kim", "0100000000");
			
			// 엔티티 클래스 인스턴스만 만들면 아무 의미없음.
			// 엔티티매니저의 persist 메서드에게 엔티티 클래스 인스턴스 생성을 알려야 함
			em.persist(p1);		// insert query p1-TWB queue
			em.persist(p2);		// insert query p2-TWB queue
			em.persist(p3);		// insert query p3-TWB queue
			em.persist(p4);		// insert query p4-TWB queue
			em.persist(p5);		// insert query p5-TWB queue
			em.persist(p6);		// insert query p6-TWB queue
			em.persist(p7);		// insert query p7-TWB queue
			em.persist(p8);		// insert query p8-TWB queue
			// 총 8개의 insert 쿼리가 transactional write behid queue에 큐잉됨
			
			// Synchronize the persistence context to the underlying database.
			em.flush();
		
			et.commit(); // 8개의 insert 쿼리가 순차적으로 연속적으로 DB에게 쿼리 전송!!!
		} catch (RollbackException e) {
			et.rollback();
		} finally {
			
		}		
	}
	
	public static void removePerson(EntityManager em, Long id) {
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		try {
			Person person = em.find(Person.class, id);
			em.remove(person);
			tx.commit();
		} catch (RollbackException e) {
			tx.rollback();
		} finally {
			
		}
	}

    public static void main(String[] args) {

        EntityManagerFactory emf = 
        		Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();
        
        addPersons(em);
        
        removePerson(em, 1L);
        
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            // 1. 비영속(transient) 상태
            Member member = new Member(1L, "John");
            System.out.println("🟡 비영속: " + member);

            // 2. 영속 상태 : 엔티티 매니저가 해당 엔티티 클래스 객체를 관리하겠다...
            // 쓰기 지연 SQL 저장소 : 1st insert query 저장
            em.persist(member); // 이제부터 관리됨
            System.out.println("🟢 영속: persist() 호출 후");

            // 3. flush 없이 조회 (1차 캐시)            
            Member find = em.find(Member.class, 1L);
            System.out.println("🔎 조회된 이름 = " + find.getName());

            // 쓰기 지연 SQL 저장소에 SQL 쿼리를 실행시킬 수 있음.
            em.flush();  
            
            // 4. 준영속 상태
            em.detach(member);  // 관리 중단:1차 캐시에 있는 Member.class + 1L가 삭제됨!!!
            member.setName("변경된 이름"); // 변경해도 반영되지 않음
            System.out.println("🔴 준영속: 이름 수정됨 (DB 반영 X)");

            // 5. 삭제 상태
            // 쓰기 지연 SQL 저장소 : 동일한 2nd insert query 저장
            //em.persist(member);  // persist를 호출하는 것은 전이 상태 위배
            em.merge(member); // 다시 영속화 시킴 
                              // :1차 캐시에 Member.class + 1L의 member instance를 저장
                        
            em.flush(); // flush를 하면 더 이상 1차 캐시에 Member.class + 1L의 
                        // member instance가 없음?
                        // : 아님. flush를 해도 1차 캐시는 유지됨
            
            Member goMember = em.find(Member.class, 1L);
            //em.detach(member);
            em.remove(member);  // select 쿼리가 실행됨?
            System.out.println("⚫ 삭제 상태: remove() 호출됨");

            tx.commit(); // 쓰기 지연 SQL 저장소의 SQL 쿼리가 실행됨...
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
            emf.close();
        }
    }
}
