package com.intheeast;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

public class Main {
	
	public static void makeEObject(EntityManagerFactory ef) {
		
		EntityManager em = 
				ef.createEntityManager();
		
		EntityTransaction et = em.getTransaction();
		
		try {
			et.begin();
			
			Student student1 = new Student();
			student1.setId(1L);
			student1.setName("kris");
			
			Student student2 = new Student();
			student2.setId(2L);
			student2.setName("john");
			
			Student student3 = new Student();
			student3.setId(3L);
			student3.setName("choi");
			
			em.persist(student1);
			em.persist(student2);
			em.persist(student3);
			
			et.commit();
		} catch(Exception e) {
			et.rollback();
		} finally {
			em.close();
		}
		
	}
	
	public static void getEObject(EntityManagerFactory ef) {
		
		EntityManager em = 
				ef.createEntityManager();
		
		EntityTransaction et = em.getTransaction();
		
		try {
			et.begin();
			
			Student student1 = em.find(Student.class, 1L);
			Student student2 = em.find(Student.class, 2L);
			Student student3 = em.find(Student.class, 3L);
			
			et.commit();
		} catch(Exception e) {
			et.rollback();
		} finally {
			em.close();
		}

		
	}

	public static void main(String[] args) {

		EntityManagerFactory ef = 
				Persistence.createEntityManagerFactory("hello");
		
		makeEObject(ef);
		
		getEObject(ef);
	}

}
