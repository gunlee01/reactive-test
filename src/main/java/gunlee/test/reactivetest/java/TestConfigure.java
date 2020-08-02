package gunlee.test.reactivetest.java;

import org.reactivestreams.Subscription;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Operators;
import reactor.util.context.Context;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 31/07/2020
 */
//@Configuration
public class TestConfigure {

	public static final String MDC_CONTEXT_REACTOR_KEY = TestConfigure.class.getName();

	@PostConstruct
	@SuppressWarnings("unchecked")
	public void contextOperatorHook() {
		Hooks.onEachOperator(MDC_CONTEXT_REACTOR_KEY, Operators.lift((scannable, subscriber) -> new MdcContextLifter(subscriber)));
	}

	@Bean
	public MdcLoggingFilter mdcLoggingFilter() {
		return new MdcLoggingFilter();
	}

	@PreDestroy
	public void cleanupHook() {
		Hooks.resetOnEachOperator(MDC_CONTEXT_REACTOR_KEY);
	}


	/**
	 * Helper that copies the state of Reactor [Context] to MDC on the #onNext function.
	 */
	public static class MdcContextLifter<T> implements CoreSubscriber<T> {

		private final CoreSubscriber<T> coreSubscriber;

		public MdcContextLifter(CoreSubscriber<T> coreSubscriber) {
			this.coreSubscriber = coreSubscriber;
		}

		@Override
		public void onSubscribe(Subscription subscription) {
			coreSubscriber.onSubscribe(subscription);
		}

		@Override
		public void onNext(T t) {
			copyToThread(coreSubscriber.currentContext());
			coreSubscriber.onNext(t);
		}

		@Override
		public void onError(Throwable throwable) {
			coreSubscriber.onError(throwable);
		}

		@Override
		public void onComplete() {
			coreSubscriber.onComplete();
		}

		@Override
		public Context currentContext() {
			return coreSubscriber.currentContext();
		}

		/**
		 * Extension function for the Reactor [Context]. Copies the current context to the MDC, if context is empty clears the MDC.
		 * State of the MDC after calling this method should be same as Reactor [Context] state.
		 * One thread-local access only.
		 */
		void copyToThread(Context context) {
			if (context != null && !context.isEmpty()) {
				Object traceId = context.get("traceId");
				MDC.put("myTraceId", (String) traceId);
			} else {
				MDC.clear();
			}
		}
	}

	public static class MdcLoggingFilter implements WebFilter {
		@Override
		public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
			Mono<Void> filter = chain.filter(exchange);
			return filter.subscriberContext(ctx -> ctx.put("traceId", "100"));
		}
	}
}
