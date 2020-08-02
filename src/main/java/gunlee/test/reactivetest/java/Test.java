package gunlee.test.reactivetest.java;

import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import org.jetbrains.annotations.NotNull;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 29/07/2020
 */
public class Test {

	public void test() {
//		ContinuationKt..context;
//		Continuation
		new Cont().getContext();

	}

	public static class Cont implements Continuation {

		@NotNull
		@Override
		public CoroutineContext getContext() {
			return null;
		}

		@Override
		public void resumeWith(@NotNull Object o) {

		}
	}
}
