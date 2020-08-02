package gunlee.test.reactivetest.sandbox

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import kotlin.coroutines.coroutineContext


/**
 * @author Gun Lee (gunlee01@gmail.com) on 29/07/2020
 */
@RestController
class SandBoxController {

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

    @GetMapping("/test-webcall")
    fun testWebCall(request: ServerHttpRequest): Mono<String> {
        println("Thread=${Thread.currentThread().name}")
        return testWebfluxService.testWebCall()
                .flatMap {
                    testWebfluxService.testWebCall()
                }
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

    @GetMapping("/test-sleep-flow-error")
    suspend fun findSleepFlowError(): Flow<Int> {
        println("Thread=${Thread.currentThread().name}")
        delay(3500)
        println(">>> sleep done");
        if (true) {
            throw Exception("TEST EXCEPTION");
        }
        return foo()
    }

    @GetMapping("/test-sleep-flow")
    suspend fun findSleep2(): Flow<Int> {
        println("Thread=${Thread.currentThread().name}")
        return foo()
    }

    fun foo(): Flow<Int> = flow { // flow builder
        for (i in 1..5) {
            delay(1000) // pretend we are doing something useful here
            emit(i) // emit next value
        }
    }


    data class Test(val name: String) {
    }
}
