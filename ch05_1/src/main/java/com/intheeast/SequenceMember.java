package com.intheeast;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;

import lombok.*;

@Getter
@Setter
@Entity
@SequenceGenerator(
    name = "member_seq_generator",
    sequenceName = "member_seq", // DB 시퀀스 이름
    initialValue = 1,
    allocationSize = 50
)
public class SequenceMember {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, 
    	generator = "member_seq_generator")
    private Long id;

    private String name;

    public SequenceMember() {}
    public SequenceMember(String name) {
        this.name = name;
    }
}
