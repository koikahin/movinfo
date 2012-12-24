package org.koik.movinfo.provider;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.koik.movinfo.core.Plugin;
import org.koik.movinfo.core.db.MovInfoDb;
import org.koik.movinfo.core.db.table.Column;
import org.koik.movinfo.core.db.table.MovInfo;
import org.koik.movinfo.core.db.table.Table.Record;
import org.koik.movinfo.log.Logger;
import org.koik.movinfo.provider.OMDbResult.Attribute;
import org.koik.movinfo.util.Pair;

public class SyncManager implements Plugin {
	File dir; 
	Map<String, File> files;
	private MovInfoDb movinfodb;
	
	@Override
	public void init(File dir, Map<String, File> files, MovInfoDb dbman) {
		this.dir = dir;
		this.files = files;
		this.movinfodb = dbman;
	}
	
	private void process(Collection<File> files) throws IOException, InterruptedException, Exception {
		Sink dbsink = new DbWriter();
		new Processor(dbsink).process(files);
	}

	@SuppressWarnings("serial")
	@Override
	public Void run(Void a) throws Exception {
		final Set<String> alreadyProcessed = movinfodb.getProcessedFiles();
		Set<String> todo = new HashSet<String>(files.keySet()){{removeAll(alreadyProcessed);}};
		
		Set<String> staleInDb = new HashSet<String>(alreadyProcessed){{removeAll(files.keySet());}};
		Logger.debugPad("present in db", alreadyProcessed.size(), "present in dir", files.size(), "new in dir", todo.size(), "stale in db", staleInDb.size());
		
		// remove stale records from the db
		movinfodb.deleteAll(staleInDb);
		
		// retain only new entries to be processed
		files.keySet().retainAll(todo);
		
		Logger.info("Dir: " + dir + ". No. of files to get information for: " + files.size());
		process(files.values());
		
		return null;
	}

	@Override
	public void close() {
		movinfodb.close();
	}

	@Override
	public boolean takesCmdLineOptions() {
		return false;
	}
	
	class DbWriter implements Sink {
		@SuppressWarnings("unchecked")
		@Override
		public Void run(Pair<File, List<OMDbResult>> reslist) throws Exception {
			final String filename = reslist.getFirst().getName();
			final List<OMDbResult> results = reslist.getSecond();
			if (results.size() == 0) {
				Record<MovInfo> rec = movinfodb.newRecord();
				rec.setColumnValue(MovInfo.Filename, filename);
				movinfodb.insert(rec);
			} else {
				for (OMDbResult result : results) {
					Record<MovInfo> rec = movinfodb.newRecord();
					rec.setColumnValue(MovInfo.Filename, filename);
					for (Attribute a : Attribute.values()) {
						rec.setColumnValue((Column<Object>)a.getCorrespondingColumn(), result.get(a));
					}
					Logger.debugPad("writing to db", filename, result);
					try {
						movinfodb.insert(rec);
					} catch (SQLException ex) {
						Logger.debugPad("SQLException caught for", filename, result, ex);
						throw ex;
					}
				}
			}
			
			return null;
		}
	}
}
