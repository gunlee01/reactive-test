package gunlee.test.reactivetest

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import reactor.tools.agent.ReactorDebugAgent

@SpringBootApplication
class ReactiveTestApplication

fun main(args: Array<String>) {
//	ReactorDebugAgent.init();
	runApplication<ReactiveTestApplication>(*args)
}
