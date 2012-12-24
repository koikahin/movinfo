package org.koik.movinfo.core;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class ServiceUtils {
	public static <A, I, R> Service<A, R> pipe(final Service<A, I> s1, final Service<I, R> s2) {
		return new Service<A, R>() {
			@Override
			public R run(A a) throws Exception {
				I intermediate = s1.run(a);
				return s2.run(intermediate);
			}
			
			@Override
			public String toString() {
				return "[" + s1 + "->" + s2 + "]";
			}
		};
	}
	
	private static final ExecutorService common = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
            0, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>());
	public static <A, R> Service<A, Future<R>> async(final Service<A, R> s) {
		return async(s, common);
	}
	
	public static <A, R> Service<A, Future<R>> async(final Service<A, R> s, final ExecutorService es) {
		return new Service<A, Future<R>>() {
			@Override
			public Future<R> run(final A a) throws Exception {
				return es.submit(new Callable<R>() {
					@Override
					public R call() throws Exception {
//						Logger.debug("invoking: " + s + " with: " + a);
						return s.run(a);
					}
				});
			}
			
			@Override
			public String toString() {
				return "async-" + s.toString();
			}
		};
	}
}
