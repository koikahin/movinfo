package org.koik.movinfo.core.db.table;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.koik.movinfo.core.db.Db;
import org.koik.movinfo.core.db.DbHandler;

@SuppressWarnings("serial")
public class MovInfoImpl implements MovInfo {	
	public static final List<Set<Column<?>>> uniqueKeys = new Vector<>();
	
	static {
		uniqueKeys.add(new HashSet<Column<?>>(){{add(Filename); add(Id);}});
	}
	
	@Override
	public String name() {
		return name;
	}

	@Override
	public List<Column<?>> columns() {
		return Column.getColumns(MovInfo.class);
	}

	@Override
	public Column<?> getPKColumn() {
		return null;
	}

	@Override
	public List<Set<Column<?>>> getUniqueConstraints() {
		return uniqueKeys;
	}

	@Override
	public Record<MovInfo> newRecord() {
		MovRecordImpl rec = new MovRecordImpl(this);
		return rec;
	}
	
	public class MovRecordImpl implements MovRecord {
		MovInfoImpl table;
		private MovRecordImpl(MovInfoImpl t) {
			this.table = t;
		}
		
		@Override
		public void delete(Db db) throws SQLException {
			DbHandler.delete(db, this);
		}

		@Override
		public void insert(Db db) throws SQLException {
			DbHandler.insert(db, this);
		}

		private Map<Column<?>, Object> contents = new HashMap<>();
		
		@Override
		public Map<Column<?>, Object> getContents() {
			return contents;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <Type> Type getColumnValue(Column<Type> col) {
			return ((Type) contents.get(col));
		}

		@Override
		public <Type> void setColumnValue(Column<Type> co, Type val) {
			contents.put(co, val);
			/*
			 * SQLite is playing spoilsport. It is returning an Integer even if its type is set to string
			 */
//			if (val == null || co.getType().getJavaType().isInstance(val))
//				contents.put(co, val);
//			else 
//				throw new RuntimeException("The column " + co + "'s type is " + co.getType().getJavaType().getSimpleName() + 
//						" whereas that of the object being submitted is " + val.getClass());
		}

		@Override
		public MovInfo getTable() {
			return table;
		}
	}

	private MovInfoImpl(){}
	private static final MovInfoImpl _instance;
	static {
		_instance = new MovInfoImpl();
	}
	public static MovInfo instance() {
		return _instance;
	}
}
