package com.intheeast.jpa;


import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;

public class BiDirectionalTest {

    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("hello");

    public static void main(String[] args) {
//        initData();                // 초기 데이터 저장
//    	List<Long> gotData = initData2();
//    	initData3();
//    	persistWithoutConvenienceMethod();
//    	initWithoutConvenienceMethod();
//        checkDataConsistency();
//    	printTeamNamesForMemberIds(gotData);
//        testLazyLoading();        // 지연 로딩 확인
//        testOrphanRemoval();      // 고아 객체 삭제 확인
//        testChangeRelation();     // 연관관계 변경 확인
//        testNPlusOne();           // N+1 문제 확인
//        testFetchJoinSolution();  // N+1 문제 해결
        
//        createOrderWithItems();
//        updateOrderItems();
//        removeAndReAdd();
    	
    	initTestData();
    	printItem();
//    	selectiveUpdateItem();
//    	conditionalDeleteItem();
//    	appendNewItems();
        emf.close();
    }

    // 🔸 초기 데이터 저장
    private static void initData() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Order order = new Order("이순신");
//            em.persist(order); // 영속성 상태가 아님
            
            order.addItem(new OrderItem("모니터", 2));
            order.addItem(new OrderItem("마우스", 1));
            
            // 다음 설정으로 인해 insert 쿼리가 즉시 db에게 전송됨
            // : db의 indentity로부터 id를 얻기 위해서...
            // @GeneratedValue(strategy = GenerationType.IDENTITY)
            // 또한 cascade = CascadeType.ALL 설정으로 인해
            // 부모 엔티티 클래스 인스턴스가 영속화 되면 자식 클래스 인스턴스도 영속화됨
            em.persist(order); // cascade에 의해 item도 함께 저장됨

            tx.commit();
        } finally {
            em.close();
        }
    }
    
    private static List<Long> initData2() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        List<Long> retOrderItemIds = null;
        try {
            tx.begin();
            
            OrderItem item1 = new OrderItem("모니터", 2);

            // Order는 아직 persist()되지 않은 transient 상태
            Order order = new Order("이순신");
            
            item1.setOrder(order);
            order.addItem(item1);
            
            em.persist(item1); // order가 아직 transient 상태이므로 외래키를 참조할 수 없음.           

            tx.commit();
        } finally {
            em.close();
        }
        
        return retOrderItemIds;
    }
    
    private static List<Long> initData3() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        List<Long> retOrderItemIds = null;
        try {
            tx.begin();
            
            OrderItem item1 = new OrderItem("모니터", 2);
            OrderItem item2 = new OrderItem("마우스", 1);

            Order order = new Order("이순신"); 
            // flush를 하지 않아도 곧바로 insert 쿼리가 전송
            em.persist(order); // 바로 insert 쿼리가 전송
            
            em.persist(item1); // 바로 insert 쿼리가 전송
            em.persist(item2); // 바로 insert 쿼리가 전송
            
            item1.setOrder(order);
            item2.setOrder(order);
            
            //////////////////////////////////////////////////////
            
            System.out.println(item1.toString());
            System.out.println(item2.toString());            
            
            List<Long> orderItemIds = new ArrayList<>();
            orderItemIds.add(item1.getId());
            //////////////////////////////////
            // item1.getId를 호출함으로써, orderItemIds에는 [1, 1]라는
            // 동일한 엘리먼트를 가지고 있었음
            orderItemIds.add(item2.getId());
            
            retOrderItemIds = orderItemIds;

            em.flush();
            em.clear();

            OrderItem gotOrderItem = em.find(OrderItem.class, orderItemIds.get(0));
            System.out.println(gotOrderItem.toString());
            

            tx.commit();
        } finally {
            em.close();
        }
        
        return retOrderItemIds;
    }
    
    private static void persistWithoutConvenienceMethod() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Order order = new Order("김지영");

            OrderItem item1 = new OrderItem("SSD", 1);
            OrderItem item2 = new OrderItem("RAM", 2);

            // 주인 쪽 관계 미설정
            order.getOrderItems().add(item1);
            order.getOrderItems().add(item2);

            // order 설정하지 않음
            // item1.setOrder(order); 
            // item2.setOrder(order);

            em.persist(order); // cascade로 item 저장 시도됨
            tx.commit();

            System.out.println("# 편의 메서드 미사용 시: " + order.getOrderItems());
            System.out.println("# 각 OrderItem의 order 필드 확인: " + item1.getOrder() + ", " + item2.getOrder());

        } finally {
            em.close();
        }
    }
    
    private static void initWithoutConvenienceMethod() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Order order = new Order("김지영");

            OrderItem item = new OrderItem("진라면", 1);

            // setOrder 호출로 연관관계 주인 필드만 설정
            item.setOrder(order);

            // order.getOrderItems().add(item); 는 생략 (역방향 미설정)

            em.persist(order); 
            em.persist(item);

            tx.commit();

        } finally {
            em.close();
        }
    }

    // 저장된 데이터를 읽어들이며 불일치 확인
    private static void checkDataConsistency() {
        EntityManager em = emf.createEntityManager();

        try {
            Order order = em.find(Order.class, 1L);
            System.out.println(" Order 객체 조회: " + order);
            System.out.println(" 포함된 OrderItem 목록: " + order.getOrderItems());

            OrderItem item = em.find(OrderItem.class, 1L);
            System.out.println(" OrderItem 객체 조회: " + item);
            System.out.println(" item.getOrder(): " + item.getOrder());

        } finally {
            em.close();
        }
    }
    
    public static void printTeamNamesForMemberIds(List<Long> orderItemIds) {

    	System.out.println("printTeamNamesForMemberIds");
    	EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        
        List<OrderItem> items = em.createQuery("Select o from OrderItem o", OrderItem.class)
        		.getResultList();
        
        for (OrderItem item : items) {
        	System.out.println("###############################################3");
        	Order order = item.getOrder();
        	System.out.println("###############################################3");
        	System.out.println(order);
        }

        try {
            tx.begin();
	
//            Order gOrder = em.find(Order.class, 1L);
//            gOrder.getId();
            
	        List<OrderItem> orderItems = new ArrayList<>();
	        List<Order> orders = new ArrayList<>();
	        
	        for (Long orderItemId : orderItemIds) {
	        	// 자식[OrderItem]측에서 부모[Order]를 찾는 방법
	        	OrderItem orderItem = em.find(OrderItem.class, orderItemId.longValue()); // LEFT Outer Join!!!
	            System.out.println("got Member Entity Class Object");
	            orderItems.add(orderItem);
	            /////////////////////////////////////
	            Long orderId = orderItem.getOrder().getId(); 
	            
	            boolean isAlready = false;
	            int isAlreadyId = -1;
	            for (Order got : orders) {
	            	if (orderId == got.getId()) {
	            		isAlready = true;
	            		isAlreadyId = orders.indexOf(got);
	            		break;
	            	} 
	            }
	            
	            if (!isAlready) {
	            	Order order = em.find(Order.class, orderId.longValue());
		            //order.addItem(orderItem); // 이미 order에는 orderItems에 있음
		            
		            orders.add(order);
	            } else {
	            	Order order = orders.get(isAlreadyId);
	            	//order.addItem(orderItem);
	            }
	        }
	
	        System.out.println("***************************************************");
	
	        for (Order order : orders) {
	            System.out.printf("팀 이름:%s", order.getCustomer());
	            System.out.println("\n");
	
	            ////////////////////////////////////////////////////////
	            for (OrderItem orderItem : order.getOrderItems()) {
	                System.out.printf("          멤버 ID:%d, 멤버 이름:%S", orderItem.getId(), orderItem.getProduct());
	                System.out.println("\n");
	            }
	            /////////////////////////////////////////////////
	        }
	
	        tx.commit();
        } catch (Exception e) {
        	
        } finally {
        	em.close();
        }

    }

    // 🔸 지연 로딩 확인
    private static void testLazyLoading() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            System.out.println("\n🧪 [지연 로딩 테스트]");

            Order order = em.createQuery("select o from Order o", Order.class)
                            .setMaxResults(1)
                            .getSingleResult();

            System.out.println("📦 주문자: " + order.getCustomer());
            System.out.println("📦 주문 항목 조회 전 (SQL X)");

            for (OrderItem item : order.getOrderItems()) {
                System.out.println(" - " + item.getProduct() + " x " + item.getQuantity());
            }

            tx.commit();
        } finally {
            em.close();
        }
    }

    // 🔸 고아 객체 제거 확인
    private static void testOrphanRemoval() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            System.out.println("\n🧪 [고아 객체 제거 테스트]");

            Order order = em.createQuery("select o from Order o", Order.class)
                            .setMaxResults(1)
                            .getSingleResult();

            OrderItem removed = order.getOrderItems().get(0);
            order.removeItem(removed); // orphanRemoval = true → 자동 삭제

            tx.commit();
        } finally {
            em.close();
        }
    }

    // 🔸 연관관계 변경 테스트
    private static void testChangeRelation() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            System.out.println("\n🧪 [연관관계 변경 테스트]");

            Order newOrder = new Order("장보고");
            em.persist(newOrder);

            OrderItem item = em.createQuery("select i from OrderItem i", OrderItem.class)
                               .setMaxResults(1)
                               .getSingleResult();

            // 기존 연관관계 제거
            item.getOrder().removeItem(item);

            // 새 연관관계 설정
            newOrder.addItem(item);  // 연관관계 편의 메서드로 양방향 유지

            tx.commit();
        } finally {
            em.close();
        }
    }

    // 🔸 N+1 문제 유도
    private static void testNPlusOne() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            System.out.println("\n🧪 [N+1 문제 유도]");

            List<Order> orders = em.createQuery("select o from Order o", Order.class)
                                   .getResultList();

            for (Order order : orders) {
                for (OrderItem item : order.getOrderItems()) {
                    System.out.println(" - " + item.getProduct());
                }
            }

            tx.commit();
        } finally {
            em.close();
        }
    }
    
 // 🔸 Fetch Join을 이용한 N+1 해결
    private static void testFetchJoinSolution() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            System.out.println("\n🧪 [N+1 문제 해결 - Fetch Join 사용]");

            List<Order> orders = em.createQuery(
                    "select o from Order o join fetch o.orderItems", Order.class)
                    .getResultList();

            for (Order order : orders) {
                System.out.println("🧾 고객: " + order.getCustomer());
                for (OrderItem item : order.getOrderItems()) {
                    System.out.println(" - " + item.getProduct() + " x " + item.getQuantity());
                }
            }

            tx.commit();
        } finally {
            em.close();
        }
    }
    
    private static void createOrderWithItems() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Order order = new Order("김지철");
            order.addItem(new OrderItem("키보드", 1));
            order.addItem(new OrderItem("마우스", 2));
            order.addItem(new OrderItem("모니터", 1));

            em.persist(order);  // cascade: OrderItem도 함께 저장

            tx.commit();
        } finally {
            em.close();
        }
    }
    
    // 변경 감지 테스트 (수량 변경)
    private static void updateOrderItems() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Order order = em.find(Order.class, 1L);
            for (OrderItem item : order.getOrderItems()) {
                if (item.getProduct().equals("마우스")) {
                    item.setQuantity(5); // 변경 감지
                }
            }

            tx.commit();
        } finally {
            em.close();
        }
    }

    
    // 리스트 초기화 후 재등록 테스트
    private static void removeAndReAdd() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            System.out.println("+removeAndReAdd");

            Order order = em.find(Order.class, 1L);

            // 기존 아이템 전부 제거 (orphanRemoval)
            for (OrderItem item : new ArrayList<>(order.getOrderItems())) {
                order.removeItem(item);
            }

            // 새로운 아이템 추가
            order.addItem(new OrderItem("SSD", 1));
            order.addItem(new OrderItem("RAM", 2));

            tx.commit();
            System.out.println("-removeAndReAdd");
        } finally {
            em.close();
        }
    }
    
    public static void initTestData() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Order order = new Order("김지철");
            order.addItem(new OrderItem("RAM", 2));
            order.addItem(new OrderItem("SSD", 1));
            order.addItem(new OrderItem("HDD", 3));
            order.addItem(new OrderItem("Mouse", 1));

            em.persist(order); // CascadeType.ALL + 편의 메서드로 아이템까지 자동 저장

            tx.commit();

        } finally {
            em.close();
        }
    }
    
    private static void printItem() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Order order = em.find(Order.class, 1L);
            for (OrderItem item : order.getOrderItems()) {
                System.out.println("Item:" + item);

            }
            tx.commit();
        } finally {
            em.close();
        }
    }
    
    private static void selectiveUpdateItem() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Order order = em.find(Order.class, 1L);
            order.getOrderItems().stream()
                    .filter(item -> item.getProduct().equals("HDD"))
                    .findFirst()
                    .ifPresent(item -> item.setQuantity(item.getQuantity() + 1));

            tx.commit();
            System.out.println("'HDD' 수량 +1 증가 완료");
        } finally {
            em.close();
        }
    }

    // 수량이 1개 이하인 상품 제거
    private static void conditionalDeleteItem() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Order order = em.find(Order.class, 1L);
            List<OrderItem> toRemove = order.getOrderItems().stream()
                    .filter(item -> item.getQuantity() <= 1)
                    .toList();

            toRemove.forEach(order::removeItem); // orphanRemoval로 DB에서도 삭제됨

            tx.commit();
            System.out.println("수량 1 이하인 상품 삭제 완료");
        } finally {
            em.close();
        }
    }

    // 이미 등록된 아이템을 유지하고 새로 추가
    private static void appendNewItems() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Order order = em.find(Order.class, 1L);

            // 중복 검사 없이 새 상품 추가
            order.addItem(new OrderItem("그래픽카드", 1));
            order.addItem(new OrderItem("CPU", 1));

            tx.commit();
            System.out.println(" '그래픽카드'와 'CPU' 추가 완료");
        } finally {
            em.close();
        }
    }

}
