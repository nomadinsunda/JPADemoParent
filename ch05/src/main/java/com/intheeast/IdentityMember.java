package com.intheeast;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PrePersist;
import lombok.*;

@Getter
@Setter
@Entity
public class IdentityMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    public IdentityMember() {}
    public IdentityMember(String name) {
        this.name = name;
    }
    
    // ========== 📌 JPA 생명주기 콜백 ==========
    @PrePersist
    public void beforePersist() {
        System.out.println("📌 @PrePersist 호출됨 - INSERT 전: " + this.name);
    }

    @PostPersist
    public void afterPersist() {
        System.out.println("📌 @PostPersist 호출됨 - INSERT 후: " + this.name);
    }
}
