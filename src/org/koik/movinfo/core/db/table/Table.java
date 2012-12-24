package org.koik.movinfo.core.db.table;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.koik.movinfo.core.db.Db;


public interface Table<T extends Table<T>> {
	
	public String name();
	
	/**
	 * Return a linked hashmap from 
	 * 	ColumnName -> Type (as a java class)
	 * 
	 * @return
	 */
	public List<Column<?>> columns();
	
	public Column<?> getPKColumn();
	
	public List<Set<Column<?>>> getUniqueConstraints();
	
	public Record<T> newRecord();
	
	public interface Record<T> {
		public void insert(Db db) throws SQLException;
		
		public void delete(Db db) throws SQLException;
		
		public Map<Column<?>, Object> getContents();
		
		public <Type> Type getColumnValue(Column<Type> col);
		
		public <Type> void setColumnValue(Column<Type> co, Type val);
		
		// A record belongs to a table
		public T getTable();
	}
}
