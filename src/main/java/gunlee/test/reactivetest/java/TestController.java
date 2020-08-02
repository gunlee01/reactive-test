package gunlee.test.reactivetest.java;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 31/07/2020
 */
@RestController
public class TestController {

	@GetMapping("/java/test1")
	public Mono<String> testJava1() {
		return Mono.just("test1");
	}

	@GetMapping("/java/test1/{no}")
	public Mono<String> testJava11(@PathVariable String no) {
		return Mono.just("test1" + no);
	}
}
