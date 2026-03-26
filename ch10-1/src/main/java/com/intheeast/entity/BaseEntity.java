package com.intheeast.entity;

import java.time.LocalDateTime;

import jakarta.persistence.MappedSuperclass;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public class BaseEntity {
	
	private String createdBy;
	
	@CreationTimestamp
	private LocalDateTime creationDate;
	
	private String lastModifiedBy;
	
	@UpdateTimestamp
	private LocalDateTime lastModifiedDate;

}
