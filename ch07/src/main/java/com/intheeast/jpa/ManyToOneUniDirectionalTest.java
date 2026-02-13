package com.intheeast.jpa;

import java.util.List;
import jakarta.persistence.*;

public class ManyToOneUniDirectionalTest {

    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("hello");
    
    
    private static Department testDep;

    public static void main(String[] args) {
        initData();
        testDepartmentEC();
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
                Department department = new Department("부서" + i, 10 + i);
                em.persist(department);
                
                for (int j =0; j<10; j++) {
                	Empolyee empolyee = new Empolyee(String.valueOf(i), department);
                	em.persist(empolyee);
                }
            }

            tx.commit();
        } finally {
            em.close();
        }
    }
    
    // 
    private static void testDepartmentEC() {
    	EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            
            Department department = new Department("신규부서", 30);
            em.persist(department);
            
            em.flush();
            
            // 1차 캐시에 캐싱되어 있던 모든 엔티티 클래스 객체를 삭제함
            em.clear();
            
            Department changed = em.find(Department.class, department.getId());

            tx.commit();
            
            
        } finally {
        	
        }
    	
    }
    
    
    // 🔹 연관관계 수정 테스트
    private static void testChangeProduct() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            System.out.println("\n🧪 연관관계 변경 테스트");

            Department newDepartment = new Department("새부서", 50);
            em.persist(newDepartment); // 1차 캐시에 저장(영속성 컨텍스트안에)

            // orderitem 테이블에 첫번째 row를 쿼리함.
            // JPQL(JPA Query Lanaguae)
            Empolyee employe = em.createQuery("select i from Empolyee i", 
            		Empolyee.class)
                    .setMaxResults(1)
                    .getSingleResult();
            // 성공적인 쿼리 수행이 완료되면 orderitem의 첫번째 row의 엔티티 클래스 객체가 만들어져서
            // 1차 캐시에 저장

            System.out.println("🛒 변경 전 부서: " + employe.getDepartment().getName());

            // order 변경 : Dirty Checking 발생
            employe.changeDepartment(newDepartment); // 연관관계 변경

            // 더티 체킹으로 인해 update 쿼리가 즉시(TWB에 저장되지 않고) 데이터베이스가 전송 
            em.flush();
            
            // 1차 캐시에 캐싱되어 있던 모든 엔티티 클래스 객체를 삭제함
            em.clear();

            // 성공적인 쿼리 수행 후, 1차 캐시에 저장됨
            Empolyee changed = em.find(Empolyee.class, employe.getId());
            Department department = changed.getDepartment();
            System.out.println("🔄 변경 후 부서: " + department.getName());

            tx.commit();
        } finally {
            em.close();
        }
    }
    
    
    // 🔹 지연 로딩 테스트
//    private static void testLazyLoading() {
//        EntityManager em = emf.createEntityManager();
//        EntityTransaction tx = em.getTransaction();
//        try {
//            tx.begin();
//            System.out.println("\n🧪 Lazy Loading 테스트");
//
//            OrderItem item = em.createQuery("select i from OrderItem i", OrderItem.class)
//                    .setMaxResults(1)
//                    .getSingleResult();
//
//            System.out.println("수량: " + item.getQuantity());
//            System.out.println("🕐 상품명 조회 전 - SQL 없음");
//            
//            /////////////////////////////////////////////////////////////////////////
//            System.out.println("상품명: " + item.getOrder().getName()); // 여기서 SQL 발생
//
//            tx.commit();
//        } finally {
//            em.close();
//        }
//    }
//
//    // 🔹 N+1 문제 유도 테스트
//    private static void testNPlusOneProblem() {
//        EntityManager em = emf.createEntityManager();
//        EntityTransaction tx = em.getTransaction();
//        try {
//            tx.begin();
//            System.out.println("\n🧪 N+1 문제 유도");
//
//            List<OrderItem> items = em.createQuery("select i from OrderItem i", OrderItem.class)
//                    .getResultList();
//
//            int count = 0;
//            for (OrderItem item : items) {
//                count++;
//                System.out.println("[" + count + "] 상품명: " + 
//                		item.getOrder().getName()); // 여기서 N번 SQL
//            }
//
//            tx.commit();
//        } finally {
//            em.close();
//        }
//    }
//
//    // 🔹 N+1 문제 해결 : Fetch Join
//    private static void testNPlusOneProblemSolvedWithFetchJoin() {
//        EntityManager em = emf.createEntityManager();
//        EntityTransaction tx = em.getTransaction();
//        try {
//            tx.begin();
//            System.out.println("\n✅ N+1 문제 해결 - Fetch Join 사용");
//
//            // 🔹 Product까지 한 번에 조인하여 가져옴
//            List<OrderItem> items = em.createQuery(
//                "select i from OrderItem i join fetch i.product", OrderItem.class)
//                .getResultList();
//            // 실제 fetch join은 표준 sql이 아님
//            // : jpa에서 정의한 join임...단지 inner join 또는 left outer join을 사용함
//            //   team을 즉시[eager] 로딩함!!!
//            /*
//            select
//            	orderitem0_.id as id1_0_0_,
//            	product1_.id as id1_1_1_,
//            	orderitem0_.product_id as product_3_0_0_,
//            	orderitem0_.quantity as quantity2_0_0_,
//            	product1_.name as name2_1_1_,
//            	product1_.price as price3_1_1_ 
//        	from
//            	OrderItem orderitem0_ 
//        	inner join
//            	Product product1_ 
//                	on orderitem0_.product_id=product1_.id 
//             */
//
//            int count = 0;
//            for (OrderItem item : items) {
//                count++;
//                System.out.println("[" + count + "] 상품명: " + 
//                		item.getOrder().getName());  // SQL 발생 없음
//            }
//
//            tx.commit();
//        } finally {
//            em.close();
//        }
//    }
//
//    // 🔹 외래 키 제약 조건 확인
//    private static void testForeignKeyConstraint() {
//        EntityManager em = emf.createEntityManager();
//        EntityTransaction tx = em.getTransaction();
//        try {
//            tx.begin();
//            System.out.println("\n🧪 외래 키 제약 테스트");
//
//            Order order = em.createQuery("select p from Order p", Order.class)
//                    .setMaxResults(1)
//                    .getSingleResult();
//            
//            em.remove(order); // 참조 중이므로 삭제 불가 → 예외 발생
//
//            tx.commit();
//        } catch (Exception e) {
//            System.err.println("🚫 외래키 제약 조건 위반으로 삭제 실패: " + e.getMessage());
//            tx.rollback();
//        } finally {
//            em.close();
//        }
//    }

    
}
