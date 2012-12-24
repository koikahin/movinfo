package org.koik.movinfo.core.db.table;

public final class DataType<Type> {
	public static final DataType<String>
		STRING = new DataType<String>(String.class, "string");
	
	private final String dbtype;
	private final Class<Type> clazz;
	
	
	private DataType(Class<Type> clazz, String dbtype) {
		this.clazz = clazz;
		this.dbtype = dbtype;
	}
	
	public String getDbtype() {
		return dbtype;
	}
	
	public Class<Type> getJavaType() {
		return clazz;
	}
	
	@Override
	public String toString() {
		return dbtype +"|" + clazz.getSimpleName();
	}
}