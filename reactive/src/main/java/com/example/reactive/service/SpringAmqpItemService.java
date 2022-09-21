package com.example.reactive.service;

import com.example.reactive.domain.Item;
import com.example.reactive.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
@Slf4j
public class SpringAmqpItemService {
    private final ItemRepository repository;

    @RabbitListener(ackMode = "MANUAL",
                    bindings = @QueueBinding(
                        value = @Queue,
                        exchange = @Exchange("hacking-spring-boot"),
                        key = "new-items-spring-amqp"))
    public Mono<Void> processNewItemsViaSpringAmqp(Item item) {
        log.debug("Consuming => {}", item);
        return this.repository.save(item).then(); // then() 을 호출해서 저장이 완료될 때까지 기다림
    }
}
