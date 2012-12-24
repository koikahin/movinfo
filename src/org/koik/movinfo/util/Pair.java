package org.koik.movinfo.util;

public class Pair<F, S> {
	F f;
	S s;
	
	public F getFirst() {
		return f;
	}
	
	public S getSecond() {
		return s;
	}
	
	public Pair(F f, S s) {
		this.f = f;
		this.s = s;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object obj) {
		return (obj instanceof Pair) ? isEqual(((Pair) obj).f, this.f) && isEqual(((Pair) obj).s, this.s) : false;
	}
	
	private boolean isEqual(Object o1, Object o2) {
		if (o1 != null) {
			return o1.equals(o2);
		} else {
			return o2 == null;
		}	
	}
	
	@Override
	public String toString() {
		return "("+f+", "+s+")";
	}
}
