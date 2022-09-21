package com.example.reactive.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
public class Cart { // 장바구니
    private @Id String id;
    private List<CartItem> cartItems;

    private Cart() {
    }

    public Cart(String id) {
        this(id, new ArrayList<>());
    }
}
