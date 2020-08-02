package gunlee.test.reactivetest.sandbox

import org.springframework.http.server.reactive.ServerHttpRequest
import kotlin.coroutines.coroutineContext

/**
 * @author Gun Lee (gunlee01@gmail.com) on 29/07/2020
 */
class SandBoxController2 {

    suspend fun test(request: ServerHttpRequest): String {
        println(coroutineContext)
        return "1"
    }
}
