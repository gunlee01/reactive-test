package gunlee.test.reactivetest.sandbox

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Duration
import java.util.concurrent.Executors

/**
 * @author Gun Lee (gunlee01@gmail.com) on 29/07/2020
 */
@Component
class TestWebfluxService {

    val newFixedThreadPool = Executors.newFixedThreadPool(10)

    fun test(): Mono<String> {
        return Mono.just("test")
    }

    fun testSleep(): Mono<String> {
        return Mono.delay(Duration.ofMillis(3000))
                .map {
                    println("Thread=${Thread.currentThread().name}")
                    "test"
                }
    }

    fun testFlux(): Flux<String> {
        return Flux.just("test1", "test2", "test3")
    }

    fun testFluxSleep(): Flux<String> {
        return Mono.delay(Duration.ofMillis(3000))
                .flatMapMany {
                    println("Thread=${Thread.currentThread().name}")
                    Flux.just("test1", "test2", "test3")
                }
    }

    fun testWebCall(): Mono<String> {
        val webClient = WebClient.create("https://httpbin.org")
        return webClient.get().uri("/delay/1").retrieve().bodyToMono(String::class.java)
    }

    fun testSubscribeOn(): Mono<String> {
        return Mono.just("test")
                .map {
                    Thread.sleep(3000);
                    "test"
                }.subscribeOn(Schedulers.fromExecutor(newFixedThreadPool))
    }
}
