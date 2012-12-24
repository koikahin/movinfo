package org.koik.movinfo.output.csv;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.koik.movinfo.core.Plugin;
import org.koik.movinfo.core.db.DbChangeListener;
import org.koik.movinfo.core.db.Event;
import org.koik.movinfo.core.db.MovInfoDb;
import org.koik.movinfo.core.db.table.Column;
import org.koik.movinfo.core.db.table.MovInfo;
import org.koik.movinfo.core.db.table.Table.Record;
import org.koik.movinfo.log.Logger;
import org.koik.movinfo.util.ComparablePair;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandNames="csv", commandDescription="Provides movie information as a CSV file")
public class CSVOutput implements Plugin {
	@Parameter(names={"-f", "--file"}, required=false, description="Specify the name of the file to write to. Warning: the file specified will be overwritten.")
	String filename = "movinfo.csv";
	
	@Parameter(names={"-d", "--delimiter"}, required=false, description="Specify the delimiter to use in the csv file")
	String delim = ",";
	
	File dir;
	MovInfoDb midb;
	Map<String, File> todo;

	@Override
	public void init(File dir, Map<String, File> files, MovInfoDb db) {
		this.dir = dir;
		this.todo = files;
		this.midb = db;
		midb.registerListenter(new DbChangeListener<MovInfo>(){
			@Override
			public void registerEvent(Event e, Record<MovInfo> record) {
				Logger.debugPad("got event", e, "for record", record);
				// irrespective of the event, just note which record changed.
				// we'll reconcile them during close()
				changed.add(new ComparablePair<String, String>(record.getColumnValue(MovInfo.Filename), record.getColumnValue(MovInfo.Id)));
			}
		});
	}
	Set<ComparablePair<String, String>> changed = new HashSet<>();
	
	SortedMap<ComparablePair<String, String>, Map<Column<?>, Object>> records = Collections.synchronizedSortedMap(new TreeMap<ComparablePair<String, String>, Map<Column<?>, Object>>());
	@Override
	public Void run(Void a) throws Exception {
		Logger.debugPad("creating csv for dir", dir, "files", todo.keySet());
		List<Record<MovInfo>> dbrecords = midb.listAll();
		Logger.debugPad("no. of db records", dbrecords.size());
		for (Record<MovInfo> rec : dbrecords) {
			ComparablePair<String, String> key = new ComparablePair<>(rec.getColumnValue(MovInfo.Filename), rec.getColumnValue(MovInfo.Id));
			Map<Column<?>, Object> contents = rec.getContents();
			records.put(key, contents);
		}
		
		return null;
	}

	@Override
	public boolean takesCmdLineOptions() {
		return true;
	}

	@Override
	public void close() {
		try {
			write();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void write() throws SQLException {
		reconscileChanges();
		
		File csv = new File(dir, filename);
		if (csv.exists()) {
			if (!csv.delete()) {
				throw new RuntimeException("Unable to delete csv file: " + csv);
			}
		}
		
		PrintWriter writer;
		try {
			writer = new PrintWriter(new BufferedWriter(new FileWriter(csv)));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		List<Column<?>> allcolumns = new ArrayList<>(midb.getTable().columns());
		Set<Column<?>> neverused = new HashSet<>(midb.getTable().columns());
		
		// filter out files that are not to be processed, and figure out which columns are never used
		for (Iterator<Map<Column<?>, Object>> recitr = records.values().iterator(); recitr.hasNext();) {
			Map<Column<?>, Object> rec = recitr.next();
			if (!todo.containsKey(rec.get(MovInfo.Filename))) {
				recitr.remove();
				continue;
			}
			
			if (neverused.size() == 0) 
				break; // all columns are being used by atleast one record
			
			for (Iterator<Column<?>> itr = neverused.iterator(); itr.hasNext(); ) {
				Column<?> col = itr.next();
				if (rec.get(col) != null) {
					itr.remove(); // this column is used
				}
			}
		}
		Logger.debugPad("unused columns", neverused);
		allcolumns.removeAll(neverused);
		
		String header = toLine(allcolumns);
		Logger.debugPad("csv header", header);
		writer.println(header);

		for (Map<Column<?>, Object> rec : records.values()) {
			List<Object> lineobj = new ArrayList<>();
			for (Column<?> col : allcolumns) {
				lineobj.add(rec.get(col));
			}
			String line = toLine(lineobj);
			writer.println(line);
			Logger.debugPad("writing line", line);
			writer.flush();
		}
		
		writer.close();
	}

	private void reconscileChanges() throws SQLException {
		Logger.debugPad("Reconciling changes made during run");
		Set<ComparablePair<String, String>> changedRecs = new HashSet<>(changed);
		Logger.debugPad("Changes detected in entities", changedRecs);
		for (ComparablePair<String, String> rec : changedRecs) {
			Logger.debugPad("processing changed record", rec);
			Record<MovInfo> record = midb.get(rec.getFirst(), rec.getSecond());
			if (record == null) {
				Logger.debugPad("record was removed", rec);
				records.remove(rec);
			} else {
				records.put(rec, record.getContents());
			}
		}
	}

	private String toLine(List<?> allcolumns) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Object o : allcolumns) {
			if (first) first = false;
			else sb.append(delim);
			sb.append("\"").append(o == null ? "" : o.toString()).append("\"");
		}
		return sb.toString();
	}
}
