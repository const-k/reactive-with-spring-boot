package com.example.reactive.repository;

import com.example.reactive.helper.HttpTraceWrapper;
import org.springframework.data.repository.Repository;

import java.util.stream.Stream;

// HttpTraceRepository(actuator) 가 논블로킹 패러다임을 사용하지 않기 때문에 Repository 상속
public interface HttpTraceWrapperRepository extends Repository<HttpTraceWrapper, String> {
    Stream<HttpTraceWrapper> findAll();

    void save(HttpTraceWrapper trace);
}
