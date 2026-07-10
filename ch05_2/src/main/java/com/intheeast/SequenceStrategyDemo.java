package com.intheeast;

import jakarta.persistence.*;

public class SequenceStrategyDemo {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            for (int i=0; i<100; i++) {
            	SequenceMember m = new SequenceMember("영희");
            	em.persist(m);
            	
            	System.out.println("🌱 생성된 ID (SEQUENCE): " + m.getId());
            }
            
            tx.commit();
        } finally {
            em.close();
            emf.close();
        }
    }
}