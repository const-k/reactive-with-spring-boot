package com.example.reactive;//package com.example.reactive;
//
//import com.example.reactive.domain.Cart;
//import com.example.reactive.domain.CartItem;
//import com.example.reactive.domain.Item;
//import com.example.reactive.repository.CartRepository;
//import com.example.reactive.repository.ItemRepository;
//import com.example.reactive.service.AltInventoryService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import reactor.core.publisher.Mono;
//import reactor.test.StepVerifier;
//
//import java.time.Duration;
//import java.util.Collections;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(SpringExtension.class)
//public class BlockingHoundIntegrationTest {
//    AltInventoryService inventoryService;
//
//    @MockBean ItemRepository itemRepository;
//    @MockBean CartRepository cartRepository;
//
//    @BeforeEach
//    void setUp() {
//        Item sampleItem = new Item("item1", "TV tray", "Alf TV tray", 19.99);
//        CartItem sampleCartItem = new CartItem(sampleItem);
//        Cart sampleCart = new Cart("My Cart", Collections.singletonList(sampleCartItem));
//
//        when(cartRepository.findById(anyString()))
//            .thenReturn(Mono.<Cart>empty().hide()); // 리액터는 필요하지 않다면 블로킹 호출을 알아서 삭제함
//                                                    // inventoryService 에서 장바구니가 비어있을 경우 장바구니를 추가한 후
//                                                    // block 을 호출하는데 리액터에서 삭제하므로 hide 로 리액터의 최적화 방지
//
//        when(itemRepository.findById(anyString())).thenReturn(Mono.just(sampleItem));
//        when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(sampleCart));
//
//        inventoryService = new AltInventoryService(itemRepository, cartRepository);
//    }
//
//    @Test
//    void blockHoundShouldTrapBlockingCall() {
//        Mono.delay(Duration.ofSeconds(1))
//            .flatMap(tick -> inventoryService.addItemToCart("My Cart", "item1"))
//            .as(StepVerifier::create)
//            .verifyErrorSatisfies(throwable -> {
//                assertThat(throwable).hasMessageContaining("block()/blockFirst()/blockLast() are blocking");
//            });
//    }
//}
