package com.example.reactive.domain;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Dish {
    private String description;
    private boolean delivered = false;

    public Dish(String description) {
        this.description = description;
    }

    public static Dish deliver(Dish dish) {
        Dish deliveredDish = new Dish(dish.description);
        deliveredDish.delivered = true;
        return deliveredDish;
    }
}
