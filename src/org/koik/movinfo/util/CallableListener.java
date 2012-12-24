package org.koik.movinfo.util;

import java.util.concurrent.Callable;

public class CallableListener<V> implements Callable<V> {
	final EventCallback<V> c;
	final Callable<V> delegate;
	public CallableListener(Callable<V> delegate, EventCallback<V> c) {
		this.delegate = delegate;
		this.c = c;
	}
	
	@Override
	public V call() throws Exception {
		c.started();
		V ret = null;
		Exception ex = null;
		try {
			ret = delegate.call();
			return ret;
		} catch (Exception e) {
			ex = e;
			throw e;
		} finally {
			c.finished(ret, ex);
		}
	}
}
