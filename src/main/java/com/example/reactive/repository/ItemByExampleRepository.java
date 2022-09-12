package com.example.reactive.repository;

import com.example.reactive.domain.Item;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;

public interface ItemByExampleRepository extends ReactiveQueryByExampleExecutor<Item> {
}
