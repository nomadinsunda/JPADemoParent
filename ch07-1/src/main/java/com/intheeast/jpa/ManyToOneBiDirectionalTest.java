package com.intheeast.jpa;

import java.util.List;
import jakarta.persistence.*;

public class ManyToOneBiDirectionalTest {

    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("hello");

    public static void main(String[] args) {
        initData();
        testChangeProduct();
//        testLazyLoading();
//        testNPlusOneProblem();
//        testNPlusOneProblemSolvedWithFetchJoin();
//        testForeignKeyConstraint();
//        testChangeProduct();
        emf.close();
    }
    
    private static Long orderId;
    private static Long item1Id;
    private static Long item2Id;

    // 🔹 초기 데이터 등록
    private static void initData() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Order order = new Order();
            order.setName("Order-2026/01/06");
            order.setPrice(50000);
            
            OrderItem item1 = new OrderItem();
            item1.setQuantity(10);
            order.addOrderItem(item1); // 양방향 편의 메서드 : 외부에서 Aggregate Root를 통해
                                       //                자식들을 수정/변경할 수 없도록 하기 위해
                        
            OrderItem item2 = new OrderItem();
            item2.setQuantity(10);
            order.addOrderItem(item2);
            
            em.persist(order);
//            em.persist(item1); // 1차 캐시 저장하고 TWB에 Insert를 저장
//            em.persist(item2);
            
            em.flush(); // flush는 commit 아니고, 단지 TWB에 저장되어 있는 쿼리가 DB에 전송되도록 함.
                        // : commit이 아니기 때문에 전송된 데이터가 영속화되지 않음
            
            orderId = order.getId();
            item1Id = item1.getId();
            item2Id = item2.getId();           
            
//            Order findOrder = em.find(Order.class, order.getId());
//            List<OrderItem> items = findOrder.getItems();
//            
//            em.clear();           

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
            OrderItem findOrderItem = em.find(OrderItem.class, item1Id);
            if(item1Id == findOrderItem.getId()) {
            	
            }
            //tx.commit();
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
