package com.example.reactive.controller;

import com.example.reactive.domain.Item;
import com.example.reactive.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.mediatype.alps.Alps;
import org.springframework.hateoas.mediatype.alps.Type;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mediatype.alps.Alps.descriptor;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn;

@RequiredArgsConstructor
@RestController
public class HypermediaItemController {
    private final ItemRepository repository;

    @GetMapping("/hypermedia/items")
    Mono<CollectionModel<EntityModel<Item>>> findAll() {
        return this.repository.findAll()
            .flatMap(item -> findOne(item.getId()))
            .collectList()
            .flatMap(entityModels -> linkTo(methodOn(HypermediaItemController.class)
                                                .findAll()).withSelfRel()
                .toMono()
                .map(selfLink -> CollectionModel.of(entityModels, selfLink)));
    }

    @GetMapping("/hypermedia/items/{id}")
    Mono<EntityModel<Item>> findOne(@PathVariable String id) {
        HypermediaItemController controller = methodOn(HypermediaItemController.class); // 컨트롤러에 대한 프록시 생성

        Mono<Link> selfLink = linkTo(controller.findOne(id)).withSelfRel().toMono(); // findOne 메소드에 대한 링크 생성

        Mono<Link> aggregateLink = linkTo(controller.findAll()).withRel(IanaLinkRelations.ITEM).toMono();

        // Mono.zip -> 여러 개의 비동기 요청(findById, selfLink, aggregateLink)을 실행하고 각 결과를 하나로 합치기 위해 사용
        // EntityModel : 도메인 객체를 감싸고 링크를 추가할 수 있는 모델
        return Mono.zip(repository.findById(id), selfLink, aggregateLink)
            .map(objects -> EntityModel.of(objects.getT1(), Links.of(objects.getT2(), objects.getT3())));
    }

    @GetMapping(value = "/hypermedia/items/profile", produces = MediaTypes.ALPS_JSON_VALUE)
    public Alps profile() {
        return Alps
            .alps()
            .descriptor(Collections.singletonList(
                descriptor()
                    .id(Item.class.getSimpleName() + "-repr")
                    .descriptor(Arrays.stream(
                            Item.class.getDeclaredFields())
                                    .map(field -> descriptor()
                                        .name(field.getName())
                                        .type(Type.SEMANTIC)
                                        .build())
                                    .collect(Collectors.toList()))
                    .build()))
            .build();
    }
}
