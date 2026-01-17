package com.intheeast.jpa;

import java.util.List;
import jakarta.persistence.*;

public class ManyToOneUniDirectionalTest {

    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("hello");

    public static void main(String[] args) {
        initData();
//        testLazyLoading();
//        testNPlusOneProblem();
//        testNPlusOneProblemSolvedWithFetchJoin();
//        testForeignKeyConstraint();
//        testChangeProduct();
        emf.close();
    }

    // 🔹 초기 데이터 등록
    private static void initData() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            for (int i = 1; i <= 10; i++) {
                Order order = new Order("상품" + i, 10000 + i);
                em.persist(order);
                
                for (int j =0; j<10; j++) {
                	OrderItem item = new OrderItem(order, i);
                	em.persist(item);
                }
            }

            tx.commit();
        } finally {
            em.close();
        }
    }
    
    
    // 🔹 연관관계 수정 테스트
    private static void testChangeProduct() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            System.out.println("\n🧪 연관관계 변경 테스트");

            Order newOrder = new Order("갤럭시", 1500000);
            em.persist(newOrder); // 1차 캐시에 저장(영속성 컨텍스트안에)

            // orderitem 테이블에 첫번째 row를 쿼리함.
            // JPQL(JPA Query Lanaguae)
            OrderItem item = em.createQuery("select i from OrderItem i", 
            		OrderItem.class)
                    .setMaxResults(1)
                    .getSingleResult();
            // 성공적인 쿼리 수행이 완료되면 orderitem의 첫번째 row의 엔티티 클래스 객체가 만들어져서
            // 1차 캐시에 저장

            System.out.println("🛒 변경 전 상품: " + item.getOrder().getName());

            // order 변경 : Dirty Checking 발생
            item.changeOrder(newOrder); // 연관관계 변경

            // 더티 체킹으로 인해 update 쿼리가 즉시(TWB에 저장되지 않고) 데이터베이스가 전송 
            em.flush();
            
            // 1차 캐시에 캐싱되어 있던 모든 엔티티 클래스 객체를 삭제함
            em.clear();

            // orderitem 테이블에 첫번째 row를 가져오기 위해서 find 메서드를 호출함
            // select 쿼리 전송
            /*
             * select
			        oi1_0.id,
			        oi1_0.product_id,
			        oi1_0.quantity -----> orderitem의 모든 컬럼
			        o1_0.id,
			        o1_0.name,
			        o1_0.price,  ----> order의 모든 컬럼			        
			    from
			        OrderItem oi1_0 
			    join(inner 조인)
			        orders o1_0 
			            on orders.id=OrderItem.product_id(FK:orders.id)
			    where
			        OrderItem.id=1
             */
            // 성공적인 쿼리 수행 후, 1차 캐시에 저장됨
            OrderItem changed = em.find(OrderItem.class, item.getId());
            Order order = changed.getOrder();
            System.out.println("🔄 변경 후 상품: " + order.getName());

            tx.commit();
        } finally {
            em.close();
        }
    }
    
    
    // 🔹 지연 로딩 테스트
    private static void testLazyLoading() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            System.out.println("\n🧪 Lazy Loading 테스트");

            OrderItem item = em.createQuery("select i from OrderItem i", OrderItem.class)
                    .setMaxResults(1)
                    .getSingleResult();

            System.out.println("수량: " + item.getQuantity());
            System.out.println("🕐 상품명 조회 전 - SQL 없음");
            
            /////////////////////////////////////////////////////////////////////////
            System.out.println("상품명: " + item.getOrder().getName()); // 여기서 SQL 발생

            tx.commit();
        } finally {
            em.close();
        }
    }

    // 🔹 N+1 문제 유도 테스트
    private static void testNPlusOneProblem() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            System.out.println("\n🧪 N+1 문제 유도");

            List<OrderItem> items = em.createQuery("select i from OrderItem i", OrderItem.class)
                    .getResultList();

            int count = 0;
            for (OrderItem item : items) {
                count++;
                System.out.println("[" + count + "] 상품명: " + 
                		item.getOrder().getName()); // 여기서 N번 SQL
            }

            tx.commit();
        } finally {
            em.close();
        }
    }

    // 🔹 N+1 문제 해결 : Fetch Join
    private static void testNPlusOneProblemSolvedWithFetchJoin() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            System.out.println("\n✅ N+1 문제 해결 - Fetch Join 사용");

            // 🔹 Product까지 한 번에 조인하여 가져옴
            List<OrderItem> items = em.createQuery(
                "select i from OrderItem i join fetch i.product", OrderItem.class)
                .getResultList();
            // 실제 fetch join은 표준 sql이 아님
            // : jpa에서 정의한 join임...단지 inner join 또는 left outer join을 사용함
            //   team을 즉시[eager] 로딩함!!!
            /*
            select
            	orderitem0_.id as id1_0_0_,
            	product1_.id as id1_1_1_,
            	orderitem0_.product_id as product_3_0_0_,
            	orderitem0_.quantity as quantity2_0_0_,
            	product1_.name as name2_1_1_,
            	product1_.price as price3_1_1_ 
        	from
            	OrderItem orderitem0_ 
        	inner join
            	Product product1_ 
                	on orderitem0_.product_id=product1_.id 
             */

            int count = 0;
            for (OrderItem item : items) {
                count++;
                System.out.println("[" + count + "] 상품명: " + 
                		item.getOrder().getName());  // SQL 발생 없음
            }

            tx.commit();
        } finally {
            em.close();
        }
    }

    // 🔹 외래 키 제약 조건 확인
    private static void testForeignKeyConstraint() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            System.out.println("\n🧪 외래 키 제약 테스트");

            Order order = em.createQuery("select p from Order p", Order.class)
                    .setMaxResults(1)
                    .getSingleResult();
            
            em.remove(order); // 참조 중이므로 삭제 불가 → 예외 발생

            tx.commit();
        } catch (Exception e) {
            System.err.println("🚫 외래키 제약 조건 위반으로 삭제 실패: " + e.getMessage());
            tx.rollback();
        } finally {
            em.close();
        }
    }

    
}
