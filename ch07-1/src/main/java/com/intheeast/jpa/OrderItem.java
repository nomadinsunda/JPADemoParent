package com.intheeast.jpa;

import lombok.*;

import jakarta.persistence.*;

@Entity
//@Setter
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
//@ToString(exclude = "orderitem")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int quantity;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name = "orders_id", nullable = false) // OrderItem 테이블의 FK
    private Order order; // FK키와 매핑
    
    public void changeOrder(Order newOrder) {
        this.order = newOrder;
    }

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public void setOrder(Order order) {
		this.order = order;
	}
}
