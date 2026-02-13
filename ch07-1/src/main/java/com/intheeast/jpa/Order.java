package com.intheeast.jpa;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
//@Setter : 사용하면 안됨
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@Table(name="orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private int price;
    
    // Aggregate Model에서 Order가 Root 임을 정의
    // : cascade=CascadeType.ALL, orphanRemoval=true
    @OneToMany(mappedBy = "order", fetch=FetchType.EAGER,
    		cascade=CascadeType.ALL, orphanRemoval=true)
    List<OrderItem> items = new ArrayList<>();
    
    public void removeElement(int index) {
    	OrderItem removedItem = items.remove(index);
    }

    public Order(String name, int price) {
        this.name = name;
        this.price = price;
    }
    
    // 양방향 편의 메서드: @Setter 사용하지 못함
    public void addOrderItem(OrderItem orderItem) {
    	items.add(orderItem);
    	//////////////////////////////
    	orderItem.setOrder(this);
    	//////////////////////////////
    }

	public void setName(String name) {
		this.name = name;
	}

	public void setPrice(int price) {
		this.price = price;
	}   
    
}
