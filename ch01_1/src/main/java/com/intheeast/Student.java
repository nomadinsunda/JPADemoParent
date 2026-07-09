package com.intheeast;

import jakarta.persistence.*;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Student {
	
	@Id
	private Long id;
	
	private String name;

}
