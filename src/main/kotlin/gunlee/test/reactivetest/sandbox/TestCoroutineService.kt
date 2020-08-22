package gunlee.test.reactivetest.sandbox

import kotlinx.coroutines.*
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitExchange
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * @author Gun Lee (gunlee01@gmail.com) on 29/07/2020
 */
@Component
class TestCoroutineService {
    suspend fun test(): String = coroutineScope {
        println("TestService 1 Thread=${Thread.currentThread().name} coroutine hashcode=${coroutineContext}")
        delay(500)
        println("TestService 2 Thread=${Thread.currentThread().name} coroutine hashcode=${coroutineContext}")
        delay(500)
        println("TestService 3 Thread=${Thread.currentThread().name} coroutine hashcode=${coroutineContext}")

        val async = async {
            println("TestService a1 Thread=${Thread.currentThread().name} coroutine hashcode=${coroutineContext}")
        }

        val async2 = async {
            println("TestService a2 Thread=${Thread.currentThread().name} coroutine hashcode=${coroutineContext}")
        }

        delay(500)
        println("TestService 4 Thread=${Thread.currentThread().name} coroutine hashcode=${coroutineContext}")

        async.await()
        async2.await()
        "test good"
    }

    suspend fun testWebCallCoroutine(): String {
        val webClient = WebClient.create("https://httpbin.org")
        return webClient.get().uri("/delay/1").awaitExchange().awaitBody()
    }

    suspend fun testWebCallCoroutineException(): String {
        val webClient = WebClient.create("https://httpbin.org")
        val awaitBody: String = webClient.get().uri("/delay/1").awaitExchange().awaitBody()
        if (true) {
            throw RuntimeException("test3");
        }
        return awaitBody
    }

    suspend fun testWebCallCoroutine9(): String {
        val webClient = WebClient.create("https://httpbin.org")
        return webClient.get().uri("/delay/9").awaitExchange().awaitBody()
    }

    val executor: ExecutorService = Executors.newFixedThreadPool(10)

    suspend fun testWithContext(): String? = withContext(executor.asCoroutineDispatcher()) {
        Thread.sleep(1500);
        "test"
    }

    val tl =  ThreadLocal<String?>()

    suspend fun testThreadLocal(): String? = coroutineScope {

        println("TestService a1 Thread=${Thread.currentThread().name}")
        tl.set("tl-1")
        println("1 " + tl.get());

        val async = async(tl.asContextElement()) {
            println("TestService a1 Thread=${Thread.currentThread().name}")
            tl.set("tl-2")
            println("2 " + tl.get());

            async {
                println("TestService a1 Thread=${Thread.currentThread().name}")
                tl.set("tl-3")
                println("3 " + tl.get());
            }

            println("4 " + tl.get());
        }

        val async2 = async(tl.asContextElement()) {
            println("TestService a1 Thread=${Thread.currentThread().name}")
            tl.set("tl-6")
            println("6 " + tl.get());
        }

        async.await()
        async2.await()
        println("5" + tl.get());

        "test"
    }
}
