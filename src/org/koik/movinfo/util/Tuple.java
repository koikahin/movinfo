package org.koik.movinfo.util;

public class Tuple {
	final Object[] o;
	public Tuple(Object... o) {
		this.o = o;
	}
	public Object get(int i){
		return o[i];
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Tuple) 
			return equals((Tuple)obj);
		else 
			return false;
	}
	public boolean equals(Tuple obj) {
		return toString().equals(obj.toString());
	}
	@Override
	public int hashCode() {
		StringBuilder sb = new StringBuilder();
		for (Object so : o) {
			sb.append(so.hashCode());
		}
		return sb.toString().intern().hashCode();
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		boolean b = true;
		for(Object so : o) {
			if (b)
				b = !b;
			else 
				sb.append(",");
			sb.append(so.toString());
		}
		return sb.toString();
	}
}
