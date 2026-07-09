package com.intheeast.jpabook;

import jakarta.persistence.*;

@Entity
public class Member {  // Member 클래스는 member라는  테이블과 매핑
	                   // Memeber 클래스 인스턴스는 member 테이블의 한 row와 매핑

    @Id  // 이 필드와 매핑되는 컬럼은 PK 지정된다는 것을 의미
    private Long id;
    
    
    private String name;

    // Getter & Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
