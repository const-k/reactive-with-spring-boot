package com.example.reactive.controller;

import com.example.reactive.domain.Cart;
import com.example.reactive.domain.Item;
import com.example.reactive.repository.ItemRepository;
import com.example.reactive.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;

@WebFluxTest(controllers = ApiItemController.class) // 웹 플럭스 컨트롤러 테스트에 필요한 내용만 자동설정, ApiItemController에서 필요로하는 빈들은 Mock 으로 주입
@AutoConfigureRestDocs // 스프링 레스트 독에 필요한 내용을 자동으로 설정해줌
class ApiItemControllerDocumentationTest {
    @Autowired private WebTestClient webTestClient; // webTestClient : 웹플럭스 컨트롤러 호출에 사용
    @MockBean InventoryService inventoryService;
    @MockBean ItemRepository itemRepository;

    @Test
    void findingAllItems() {
        when(itemRepository.findAll()).thenReturn(
            Flux.just(new Item("item-1", "Alf alarm clock", "nothing I really need", 19.99)));

        this.webTestClient.get().uri("/api/items")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            // document() = 스프링 레스트 독 정적 메소드, 문서 생성 기능을 테스트에 추가하는 역할을 함, 문서는 target/generated-snippets/findAll 디렉터리에 생성됨
            .consumeWith(document("findAll", preprocessResponse(prettyPrint())));
    }

    @Test
    void postNewItem() {
        when(itemRepository.save(any())).thenReturn(
            Mono.just(new Item("1", "Alf alarm clock", "nothing important", 19.99)));

        this.webTestClient.post().uri("/api/items")
            .bodyValue(new Item("Alf alarm clock", "nothing important", 19.99))
            .exchange()
            .expectStatus().isCreated()
            .expectBody()
            .consumeWith(document("post-new-item", preprocessResponse(prettyPrint()))); // <4>
    }
}