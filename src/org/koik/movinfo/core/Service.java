package org.koik.movinfo.core;

public interface Service<A, R> {
	R run(A a) throws Exception;
}
