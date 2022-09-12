package com.example.reactive.controller;

import com.example.reactive.domain.Cart;
import com.example.reactive.domain.Item;
import com.example.reactive.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Controller
public class HomeController {
    private final InventoryService inventoryService;

    @GetMapping
    Mono<Rendering> home() { // <1>
        return Mono.just(Rendering.view("home.html") // <2>
                             .modelAttribute("items", this.inventoryService.getInventory()) // <3>
                             .modelAttribute("cart", this.inventoryService.getCart("My Cart") // <4>
                                 .defaultIfEmpty(new Cart("My Cart")))
                             .build());
    }
    // end::2[]

    @PostMapping("/add/{id}")
    Mono<String> addToCart(@PathVariable String id) {
        return this.inventoryService.addItemToCart("My Cart", id)
            .thenReturn("redirect:/");
    }

    @DeleteMapping("/remove/{id}")
    Mono<String> removeFromCart(@PathVariable String id) {
        return this.inventoryService.removeOneFromCart("My Cart", id)
            .thenReturn("redirect:/");
    }

    @PostMapping
    Mono<String> createItem(@ModelAttribute Item newItem) {
        return this.inventoryService.saveItem(newItem) //
            .thenReturn("redirect:/");
    }

    @DeleteMapping("/delete/{id}")
    Mono<String> deleteItem(@PathVariable String id) {
        return this.inventoryService.deleteItem(id) //
            .thenReturn("redirect:/");
    }
}
