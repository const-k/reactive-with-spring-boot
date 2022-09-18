package com.example.reactive.controller;

import com.example.reactive.domain.Item;
import com.example.reactive.repository.ItemRepository;
import com.example.reactive.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;


@WebFluxTest(controllers = AffordancesItemController.class)
@AutoConfigureRestDocs
class AffordancesItemControllerDocumentationTest {

    @Autowired private WebTestClient webTestClient;

    @MockBean InventoryService service;

    @MockBean ItemRepository repository;

    @Test
    void findSingleItemAffordances() {
        when(repository.findById("item-1"))
            .thenReturn(Mono.just(
                new Item("item-1", "Alf alarm clock", "nothing I really need", 19.99)));

        this.webTestClient.get().uri("/affordances/items/item-1")
            .accept(MediaTypes.HAL_FORMS_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .consumeWith(document("single-item-affordances",
                                  preprocessResponse(prettyPrint())));
    }

    @Test
    void findAggregateRootItemAffordances() {
        when(repository.findAll()).thenReturn(Flux.just(
            new Item("Alf alarm clock", "nothing I really need", 19.99)));
        when(repository.findById((String) null)).thenReturn(Mono.just(
            new Item("item-1", "Alf alarm clock", "nothing I really need", 19.99)));

        this.webTestClient.get().uri("/affordances/items")
            .accept(MediaTypes.HAL_FORMS_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .consumeWith(document("aggregate-root-affordances", preprocessResponse(prettyPrint())));
    }
}