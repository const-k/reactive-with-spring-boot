package com.example.reactive8server;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Controller // @Service 사용하니까 route 못찾는 에러 발생
public class RSocketService {
    private final ItemRepository repository;
    private final Sinks.Many<Item> itemsSink;

    public RSocketService(ItemRepository repository) {
        this.repository = repository;
        this.itemsSink = Sinks.many().multicast().onBackpressureBuffer();
    }

    // 요청 - 응답 (1개의 스트림)
    @MessageMapping("newItems.request-response") // 도착지가 newItems.request-response로 지정된 R 소켓 메시지를 이 메소드로 라우팅
    public Mono<Item> processNewItemsViaRSocketRequestResponse(Item item) { // 스프링 메시징은 메시지가 들어오기를 리액티브하게 기다리고 있다가
        // 메시지가 들어오면 메시지 본문을 인자로 해서 save() 메소드를 호출
        return this.repository.save(item)
            .doOnNext(this.itemsSink::tryEmitNext); // doOnNext()를 호출해서 새로 저장된 Item 객체를 가져와서 싱크를 통해 FluxProcessor로 내보냄
    }

    // 요청 - 스트림 (다수의 유한한 스트림)
    @MessageMapping("newItems.request-stream") // 도착지가 newItems.request-stream으로 지정된 R 소켓 메시지를 이 메소드로 라우팅
    public Flux<Item> findItemsViaRSocketRequestStream() {
        return this.repository.findAll()
            .doOnNext(this.itemsSink::tryEmitNext);
    }

    // 실행 후 망각 (무응답)
    @MessageMapping("newItems.fire-and-forget")
    public Mono<Void> processNewItemsViaRSocketFireAndForget(Item item) { // Void -> 리액티브 스트림 프로그래밍에서 제어 신호를 받을 수 있는 수단이 필요해서 사용
        return this.repository.save(item)
            .doOnNext(this.itemsSink::tryEmitNext)
            .then(); // 리액터에서 then() 사용하면 Mono에 감싸져 있는 데이터를 사용하지 않고 버릴 수 있음
    }

    // 채널 (양방향)
    @MessageMapping("newItems.monitor")
    public Flux<Item> monitorNewItems() {
        return this.itemsSink.asFlux();
    }
}
