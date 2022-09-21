package com.example.reactive8client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureWebTestClient // WebTestClient 인스턴스를 생성해서 테스트에 활용할 수 있게 해줌
public class RSocketTest {

    @Autowired WebTestClient webTestClient;
    @Autowired ItemRepository repository;

    @Test
    void verifyRemoteOperationsThroughRSocketFireAndForget() throws InterruptedException {

        // Clean out the database
        this.repository.deleteAll()
            .as(StepVerifier::create)
            .verifyComplete();

        // Create a new "item"
        this.webTestClient.post().uri("/items/fire-and-forget")
            .bodyValue(new Item("Alf alarm clock", "nothing important", 19.99))
            .exchange()
            .expectStatus().isCreated()
            .expectBody().isEmpty();

        Thread.sleep(500);

        // Verify the "item" has been added to MongoDB
        this.repository.findAll()
            .as(StepVerifier::create)
            .expectNextMatches(item -> {
                assertThat(item.getId()).isNotNull();
                assertThat(item.getName()).isEqualTo("Alf alarm clock");
                assertThat(item.getDescription()).isEqualTo("nothing important");
                assertThat(item.getPrice()).isEqualTo(19.99);
                return true;
            })
            .verifyComplete();
    }

    @Test
    void verifyRemoteOperationsThroughRSocketRequestStream()
        throws InterruptedException {
        // Clean out the database
        this.repository.deleteAll().block();

        // Create 3 new "item"s
        List<Item> items = IntStream.rangeClosed(1, 3)
            .mapToObj(i -> new Item("name - " + i, "description - " + i, i))
            .collect(Collectors.toList());

        this.repository.saveAll(items).blockLast();


        // Get stream
        this.webTestClient.get().uri("/items/request-stream")
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus().isOk()
            .returnResult(Item.class)
            .getResponseBody()
            .as(StepVerifier::create)
            .expectNextMatches(itemPredicate("1"))
            .expectNextMatches(itemPredicate("2"))
            .expectNextMatches(itemPredicate("3"))
            .verifyComplete();
    }

    private Predicate<Item> itemPredicate(String num) {
        return item -> {
            assertThat(item.getName()).startsWith("name");
            assertThat(item.getName()).endsWith(num);
            assertThat(item.getDescription()).startsWith("description");
            assertThat(item.getDescription()).endsWith(num);
            assertThat(item.getPrice()).isPositive();
            return true;
        };
    }

    @Test
    void verifyRemoteOperationsThroughRSocketRequestResponse()
        throws InterruptedException {

        // Clean out the database
        this.repository.deleteAll()
            .as(StepVerifier::create)
            .verifyComplete();

        // Create a new "item"
        this.webTestClient.post().uri("/items/request-response")
            .bodyValue(new Item("Alf alarm clock", "nothing important", 19.99))
            .exchange()
            .expectStatus().isCreated()
            .expectBody(Item.class)
            .value(item -> {
                assertThat(item.getId()).isNotNull();
                assertThat(item.getName()).isEqualTo("Alf alarm clock");
                assertThat(item.getDescription()).isEqualTo("nothing important");
                assertThat(item.getPrice()).isEqualTo(19.99);
            });

        Thread.sleep(500);

        // Verify the "item" has been added to MongoDB
        this.repository.findAll()
            .as(StepVerifier::create)
            .expectNextMatches(item -> {
                assertThat(item.getId()).isNotNull();
                assertThat(item.getName()).isEqualTo("Alf alarm clock");
                assertThat(item.getDescription()).isEqualTo("nothing important");
                assertThat(item.getPrice()).isEqualTo(19.99);
                return true;
            })
            .verifyComplete();
    }

}
