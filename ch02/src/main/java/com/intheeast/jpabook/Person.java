package com.intheeast.jpabook;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Person {
	
	@Id
	private Long id;
	
	private String name;
	
	private String phone;

}
