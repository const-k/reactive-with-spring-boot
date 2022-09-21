package com.example.reactive;

import com.example.reactive.domain.Item;
import com.example.reactive.repository.ItemRepository;
import com.example.reactive.repository.TemplateDatabaseLoader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest()
@AutoConfigureWebTestClient
@Testcontainers // JUnit 5에서 제공하는 애너테이션, 테스트컨테이너를 테스트에 사용할 수 있게 해줌
@ContextConfiguration // 지정한 클래스를 테스트 실행 전에 먼저 애플리케이션 컨텍스트에 로딩해줌

// 레빗엠큐를 사용하는 테스트에는 StepVerifier 같은 비동기 시퀀스에 대해 검증할 수 있는 객체 제공 x, Thread.sleep 사용해서 테스트해야 함
public class RabbitTest {

    @Container
    static RabbitMQContainer container = new RabbitMQContainer("rabbitmq:3.7.25-management-alpine");

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ItemRepository repository;

    @MockBean
    TemplateDatabaseLoader templateDatabaseLoader;

    @DynamicPropertySource // Supplier를 사용해서 환경설정 내용을 Environment에 동적으로 추가
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", container::getContainerIpAddress);
        registry.add("spring.rabbitmq.port", container::getAmqpPort);
    }

    @Test
    void verifyMessagingThroughAmqp() throws InterruptedException {
        this.webTestClient.post().uri("/amqp/items")
            .bodyValue(new Item("Alf alarm clock", "nothing important", 19.99))
            .exchange()
            .expectStatus().isCreated()
            .expectBody();

        TimeUnit.MICROSECONDS.sleep(1500); // 1500ms 동안 sleep() -> post api 를 통해 브로커에 전달된 메시지가 db에 저장될 때까지 기다림

        this.webTestClient.post().uri("/amqp/items")
            .bodyValue(new Item("Smurf TV tray", "nothing important", 29.99))
            .exchange()
            .expectStatus().isCreated()
            .expectBody();

        TimeUnit.MICROSECONDS.sleep(2000);

        this.repository.findAll()
            .as(StepVerifier::create)
            .expectNextMatches(item -> {
                assertThat(item.getName()).isEqualTo("Alf alarm clock");
                assertThat(item.getDescription()).isEqualTo("nothing important");
                assertThat(item.getPrice()).isEqualTo(19.99);
                return true;
            }).expectNextMatches(item -> {
                assertThat(item.getName()).isEqualTo("Smurf TV tray");
                assertThat(item.getDescription()).isEqualTo("nothing important");
                assertThat(item.getPrice()).isEqualTo(29.99);
                return true;
            }).verifyComplete();
    }
}
