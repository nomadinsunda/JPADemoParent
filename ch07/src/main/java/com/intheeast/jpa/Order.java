package com.intheeast.jpa;

import lombok.*;

import jakarta.persistence.*;

@Entity
@Setter
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

    public Order(String name, int price) {
        this.name = name;
        this.price = price;
    }
}
