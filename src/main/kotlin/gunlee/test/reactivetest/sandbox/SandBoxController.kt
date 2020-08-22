package gunlee.test.reactivetest.sandbox

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.SynchronousSink
import java.util.*
import java.util.function.Consumer
import kotlin.coroutines.coroutineContext


/**
 * @author Gun Lee (gunlee01@gmail.com) on 29/07/2020
 */
@RestController
class SandBoxController {
    /**
     * * 체크 포인트 흐름
    (subs->next->complete의 scannable은 동일 object 이다.)
    - scannable이 subs에서 checkout point 였다면 동일 obj로 next/complte/error를 판단하면 되겠다.

    ((MonoOnErrorResume) scannable).checkpoint()

    <subs>
    checkpoint("Handler gunlee.test.reactivetest.sandbox.SandBoxController#testWebCallWithCoroutine9(ServerHttpRequest, Continuation) [DispatcherHandler]")
    24603

    checkpoint("Request to GET https://httpbin.org/delay/9 [DefaultWebClient]")
    24852

    <웹콜후>

    <next>
    checkpoint("Request to GET https://httpbin.org/delay/9 [DefaultWebClient]")
    24852

    <complete>
    checkpoint("Request to GET https://httpbin.org/delay/9 [DefaultWebClient]")

    checkpoint("Handler gunlee.test.reactivetest.sandbox.SandBoxController#testWebCallWithCoroutine9(ServerHttpRequest, Continuation) [DispatcherHandler]")
    24603


     */

    @Autowired
    lateinit var testCoroutineService: TestCoroutineService

    @Autowired
    lateinit var testWebfluxService: TestWebfluxService

    @GetMapping("/test")
    fun test(request: ServerHttpRequest): Test {
        println("Thread=${Thread.currentThread().name}")

        return Test("ok")
    }

    @GetMapping("/test-mono")
    fun testMono(request: ServerHttpRequest): Mono<String> {
        println("Thread=${Thread.currentThread().name}")
        val test = testWebfluxService.test()

        return test
    }

    @GetMapping("/test-mono-exception")
    fun testMonoException(request: ServerHttpRequest): Mono<String> {
        println("Thread=${Thread.currentThread().name}")
        val testWebCall = testWebfluxService
                .testWebCall()
                .map {
                    if (true) throw RuntimeException("text ex!")
                    it
                }

        return testWebCall
    }

    @GetMapping("/test-mono-exception-catch")
    fun testMonoExceptioncatch(request: ServerHttpRequest): Mono<String> {
        println("Thread=${Thread.currentThread().name}")
        val testWebCall = testWebfluxService
                .testWebCall()
                .map {
                    try {
                        if (true) throw RuntimeException("text ex!")
                    } catch (e: Exception) {
                        try {
                            println(e.message)
                            if (true) throw RuntimeException("text ex222!", e)

                        } catch (e2: Exception) {
                            println(e2.message)
                            if (true) throw RuntimeException("text ex333!", e2)
                        }
                    }
                    it
                }.onErrorResume { t  ->
                    println(t.message)
                    Mono.just("XXX")
                }

        return testWebCall
    }

    @GetMapping("/test-c-exception")
    suspend fun testCoroutineException(request: ServerHttpRequest): String {
        println("Thread=${Thread.currentThread().name}")
        val testWebCall = testCoroutineService.testWebCallCoroutine();
        if (true) {
            throw RuntimeException("text ex!")
        }

        return testWebCall + "a"
    }

    @GetMapping("/test-c-exception3")
    suspend fun testCoroutineException3(request: ServerHttpRequest): String {
        println("Thread=${Thread.currentThread().name}")
        val testWebCall = testCoroutineService.testWebCallCoroutineException();
        if (true) {
            throw RuntimeException("text ex0!")
        }

        return testWebCall + "a"
    }

    @GetMapping("/test-webcall")
    fun testWebCall(request: ServerHttpRequest): Mono<String> {
        println("Thread=${Thread.currentThread().name}")
        return testWebfluxService.testWebCall()
                .flatMap {
                    testWebfluxService.testWebCall()
                }
    }

    @GetMapping("/test-webcall-404")
    fun testWebCall404tc(request: ServerHttpRequest): Mono<String> {
        println("Thread=${Thread.currentThread().name}")
        return testWebfluxService.testWebCall404()
                .flatMap {
                    testWebfluxService.testWebCall()
                }
    }

    @GetMapping("/test-webcall-404-tc")
    fun testWebCall404(request: ServerHttpRequest): Mono<String> {
        println("Thread=${Thread.currentThread().name}")
        return testWebfluxService.testWebCall404()
                .onErrorResume{ t -> Mono.just("404") }
                .flatMap {
                    testWebfluxService.testWebCall()
                }
    }

    @GetMapping("/test-webcall-500")
    fun testWebCall500(request: ServerHttpRequest): Mono<String> {
        println("Thread=${Thread.currentThread().name}")
        return testWebfluxService.testWebCall()
                .flatMap {
                    testWebfluxService.testWebCall500()
                }
    }

    @GetMapping("/test-webcall-1")
    fun testWebCall1(request: ServerHttpRequest): Mono<String> {
        return testWebfluxService.testWebCall()
    }

    @GetMapping("/test-self-webcall-1")
    fun testSelfWebCall1(request: ServerHttpRequest): Mono<String> {
        return testWebfluxService.testSelfWebCall()
    }

    @GetMapping("/test-self-webcall-2")
    fun testSelfWebCall2(request: ServerHttpRequest): Mono<String> {
        return testWebfluxService.testSelfWebCall2()
    }

    @GetMapping("/test-webcall-9")
    fun testWebCall9(request: ServerHttpRequest): Mono<String> {
        return testWebfluxService.testWebCall9()
    }

    @GetMapping("/test-webcall-coroutine")
    suspend fun testWebCallWithCoroutine(request: ServerHttpRequest): String {
        println("Thread=${Thread.currentThread().name}")
        testCoroutineService.testWebCallCoroutine()
        println("Thread=${Thread.currentThread().name}")
        testCoroutineService.testWebCallCoroutine()
        println("Thread=${Thread.currentThread().name}")
        val resp = testCoroutineService.testWebCallCoroutine()
        println("Thread=${Thread.currentThread().name}")
        return resp
    }

    @GetMapping("/test-webcall-coroutine-1")
    suspend fun testWebCallWithCoroutine1(request: ServerHttpRequest): String {
        val resp = testCoroutineService.testWebCallCoroutine()
        return resp
    }

    @GetMapping("/test-webcall-coroutine-9")
    suspend fun testWebCallWithCoroutine9(request: ServerHttpRequest): String {
        val resp = testCoroutineService.testWebCallCoroutine9()
        return resp
    }

    @GetMapping("/test-subscribe-on")
    fun testSubscribeOn(request: ServerHttpRequest): Mono<String> {
        println("Thread=${Thread.currentThread().name}")
        val test = testWebfluxService.testSubscribeOn()

        return test
    }

    @GetMapping("/test-mono-sleep")
    fun testMonoSleep(request: ServerHttpRequest): Mono<String> {
        println("Thread=${Thread.currentThread().name}")
        val test = testWebfluxService.testSleep()

        return test
    }

    @GetMapping("/test-flux")
    fun testFlux(request: ServerHttpRequest): Flux<String> {
        println("Thread=${Thread.currentThread().name}")
        val test = testWebfluxService.testFlux()

        return test
    }

    @GetMapping("/test-flux-sleep")
    fun testFluxSleep(request: ServerHttpRequest): Flux<String> {
        println("Thread=${Thread.currentThread().name}")
        val test = testWebfluxService.testFluxSleep()

        return test
    }


    @GetMapping("/test-coroutine")
    suspend fun testCoroutine(request: ServerHttpRequest): Test {

        val scope = CoroutineScope(coroutineContext)
        scope.launch {
            println("controller 0 Thread=${Thread.currentThread().name} coroutine hashcode=${coroutineContext}")
        }
        println("controller 1 Thread=${Thread.currentThread().name} coroutine hashcode=${kotlin.coroutines.coroutineContext}")
        val name = testCoroutineService.test()
        println("controller 2 Thread=${Thread.currentThread().name} coroutine hashcode=${kotlin.coroutines.coroutineContext}")

        return Test(name)
    }

    @GetMapping("/test/{id}")
    suspend fun findOne(@PathVariable id: Int): Test? {
        println("Thread=${Thread.currentThread().name}")
        return Test(id.toString())
    }

    @GetMapping("/test-sleep")
    suspend fun findSleep(): Test? {
        println("Thread=${Thread.currentThread().name}")
        delay(3500)
        println(">>> sleep done");
        return Test("10000")
    }

    @GetMapping("/test-sleep-long")
    suspend fun findSleepLong(): Test? {
        println("Thread=${Thread.currentThread().name}")
        delay(15000)
        println(">>> sleep done");
        return Test("10000")
    }

    @GetMapping("/test-sleep-error")
    suspend fun findSleepError(): Test? {
        println("Thread=${Thread.currentThread().name}")
        delay(3500)
        println(">>> sleep done");
        if (true) {
            throw Exception("TEST EXCEPTION");
        }
        return Test("10000")
    }

    @GetMapping("/test-with-context")
    suspend fun testWithContext(): Test? {
        println("Thread=${Thread.currentThread().name}")
        delay(1500)
        testCoroutineService.testWithContext()
        println(">>> sleep done");
        return Test("10000")
    }

    @GetMapping("/test-threadlocal")
    suspend fun testThreadLocal(): Test? {
        println("Thread=${Thread.currentThread().name}")
        testCoroutineService.testThreadLocal()
        return Test("10000")
    }

//    @GetMapping("/test-sleep-flow-error")
//    fun findSleepFlowError(): Flow<Int> {
//        println("Thread=${Thread.currentThread().name}")
//        delay(3500)
//        println(">>> sleep done");
//        if (true) {
//            throw Exception("TEST EXCEPTION");
//        }
//        return foo()
//    }

    @GetMapping("/test-sleep-flow")
    fun findSleep2(): Flow<Int> {
        println("Thread=${Thread.currentThread().name}")
        return foo()
    }

    @GetMapping("/test-flow100")
    fun findFlow100(): Flow<Int> {
        return foo100()
    }

    @GetMapping("/test-flow100-2")
    fun findFlow100_2(): Flow<Int> {
        return foo100().map { it * 2 }
    }

    @GetMapping("/test-flux-100")
    fun findFlux100(): Flux<Int> {
        var seq = Flux.generate<Int> (
                object : Consumer<SynchronousSink<Int>> {
                    private var emitCount = 0
                    private val rand: Random = Random()
                    override fun accept(sink: SynchronousSink<Int>) {
                        emitCount++
                        val data: Int = rand.nextInt(1000) + 1 // 1~100 사이 임의 정수
                        sink.next(data) // 임의 정수 데이터 발생
                        if (emitCount == 100) { // 10개 데이터를 발생했으면
                            sink.complete() // 완료 신호 발생
                        }
                    }
                }
        )
        return seq.map { it * 2 }
    }

    fun foo(): Flow<Int> = flow { // flow builder
        for (i in 1..5) {
            delay(1000) // pretend we are doing something useful here
            emit(i) // emit next value
        }
    }

    fun foo100(): Flow<Int> = flow { // flow builder
        for (i in 1..100) {
            emit(i) // emit next value
        }
    }

    data class Test(val name: String) {
    }
}
