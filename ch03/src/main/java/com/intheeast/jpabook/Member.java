package com.intheeast.jpabook;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Transient;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@NoArgsConstructor
//@AllArgsConstructor
@Entity
public class Member {

    @Id
    private Long id;

    private String firstName;
    private String lastName;
    
    @Transient
    private String fullName;
    
    public Member(Long id, String firstName, String lastName) {
    	this.id = id;
    	this.firstName = firstName;
    	this.lastName = lastName;
    }
    
    @PreUpdate // Update 쿼리가 실행되기 전에 호출됨
    public void getPreUpdate() {
    	log.info("%n BeforeUpdate:firstName=" + firstName);
    }
    
    

    @PostLoad
    public void setFullName() {
        fullName = firstName + lastName;
    }

    @PreRemove
    public void logDeletion() {
        log.info("%n Deleting Member: " + fullName);
    }

    
}
