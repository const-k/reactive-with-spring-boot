package com.example.reactive8server;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

@Getter
@Setter
@EqualsAndHashCode
public class Item {

    private @Id String id;
    private String name;
    private String description;
    private double price;

    private Item() {}

    Item(String name, String description, double price) {
        this.name = name;
        this.description = description;
        this.price = price;
    }

    Item(String id, String name, String description, double price) {
        this(name, description, price);
        this.id = id;
    }
}