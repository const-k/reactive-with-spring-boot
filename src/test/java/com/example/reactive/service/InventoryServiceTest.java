package com.example.reactive.service;

import com.example.reactive.domain.Cart;
import com.example.reactive.domain.CartItem;
import com.example.reactive.domain.Item;
import com.example.reactive.repository.CartRepository;
import com.example.reactive.repository.ItemRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(SpringExtension.class)
class InventoryServiceTest {
    private InventoryService inventoryService;
    @MockBean private ItemRepository itemRepository;
    @MockBean private CartRepository cartRepository;


    @BeforeEach
    void setUp() {
        Item sampleItem = new Item("item1", "TV tray", "Alf TV tray", 19.99);
        CartItem sampleCartItem = new CartItem(sampleItem);
        Cart sampleCart = new Cart("My Cart", Collections.singletonList(sampleCartItem));

        when(cartRepository.findById(anyString())).thenReturn(Mono.empty());
        when(itemRepository.findById(anyString())).thenReturn(Mono.just(sampleItem));
        when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(sampleCart));

        inventoryService = new InventoryService(itemRepository, cartRepository);
    }

    @Test
    void addItemToEmptyCartShouldProduceOneCartItem() { // <1>
        inventoryService.addItemToCart("My Cart", "item1") // <2>
            .as(StepVerifier::create) // <3> StepVerifier (리액터 테스트 도구)가 대신 구독을 하고 값을 확인할 수 있게 해줌
            .expectNextMatches(cart -> { // <4>
                assertThat(cart.getCartItems()).extracting(CartItem::getQuantity) //
                    .containsExactlyInAnyOrder(1); // <5>

                assertThat(cart.getCartItems()).extracting(CartItem::getItem) //
                    .containsExactly(new Item("item1", "TV tray", "Alf TV tray", 19.99)); // <6>

                return true; // <7>
            }) //
            .verifyComplete(); // <8> onComplete 시그널 확인
    }

    @Test
    void alternativeWayToTest() { // <1>
        StepVerifier.create( //
                             inventoryService.addItemToCart("My Cart", "item1")) //
            .expectNextMatches(cart -> { // <4>
                assertThat(cart.getCartItems()).extracting(CartItem::getQuantity) //
                    .containsExactlyInAnyOrder(1); // <5>

                assertThat(cart.getCartItems()).extracting(CartItem::getItem) //
                    .containsExactly(new Item("item1", "TV tray", "Alf TV tray", 19.99)); // <6>

                return true; // <7>
            }) //
            .verifyComplete(); // <8>
    }
}