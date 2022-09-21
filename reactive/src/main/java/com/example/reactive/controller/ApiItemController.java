package com.example.reactive.controller;

import com.example.reactive.domain.Item;
import com.example.reactive.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

@RequiredArgsConstructor
@RestController
public class ApiItemController {
    private final ItemRepository repository;

    @GetMapping("/api/items")
    public Flux<Item> findAll() {
        return this.repository.findAll();
    }

    @GetMapping("/api/items/{id}")
    public Mono<Item> findOne(@PathVariable String id) {
        return this.repository.findById(id);
    }

    @PostMapping("api/items")
    public Mono<ResponseEntity<?>> addNewItem(@RequestBody Mono<Item> item) {
        // Mono 타입이므로 요청 처리를 위한 리액티브 플로우에서 구독이 발생하지 않으면 요청 본문을 Item 타입으로 역직렬화하는 과정도 실행되지 않음
        return item.flatMap(this.repository::save)
            .map(savedItem -> ResponseEntity
                .created(URI.create("/api/items/" + savedItem.getId()))
                .body(savedItem)); // savedItem 객체를 직렬화해서 응답 본문에 적는 일은 스프링 웹플럭스가 담당
    }

    @PutMapping("/api/items/{id}")
    public Mono<ResponseEntity<?>> updateItem(@RequestBody Mono<Item> item,
                                              @PathVariable String id) {

        return item
            .map(content -> new Item(id, content.getName(), content.getDescription(), content.getPrice()))
            .flatMap(repository::save)
            .map(ResponseEntity::ok);
    }
}
