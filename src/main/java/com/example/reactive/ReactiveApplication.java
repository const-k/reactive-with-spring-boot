package com.example.reactive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ReactiveApplication {
	public static void main(String[] args) {
//		BlockHound.builder()
//			.allowBlockingCallsInside(
//				TemplateEngine.class.getCanonicalName(), "process")
//			.install(); // SpringApplication.run 보다 앞에 위치 -> 블록하운드가 바이트 코드를 조작할 수 있게 됨

		// 위 코드를 사용하면 상용에도 활성화되므로 테스트 환경에서만 사용하는 것이 좋다

		SpringApplication.run(ReactiveApplication.class, args);
	}

}
