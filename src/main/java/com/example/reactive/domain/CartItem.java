package com.example.reactive.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CartItem { // 구매 상품
    private Item item;
    private int quantity;

    private CartItem() {
    }

    public CartItem(Item item) {
        this.item = item;
        this.quantity = 1;
    }

    public void increment() {
        this.quantity++;
    }

    public void decrement() {
        this.quantity--;
    }
}
