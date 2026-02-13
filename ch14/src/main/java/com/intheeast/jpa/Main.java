package com.intheeast.jpa;

import java.time.LocalDate;
import java.util.Random;
import javax.persistence.*;

public class Main {
    
    static final String[] cities = {"Seoul", "Busan", "Incheon", "Daegu", "Daejeon"};
    static final String[] jobs = {"Developer", "Designer", "Manager", "Analyst", "Writer"};
    static final Random random = new Random();

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        // [1] 데이터 대량 삽입 (10만 건)
        em.getTransaction().begin();
        System.out.println("💾 데이터 삽입 중...");
        for (int i = 0; i < 1_000_000; i++) {
            String email = "user" + i + "@example.com";
            String name = "User" + i;
            String phone = "010-" + (1000 + random.nextInt(9000)) + "-" + (1000 + random.nextInt(9000));
            String city = cities[random.nextInt(cities.length)];
            String job = jobs[random.nextInt(jobs.length)];
            LocalDate birth = LocalDate.of(1970 + random.nextInt(30), 1 + random.nextInt(12), 1 + random.nextInt(28));
            
            em.persist(new User(email, name, phone, city, job, birth));

            if (i % 1000 == 0) {
                em.flush();  // 이 부분에서 배치 처리가 수행됨.
                em.clear();
            }
        }
        em.getTransaction().commit();
        System.out.println("✅ 데이터 삽입 완료.");

        // [2] 워밍업 (DB 연결 및 기본적인 로딩 완료)
        warmUp(em);

        // [3] 성능 측정 시작
        System.out.println("\n--- 📊 성능 측정 결과 (5회 반복 평균) ---");

        // ❌ 인덱스 미사용 (Full Scan) - 캐시 선점 방지를 위해 먼저 실행
        double avgFullScan = measurePerformance(em, "name", "User99999");
        System.out.printf("❌ [Full Scan]   평균 소요 시간: %.4f ms\n", avgFullScan);

        // 🔍 인덱스 사용 (Index Scan)
        double avgIndexScan = measurePerformance(em, "email", "user99999@example.com");
        System.out.printf("🔍 [Index Scan]  평균 소요 시간: %.4f ms\n", avgIndexScan);

        // 성능 차이 계산
        System.out.printf("\n🚀 인덱스 사용 시 약 %.1f배 빠름\n", (avgFullScan / avgIndexScan));

        em.close();
        emf.close();
    }

    /**
     * @param field 검색할 필드 (name 또는 email)
     * @param value 검색할 값
     * @return 평균 소요 시간 (ms)
     */
    private static double measurePerformance(EntityManager em, String field, String value) {
        int iterations = 5;
        long totalDuration = 0;

        for (int i = 0; i < iterations; i++) {
            em.clear(); // 1차 캐시(영속성 컨텍스트) 비우기
            
            long start = System.nanoTime();
            
            // 쿼리 실행
            em.createQuery("SELECT u FROM User u WHERE u." + field + " = :val", User.class)
              .setParameter("val", value)
              .getResultList();
            
            long end = System.nanoTime();
            totalDuration += (end - start);         
            
        }

        // 나노초를 밀리초로 변환하여 평균 산출
        return (totalDuration / (double) iterations) / 1_000_000.0;
    }

    private static void warmUp(EntityManager em) {
        // DB 연결 활성화 및 커넥션 풀 예열
        em.createQuery("SELECT count(u) FROM User u").getSingleResult();
        em.clear();
    }
}