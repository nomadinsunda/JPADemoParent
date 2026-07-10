package com.intheeast;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

public class IdentityStrategyDemo {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            IdentityMember m1 = new IdentityMember("철수");
            IdentityMember m2 = new IdentityMember("영희");
            
            em.persist(m1);
            em.persist(m2);
            
            System.out.println("🌱 생성된 ID1 (IDENTITY): " + m1.getId());
            System.out.println("🌱 생성된 ID2 (IDENTITY): " + m2.getId());
            tx.commit();
        } finally {
            em.close();
            emf.close();
        }
    }
}
