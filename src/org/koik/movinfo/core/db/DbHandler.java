package org.koik.movinfo.core.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.koik.movinfo.core.db.table.Column;
import org.koik.movinfo.core.db.table.Table;
import org.koik.movinfo.core.db.table.Table.Record;
import org.koik.movinfo.log.Logger;
import org.koik.movinfo.util.Pair;

public class DbHandler {
	private static String tablelistQuery = 
			"SELECT name FROM sqlite_master WHERE type IN ('table','view') AND name NOT LIKE 'sqlite_%' " +
			"UNION ALL " +
			"SELECT name FROM sqlite_temp_master WHERE type IN ('table','view') ORDER BY 1";

	public static List<String> getTableList(Db db) throws SQLException {
		return executeInner(db, tablelistQuery, null, new ExecuteAction<List<String>>() {
			@Override
			public List<String> run(Connection c, PreparedStatement ps)
					throws SQLException {
				ResultSet rs = ps.executeQuery();
				List<String> retval = new ArrayList<>();
				while(rs.next()) {
					retval.add(rs.getString(1));
				}
				return retval;
			}
		});
	}
	
	public static <T extends Table<T>> boolean tableExists(Db db, T t) throws SQLException {
		List<String> tables = getTableList(db);
		if (tables == null || !tables.contains(t.name())) 
			return false;
		else 
			return true;
	}
	
	public static <T extends Table<T>> void createTable(Db db, T t, boolean ignoreIfExists) throws SQLException {
		final StringBuffer create = new StringBuffer();
		create.append("CREATE TABLE ").append(ignoreIfExists ? "IF NOT EXISTS " : "").append(t.name()).append(" (");
		boolean first = true;
		for (Column<?> col : t.columns()) {
			if (!first) {
				create.append(", ");
			} else {
				first = false;
			}
			create.append(col.name()).append(" ").append(col.getType().getDbtype());
		}

		// handle primary key
		Column<?> pk = t.getPKColumn();
		if (pk != null)
			create.append(", PRIMARY KEY (").append(pk.name()).append(") ");
		
		// handle unique indices
		for (Set<Column<?>> idx : t.getUniqueConstraints()) {
			create.append(", UNIQUE (");
			boolean secondFirst = true; // :P
			for (Column<?> col : idx) {
				if (!secondFirst) create.append(", "); 
				else secondFirst = false;
				create.append(col.name());
			}
			create.append(") ");
		}
		
		create.append(") ");
		
		executeInner(db, create.toString(), null, new ExecuteAction<Void>() {
			@Override
			public Void run(Connection c, PreparedStatement ps) throws SQLException {
				ps.execute();
				return null;
			}
		});
	}
	
	public static <T extends Table<T>, R extends Record<T>>  void insert(Db db, final R r) throws SQLException {
		Pair<String, Map<Integer, Object>> clause = insertClausePS(r);
		
		executeInner(db, "insert into " + r.getTable().name() + " " + clause.getFirst(), clause.getSecond(), new ExecuteAction<Void>() {
			@Override
			public Void run(Connection c, PreparedStatement ps) throws SQLException {
				try {
					ps.execute();
					return null;
				} finally {
					notifyListeners(r.getTable(), Event.INSERT, r);
				}
			}
		});
	}
	
	public static <T extends Table<T>> List<Record<T>> select(Db db, final T t, Map<Column<?>, Object> colvals) throws SQLException {
		String clause = "select * from " + t.name();
		Map<Integer, Object> psvals = null;
		if (colvals != null) {
			Pair<String, Map<Integer, Object>> whereclause = whereClausePS(t, colvals);
			clause = clause + " " + whereclause.getFirst();
			psvals = whereclause.getSecond();
		}
		
		return executeInner(db, clause, psvals, new ExecuteAction<List<Record<T>>> () {
			@SuppressWarnings("unchecked")
			@Override
			public List<Record<T>> run(Connection c, PreparedStatement ps) throws SQLException {
				List<Record<T>> retval = new ArrayList<>();
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					Record<T> record = t.newRecord();
					for (Column<?> col : t.columns()) {
						Object val = rs.getObject(col.name());
//						Logger.debugPad("setting col", col, "value", val);
						record.setColumnValue((Column<Object>)col, val);
					}
					retval.add(record);
				}
				return retval;
			}
		});
	}

	public static <T extends Table<T>> int delete(Db db, final Record<T> r) throws SQLException {
		Map<Column<?>, Object> where = getUniqueColumnSet(r);
		final T t = r.getTable();
		
		Pair<String, Map<Integer, Object>> whereclause = whereClausePS(t, where);
		
		String clause = "delete from " + t.name() + " " + whereclause.getFirst();
		return executeInner(db, clause, whereclause.getSecond(), new ExecuteAction<Integer> () {
			@Override
			public Integer run(Connection c, PreparedStatement ps) throws SQLException {
				try {
					return ps.executeUpdate();
				} finally {
					notifyListeners(t, Event.DELETE, r);
				}
			}
		});
	}
	
	public static <T extends Table<T>> int delete(Db db, final T t, Map<Column<?>, Object> colvals) throws SQLException {
		int noofmods = 0;
		for (Record<T> rec : select(db, t, colvals)) {
			noofmods += delete(db, rec);
		}
		return noofmods;
	}
	
	public static <T extends Table<T>> boolean contains(Db db, Record<T> r) throws SQLException {
		Map<Column<?>, Object> where = getUniqueColumnSet(r);
		if (where == null) 
			where = r.getContents();
		return select(db, r.getTable(), where).size() > 0;	
	}
	
	private static final <T extends Table<T>> Map<Column<?>, Object> getUniqueColumnSet(Record<T> r) {
		Map<Column<?>, Object> contents = r.getContents();
		Map<Column<?>, Object> retval = null;
		
		T t = r.getTable();
		Column<?> pkcol = t.getPKColumn();
		
		if (pkcol != null && contents.containsKey(pkcol)) {
			retval = new HashMap<>();
			retval.put(pkcol, contents.get(pkcol));
			return retval;
		} else {
			for (Set<Column<?>> uniqueIndex : t.getUniqueConstraints()) {
				if (contents.keySet().containsAll(uniqueIndex)) {
					retval = new HashMap<>(contents);
					retval.keySet().retainAll(uniqueIndex);
					return retval;
				} 
			}
			return retval;
		}
	}
	
	private static final <V> V executeInner(
			Db db, 
			String query, 
			Map<Integer, Object> psvals, 
			ExecuteAction<V> action) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			// There are serious concurrency issues to fix. Till then, let's just sync all operations!
			synchronized(db) {
				conn = db.connect();
				if (query != null) {
					Logger.debugPad("going to prepare statement for query", query);
					stmt = conn.prepareStatement(query);
				} 
				 
				if (psvals != null) {
					Logger.debugPad("setting values", psvals);
					insertIntoPS(stmt, psvals);
				}
				return action.run(conn, stmt);
			}
		} finally {		
			if (stmt != null)
				stmt.close();
			if (conn != null)
				conn.close();
		}
	}
	
	static interface ExecuteAction<V> {
		V run(Connection c, PreparedStatement ps) throws SQLException;
	}
	
	private static final void insertIntoPS(PreparedStatement ps, Map<Integer, Object> psvals) throws SQLException {
		for (Map.Entry<Integer, Object> val : psvals.entrySet()) {
			ps.setObject(val.getKey(), val.getValue());
		}
	}
	
	private static final <T extends Table<T>> Pair<String, Map<Integer, Object>> whereClausePS(T t, Map<Column<?>, Object> colvals) throws SQLException {
		Map<Integer, Object> psvals = new HashMap<>();
		StringBuilder where = new StringBuilder();
		int qcount = 0;
		boolean first = true;
		for (Map.Entry<Column<?>, Object> cdata : colvals.entrySet()) {
			if (first) {
				where.append(" where ");
				first = false;
			} else {
				where.append(" and ");
			}

			Object val = cdata.getValue();
			if (val == null) {
				where.append(cdata.getKey().name()).append(" IS NULL");	
			} else {
				qcount++;
				where.append(cdata.getKey().name()).append(" = ").append("?");
				psvals.put(qcount, val);	
			}
		}
		
		return new Pair<>(where.toString(), psvals);
	}
	
	private static final <T extends Table<T>> Pair<String, Map<Integer, Object>> insertClausePS(Record<T> r) throws SQLException {
		Map<Integer, Object> psvals = new HashMap<>();
		StringBuilder values = new StringBuilder();
		StringBuilder columns= new StringBuilder();
		Map<Column<?>, Object> data = r.getContents();
		int i = 0;
		for (Map.Entry<Column<?>, Object> cdata : data.entrySet()) {
			i++;
			if (i == 1) {
				columns.append(" (");
				values.append(" values (");
			} else {
				columns.append(", ");
				values.append(", ");
			}
			columns.append(cdata.getKey().name());
			values.append("?");
			psvals.put(i, (Object)cdata.getValue());
		}
		
		if (i == 0) {
			// nothing added
			throw new SQLException("Cannot insert a blank record: " + r);
		}
		
		columns.append(")");
		values.append(")");
		
		return new Pair<>(columns.append(values).toString(), psvals);
	}

	private static final Map<Table<?>, Vector<DbChangeListener<?>>> callbacks = new ConcurrentHashMap<Table<?>, Vector<DbChangeListener<?>>>();
	public synchronized static <T extends Table<T>> void registerListener(T table, DbChangeListener<T> cb) {
		if (!callbacks.containsKey(table)) 
			callbacks.put(table, new Vector<DbChangeListener<?>>());
		callbacks.get(table).add(cb);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static <T extends Table<T>, R extends Record<T>> void notifyListeners(T table, Event e, R r) {
		if (callbacks.containsKey(table)) {
			for (DbChangeListener<?> cb : callbacks.get(table)) {
				cb.registerEvent(e, (Record)r);
			}	
		}
	}
}
