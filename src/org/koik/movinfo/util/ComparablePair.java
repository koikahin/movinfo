package org.koik.movinfo.util;

public class ComparablePair<F extends Comparable<F>, S extends Comparable<S>> extends Pair<F, S> implements Comparable<ComparablePair<F, S>>{
	public ComparablePair(F f, S s) {
		super(f, s);
	}

	@Override
	public int compareTo(ComparablePair<F, S> o) {
		int ret = compare(f, o.getFirst());
		if (ret == 0) {
			return compare(s, o.getSecond());
		} else {
			return ret;
		}
	}
	
	public <T extends Comparable<T>> int compare(T t1, T t2) {
		if (t1 == null) {
			if (t2 == null) {
				return 0;
			} else {
				return (-1)*t2.compareTo(t1);
			}
		} else {
			return t1.compareTo(t2);
		}
	}
}
