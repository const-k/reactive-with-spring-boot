package com.example.reactive.repository;

import com.example.reactive.domain.Item;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ItemRepository extends ReactiveCrudRepository<Item, String> {

    Flux<Item> findByNameContaining(String partialName);

    @Query("{ 'name' : ?0, 'age' :  }")
    Flux<Item> findItemsForCustomerMonthlyReport();

    @Query(sort = "{ 'age' : -1", value = "{ 'name' : 'TV tray', 'age' : }")
    Flux<Item> findSortedStuffForWeeklyReport();

    // search by name
    Flux<Item> findByNameContainingIgnoreCase(String partialName);

    // search by description
    Flux<Item> findByDescriptionContainingIgnoreCase(String partialName);

    // search by name AND description
    Flux<Item> findByNameContainingAndDescriptionContainingAllIgnoreCase(String partialName, String partialDesc);

    // search by name OR description
    Flux<Item> findByNameContainingOrDescriptionContainingAllIgnoreCase(String partialName, String partialDesc);
}
