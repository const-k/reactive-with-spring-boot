package com.example.reactive.controller;

import com.example.reactive.domain.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;

@RequiredArgsConstructor
@RestController
@Slf4j
public class SpringAmqpItemController {

    private final AmqpTemplate template;

    @PostMapping("/amqp/items")
    Mono<ResponseEntity<?>> addNewItemUsingSpringAmqp(@RequestBody Mono<Item> itemMono) {
        return itemMono
            // 리액터는 스케줄러를 통해 개별 수행 단계가 어느 스레드에서 실행될지 지정할 수 있음
            // subScribeOn 은 호출되는 위치와 상관 없이 해당 플로우 전체가 subScribeOn에서 지정한 스레드에서 실행됨. publishOn 은 호출된 이후부터 지정한 스레드에서 실행됨
            .subscribeOn(Schedulers.boundedElastic()) // amqpTemplate은 블로킹 API를 호출함 -> 엘라스틱 스케줄러에서 관리하는 별도의 스레드에서 실행되게 함
                                                      // 별도의 스레드 풀이므로 블로킹 API 호출이 있더라도 다른 리액터 플로우에 블로킹 영향을 전파하지 않음
            .flatMap(content -> {
                return Mono
                    .fromCallable(() -> {
                        this.template.convertAndSend("hacking-spring-boot", "new-items-spring-amqp", content);
                        return ResponseEntity.created(URI.create("items")).build();
                    });
            });
    }
}
