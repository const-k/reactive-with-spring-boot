package com.example.reactive8client;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;

import static io.rsocket.metadata.WellKnownMimeType.MESSAGE_RSOCKET_ROUTING;
import static org.springframework.http.MediaType.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_NDJSON_VALUE;
import static org.springframework.http.MediaType.parseMediaType;

@RestController
public class RSocketController {
    //RSocketRequester = R 소켓에 무언가를 보낼 때 사용하는 얇은 포장재와 같음, R 소켓의 API는 프로젝트 리액터를 사용
    //RSocketRequester 를 사용해야 스프링 프레임워크와 연동됨 (R 소켓에 스프링의 메시징 패러다임 포함되지 않았기 때문)
    private final Mono<RSocketRequester> requester; // Mono 를 사용하므로 R 소켓에 연결된 코드는 새 클라이언트가 구독할 때마다 호출됨

    public RSocketController(RSocketRequester.Builder builder) {
        this.requester = builder
            .dataMimeType(APPLICATION_JSON)
            .metadataMimeType(parseMediaType(MESSAGE_RSOCKET_ROUTING.toString()))
            .connectTcp("localhost", 7000)
            .retry(5) // 메시지 처리 실패 시 Mono가 5번까지 재시도
            .cache(); // 요청 Mono를 핫 소스로 전환.
    }

    @PostMapping("/items/request-response")
    Mono<ResponseEntity<?>> addNewItemUsingRSocketRequestResponse(@RequestBody Item item) {
        return this.requester
            .flatMap(rSocketRequester -> rSocketRequester
                .route("newItems.request-response")
                .data(item)
                .retrieveMono(Item.class))
            .map(savedItem -> ResponseEntity.created(
                URI.create("/items/request-response")).body(savedItem));
    }

    // HTTP GET 요청 처리하고 Flux 를 통해 JSON 스트림 데이터를 반환.
    // APPLICATION_NDJSON_VALUE -> 스트림 방식으로 반환하기 위해 사용
    @GetMapping(value = "/items/request-stream", produces = APPLICATION_NDJSON_VALUE)
    Flux<Item> findItemsUsingRSocketRequestStream() {
        return this.requester
            .flatMapMany(rSocketRequester -> rSocketRequester // 여러 겅늬 조회 결과를 리턴하도록 flatMapMany 사용
                .route("newItems.request-stream")
                .retrieveFlux(Item.class)
                .delayElements(Duration.ofSeconds(1))); // 여러 건의 Item을 1초에 1건씩 반환하도록 요청
    }

    @PostMapping("/items/fire-and-forget")
    Mono<ResponseEntity<?>> addNewItemUsingRSocketFireAndForget(@RequestBody Item item) {
        return this.requester
            .flatMap(rSocketRequester -> rSocketRequester
                .route("newItems.fire-and-forget")
                .data(item)
                .send())
            .then(Mono.just(ResponseEntity.created( // Mono<Void> 를 map()이나 flatMap() 을 사용해서 다른 것으로 전환할 수 없음
                URI.create("/items/fire-and-forget")).build()));
    }

    // TEXT_EVENT_STREAM_VALUE -> 응답할 결과가 생길 때마다 결과값을 스트림에 흘려보낸다는 것을 의미
    @GetMapping(value = "/items", produces = TEXT_EVENT_STREAM_VALUE)
    Flux<Item> liveUpdates() {

        System.out.println("test");
        return this.requester
            .flatMapMany(rSocketRequester -> rSocketRequester
                .route("newItems.monitor")
                .retrieveFlux(Item.class));
    }
}
