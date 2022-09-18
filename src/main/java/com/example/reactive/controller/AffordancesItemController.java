package com.example.reactive.controller;

import com.example.reactive.domain.Item;
import com.example.reactive.repository.ItemRepository;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.alps.Alps;
import org.springframework.hateoas.mediatype.alps.Type;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mediatype.alps.Alps.alps;
import static org.springframework.hateoas.mediatype.alps.Alps.descriptor;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn;

@RestController
public class AffordancesItemController {

    private final ItemRepository repository;

    public AffordancesItemController(ItemRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/affordances")
    Mono<RepresentationModel<?>> root() {
        AffordancesItemController controller = methodOn(AffordancesItemController.class);

        Mono<Link> selfLink = linkTo(controller.root()) 
            .withSelfRel() 
            .toMono();

        Mono<Link> itemsAggregateLink = linkTo(controller.findAll()) 
            .withRel(IanaLinkRelations.ITEM) 
            .toMono();

        return selfLink.zipWith(itemsAggregateLink) 
            .map(links -> Links.of(links.getT1(), links.getT2())) 
            .map(links -> new RepresentationModel<>(links.toList()));
    }

    @GetMapping("/affordances/items")
    Mono<CollectionModel<EntityModel<Item>>> findAll() {
        AffordancesItemController controller = methodOn(AffordancesItemController.class);

        Mono<Link> aggregateRoot = linkTo(controller.findAll()) 
            .withSelfRel() 
            .andAffordance(controller.addNewItem(null))
            .toMono();

        return this.repository.findAll()
            .flatMap(item -> findOne(item.getId()))
            .collectList()
            .flatMap(models -> aggregateRoot 
                .map(selfLink -> CollectionModel.of(models, selfLink)));
    }

    @GetMapping("/affordances/items/{id}")
    Mono<EntityModel<Item>> findOne(@PathVariable String id) {
        AffordancesItemController controller = methodOn(AffordancesItemController.class);

        Mono<Link> selfLink = linkTo(controller.findOne(id)) 
            .withSelfRel() 
            .andAffordance(controller.updateItem(null, id))
            .toMono();

        Mono<Link> aggregateLink = linkTo(controller.findAll()) 
            .withRel(IanaLinkRelations.ITEM) 
            .toMono();

        return Mono.zip(repository.findById(id), selfLink, aggregateLink) 
            .map(o -> EntityModel.of(o.getT1(), Links.of(o.getT2(), o.getT3())));
    }

    @PostMapping("/affordances/items")
    Mono<ResponseEntity<?>> addNewItem(@RequestBody Mono<EntityModel<Item>> item) {
        return item 
            .map(EntityModel::getContent)
            .flatMap(this.repository::save)
            .map(Item::getId)
            .flatMap(this::findOne)
            .map(newModel -> ResponseEntity.created(newModel
                                                        .getRequiredLink(IanaLinkRelations.SELF) 
                                                        .toUri()).body(newModel.getContent()));
    }


    @PutMapping("/affordances/items/{id}")
    public Mono<ResponseEntity<?>> updateItem(@RequestBody Mono<EntityModel<Item>> item,
                                              @PathVariable String id) {
        return item 
            .map(EntityModel::getContent) 
            .map(content -> new Item(id, content.getName(),
                                     content.getDescription(), content.getPrice())) 
            .flatMap(this.repository::save)
            .then(findOne(id))
            .map(model -> ResponseEntity.noContent()
                .location(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).build());
    }

    @GetMapping(value = "/affordances/items/profile", produces = MediaTypes.ALPS_JSON_VALUE)
    public Alps profile() {
        return alps() 
            .descriptor(Collections.singletonList(descriptor() 
                                                      .id(Item.class.getSimpleName() + "-representation") 
                                                      .descriptor( 
                                                                   Arrays.stream(Item.class.getDeclaredFields()) 
                                                                       .map(field -> descriptor()
                                                                           .name(field.getName())
                                                                           .type(Type.SEMANTIC)
                                                                           .build())
                                                                       .collect(Collectors.toList()))
                                                      .build()))
            .build();
    }

}
