package com.intheeast.jpa;

import lombok.*;

import jakarta.persistence.*;

@Entity
@Setter
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
//@ToString(exclude = "employee")
public class Empolyee {
	
	// 연관관계 : Many to One 
	// 엔티티 그래프에서는 단방향 : Empolyee -> Department 참조하고 있음.

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "department_id"/*, nullable = false*/) // 
    private Department department;
       

    public Empolyee(String name, Department department) {
        this.department = department;
        this.name = name;
    }
    
    public Long getId() {
    	return this.id;
    }
    
    public String getName() {
    	return this.name;
    }
    
    public Department getDepartment() {
    	return this.department;
    }

    public void changeDepartment(Department department) {
        this.department = department;
    }
}
