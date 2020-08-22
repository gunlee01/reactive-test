package gunlee.test.reactivetest

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ReactiveTestApplication

fun main(args: Array<String>) {
//	ReactorDebugAgent.init();
	runApplication<ReactiveTestApplication>(*args)
}
