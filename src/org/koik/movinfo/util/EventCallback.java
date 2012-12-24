package org.koik.movinfo.util;

public interface EventCallback<V> {
	void started();
	void finished(V ret, Exception e);
}
