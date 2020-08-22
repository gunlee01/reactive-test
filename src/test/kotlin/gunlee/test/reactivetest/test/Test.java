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

package gunlee.test.reactivetest.test;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.Scannable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Operators;
import reactor.tools.agent.ReactorDebugAgent;
import reactor.util.context.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2020/08/05
 */
public class Test {

    @org.junit.jupiter.api.Test
    public void test0() {
        Hooks.onEachOperator("testName", Operators.lift(
                new BiFunction<Scannable, CoreSubscriber<? super Object>, CoreSubscriber<? super Object>>() {
                    @Override
                    public CoreSubscriber<? super Object> apply(Scannable scannable, CoreSubscriber<? super Object> subscriber) {
                        return new TxidLifter(subscriber, scannable, null);
                    }
                }));

        Mono<Integer> root = Mono.just(3);

        Mono<String> pub1 = root.map(n ->
                String.valueOf(n) + "s");

        pub1.subscribe();

    }

    @org.junit.jupiter.api.Test
    public void test() {
        Hooks.onEachOperator("testName", Operators.lift(
                new BiFunction<Scannable, CoreSubscriber<? super Object>, CoreSubscriber<? super Object>>() {
                    @Override
                    public CoreSubscriber<? super Object> apply(Scannable scannable, CoreSubscriber<? super Object> subscriber) {
                        return new TxidLifter(subscriber, scannable, null);
                    }
                }));

        Flux<Integer> root = Flux.just(0, 1, 2);

        Flux<String> pub1 = root.map(n ->
                String.valueOf(n) + "s");

        Flux<String> pub2 = pub1.map(s ->
                "a:" + s);

        pub2.subscribe();

    }

    @org.junit.jupiter.api.Test
    public void testException() {
        ReactorDebugAgent.init();
        Hooks.onEachOperator("testName", Operators.lift(
                new BiFunction<Scannable, CoreSubscriber<? super Object>, CoreSubscriber<? super Object>>() {
                    @Override
                    public CoreSubscriber<? super Object> apply(Scannable scannable, CoreSubscriber<? super Object> subscriber) {
                        return new TxidLifter(subscriber, scannable, null);
                    }
                }));

        Flux<Integer> root = Flux.just(0, 1, 2);

        Flux<String> pub1 = root.map(n -> String.valueOf(n) + "s");
        Flux<String> pub2 = pub1.map(s -> {
            if (true){
                throw new RuntimeException("my test exception1");
            }
            return s;
        });

        pub2.subscribe();

    }

    @org.junit.jupiter.api.Test
    public void test2() {
        Hooks.onNextDropped(o -> {
            System.out.println("onNextDropped : " + o);
        });

        Hooks.onLastOperator("test", new Function<Publisher<Object>, Publisher<Object>>() {
            @Override
            public Publisher<Object> apply(Publisher<Object> objectPublisher) {
                if (objectPublisher != null) {
                    System.out.println("onLast:objectPublisher:" + objectPublisher.toString() + ":" + objectPublisher.hashCode());
                }
                return objectPublisher;
            }
        });

        Hooks.onEachOperator("testName", Operators.lift(
                new BiFunction<Scannable, CoreSubscriber<? super Object>, CoreSubscriber<? super Object>>() {
                    @Override
                    public CoreSubscriber<? super Object> apply(Scannable scannable, CoreSubscriber<? super Object> subscriber) {
                        return new TxidLifter(subscriber, scannable, null);
                    }
                }));

        WebClient webClient = WebClient.create("https://httpbin.org");

        Mono<ClientResponse> exchange = webClient.get().uri("/delay/3").exchange();

        Mono<String> map = exchange.flatMap(c ->
                c.bodyToMono(String.class));

        Disposable subscribe = exchange.subscribe();

        try {
            Thread.sleep(5000000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("done");

    }











    public static class TxidLifter<T> implements CoreSubscriber<T>, Subscription {

        private final CoreSubscriber<T> coreSubscriber;
        private final Scannable scannable;
        private final Publisher publisher;

        private Subscription s;

        public TxidLifter(CoreSubscriber<T> coreSubscriber, Scannable scannable, Publisher publisher) {
            this.coreSubscriber = coreSubscriber;
            this.scannable = scannable;
            this.publisher = publisher;
        }

        static Map<Integer, Subscription> subs = new HashMap<>();

        @Override
        public void onSubscribe(Subscription subscription) {
            //TODO 실시간 stack에 추가할 것 : scnnable hashcode 기준 onSubscribe 이후에
            //      onComplete이나 onError 혹은 cancel()이 요청되지 않은 경우
            //--> 어떻게? scannable의 step 인쇄
            //TODO 그러려면 incomplete scannable 목록을 trace context에 보관해야 한다.

            //TODO * 체크 포인트 흐름
            // (subs->next->complete의 scannable은 동일 object 이다.)
            //  - scannable이 subs에서 checkout point 였다면 동일 obj로 next/complte/error를 판단하면 되겠다.
            //  - scannable instanceof OptimizableOperator ==> nextOptimizableSource()
            //   ==> 1st Assembly 만나면 flux, mono에 따라 sanpshot or stacktrace 체크해서 checkpoint 인지 확인한다.
            //      ((MonoOnAssembly)((OptimizableOperator) scannable).nextOptimizableSource()).checkpoint

            //TODO check point인 경우는 메시지를 profile에 add 한다. (onComplete에서도 complete log를 add 한다.)
            //    이때(check point 판단시) 최대 10번까지만 nextOptimizableSource()를 탐색하자.

            //TODO 프로필내에 stack 삽입은
            //    checkpoint로 마킹된 경우만 삽입하자.

            //TODO Exception 감지
            //   reactor.core.publisher.FluxOnAssembly.OnAssemblySubscriber.fail() 호출시 응답값 t에서 stacktrace 구하기
            //     local 변수가 reactor.core.publisher.FluxOnAssembly.OnAssemblyException 타입인 경우에 처리하면 된다.
            //     onAssemblyException.getMessage()
            //     onAssemblyException.getStackTrace()
            //

            System.out.println("onSubs:scannable:" + scannable.name() + ":" + scannable.hashCode());
//            System.out.println("onSubs:publisher:" + publisher + ":" + publisher.hashCode());
            subs.put(subscription.hashCode(), subscription);
            this.s = subscription;
            coreSubscriber.onSubscribe(this);
        }

        @Override
        public void onNext(T t) {
            System.out.println("onNxt:scannable:" + scannable.name() + ":" + scannable.hashCode());
            coreSubscriber.onNext(t);
        }

        @Override
        public void onError(Throwable throwable) {
            System.out.println("onErr:scannable:" + scannable.name() + ":" + scannable.hashCode());
            coreSubscriber.onError(throwable);
        }

        @Override
        public void onComplete() {
            System.out.println("onComp:scannable:" + scannable.name() + ":" + scannable.hashCode());
            coreSubscriber.onComplete();
        }

        @Override
        public Context currentContext() {
            return coreSubscriber.currentContext();
        }

        @Override
        public void request(long n) {
            this.s.request(n);
        }

        @Override
        public void cancel() {
            this.s.cancel();
        }
    }
}
