/*
 *  Copyright 2015 the original author or authors.
 *  @https://github.com/scouter-project/scouter
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package gunlee.test.reactivetest.sandbox

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2020/08/17
 */
@RestController
class MongoController {

    @Autowired
    lateinit var reactiveMongoService: ReactiveMongoService

    @GetMapping("/mongo")
    fun mongo(request: ServerHttpRequest): Mono<String> {
        println("Thread=${Thread.currentThread().name}")
        val test = reactiveMongoService.mongo()

        return test
    }

    @GetMapping("/mongo-add")
    fun mongoAdd(request: ServerHttpRequest): Mono<Employee> {
        println("Thread=${Thread.currentThread().name}")
        val test = reactiveMongoService.mongoAdd()

        return test
    }

    @GetMapping("/mongo-update")
    fun mongoUpdate(request: ServerHttpRequest): Mono<Employee> {
        println("Thread=${Thread.currentThread().name}")
        val test = reactiveMongoService.mongoUpdate()

        return test
    }

    @GetMapping("/mongo-flux")
    fun mongoFlux(request: ServerHttpRequest): Flux<String> {
        println("Thread=${Thread.currentThread().name}")
        val test = reactiveMongoService.mongoAll()

        return test
    }
}

@Service
class ReactiveMongoService {

    @Autowired
    lateinit var employeeRepository: EmployeeRepository

    fun mongo(): Mono<String> {
        val emp = employeeRepository.findByName("Gun1")
        return emp.next().map {
            it.name
        }
    }

    fun mongoUpdate(): Mono<Employee> {
        val emp0 = Employee()
        emp0.name = "gun200"
        emp0.id = 200

        val emp1 = employeeRepository.findByName("gun200")
                .flatMap { employeeRepository.save(it) }
        return emp1.next()
    }

    fun mongoAdd(): Mono<Employee> {
        val emp0 = Employee()
        emp0.name = "gun200"
        emp0.id = 200

        return employeeRepository.save(emp0)
    }

    fun mongoAll(): Flux<String> {
        val emps = employeeRepository.findAll()
        return emps.map {
            it.name
        }
    }
}

