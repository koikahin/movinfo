package org.koik.movinfo.core.db.table;

import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;


public final class Column<Type> {
	private final String name;
	private final DataType<Type> type;
	
	public String name() {
		return name;
	}
	
	public DataType<Type> getType() {
		return type;
	}
	
	<T extends Table<T>> Column (Class<T> table, String name, DataType<Type> type) {
		this.name = name;
		this.type = type;
		register(table, this);
	}
	
	private static final Map<Class<? extends Table<?>>, List<Column<?>>> config = new ConcurrentHashMap<>();
	public static synchronized <T extends Table<T>> List<Column<?>> getColumns(Class<T> table) {
		if (!config.containsKey(table))
			config.put(table, new Vector<Column<?>>());
		return config.get(table);
	}
	
	private static <T extends Table<T>> void register(Class<T> table, Column<?> c) {
		getColumns(table).add(c);
	}
	
	@Override
	public String toString() {
		return name;
	}
}
