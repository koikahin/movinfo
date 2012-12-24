package org.koik.movinfo.core.db;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.koik.movinfo.core.db.table.Column;
import org.koik.movinfo.core.db.table.MovInfo;
import org.koik.movinfo.core.db.table.MovInfoImpl;
import org.koik.movinfo.core.db.table.Table;
import org.koik.movinfo.core.db.table.Table.Record;
import org.koik.movinfo.log.Logger;

public class MovInfoDb {
	private static final String _dbfileName = "movinfo.db";
	
	final File dir;
	final Db db;
	private MovInfoDb(File dir, boolean clean) throws SQLException, IOException {
		this.dir = dir;
		File dbfile = new File(dir, _dbfileName);
		if (clean)
			dbfile.delete();
		db = Db.getInstance(dbfile);
		mergeDbs();
	}
	
	private static final Map<File, MovInfoDb> instances = new ConcurrentHashMap<>();
	public static MovInfoDb getInstance(File dir) throws IOException, SQLException {
		return getInstance(dir, false);
	}
	public static MovInfoDb getInstance(File dir, boolean clean) throws IOException, SQLException {
		File cdir = dir.getCanonicalFile();
		if (!instances.containsKey(cdir)) {
			synchronized (cdir.toString().intern()) {
				if (!instances.containsKey(cdir)) {
					instances.put(cdir, new MovInfoDb(dir, clean));
				}
			}
		}
		return instances.get(cdir);
	}
	
	private void mergeDbs() throws IOException, SQLException {
		/*
		 * While copying a file to a destination which has a file with the 
		 * same name, different OSes (and different versions of the same OS) 
		 * treat renaming of the file differently. So, we list all files matching 
		 * .*minfo.*[.]db and try to see which of these are valid 
		 * database files. We try merging them into the primary, and delete 
		 * the copies.
		 */
		for (File file : dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isFile() && file.getName().toLowerCase().matches("^.*minfo.*[.]db$");
			}})) {
			if (file.getName().equals(_dbfileName)) {
				continue; // don't merge the same file with itself!
			}
			
			Db tmpdb = null;
			try {
				tmpdb = Db.getInstance(file);
			} catch (final Exception e) {
				// probably not a db file!
				Logger.debugPad(file, "skipped because of exception", new StringWriter(){{e.printStackTrace(new PrintWriter(this));close();}}.toString());
			}
			Logger.info("merging " + _dbfileName + " file: " + file);
			for (Record<MovInfo> tmprec : DbHandler.select(tmpdb, MovInfoImpl.instance(), null)) {
				// we merge iff we don't already have the same record
				if (!DbHandler.contains(db, tmprec)) {
					DbHandler.insert(db, tmprec);
				}
			}
			tmpdb.close();
			if (!file.delete()) {
				Logger.warn("File: " + file + " couldn't be removed. Delete will be attempted on exit.");
				file.deleteOnExit();
			}
		}
	}

	public Set<String> getProcessedFiles() throws SQLException {
		Set<String> files = new HashSet<>();
		
		for (Record<MovInfo> row : listAll()){
			files.add(row.getColumnValue(MovInfo.Filename));
		}
		return files;
	}

	private List<Record<MovInfo>> listAll(Map<Column<?>, Object> clause) throws SQLException {
		return DbHandler.select(
				db, 
				MovInfoImpl.instance(), 
				clause);
	}
	
	public List<Record<MovInfo>> listAll() throws SQLException {
		return listAll((Map<Column<?>, Object>)null);
	}
	
	@SuppressWarnings("serial")
	public List<Record<MovInfo>> listAll(final String filename) throws SQLException {
		return listAll(new HashMap<Column<?>, Object>(){{put(MovInfo.Filename, filename);}});
	}

	@SuppressWarnings("serial")
	public Record<MovInfo> get(final String filename, final String id) throws SQLException {
		List<Record<MovInfo>> list = listAll(new HashMap<Column<?>, Object>(){{put(MovInfo.Filename, filename); put(MovInfo.Id, id);}});
		return (list == null || list.size() == 0 ? null : list.get(0));
	}

	public void deleteAll(Set<String> stalefiles) throws SQLException {
		for (final String filename : stalefiles) {
			deleteAll(filename);
		}
	}
	
	public void insert(Record<MovInfo> rec) throws SQLException {
		DbHandler.insert(db, rec);
	}
	
	public int delete(Record<MovInfo> rec) throws SQLException {
		return DbHandler.delete(db, rec);
	}
	
	@SuppressWarnings("serial")
	public int deleteAll(final String filename) throws SQLException {
		return DbHandler.delete(
				db, 
				MovInfoImpl.instance(), 
				new HashMap<Column<?>, Object>(){{
					put(MovInfo.Filename, filename);}});
	}
	
	@SuppressWarnings("serial")
	public int delete(final String filename, final String id) throws SQLException {
		return DbHandler.delete(
				db, 
				MovInfoImpl.instance(), 
				new HashMap<Column<?>, Object>(){{
					put(MovInfo.Filename, filename);
					put(MovInfo.Id, id);}});
	}
	
	public Record<MovInfo> newRecord() {
		return MovInfoImpl.instance().newRecord();
	}

	public void close() {
		db.close();
	}
	
	public Table<MovInfo> getTable() {
		return MovInfoImpl.instance();
	}
	
	public void registerListenter(DbChangeListener<MovInfo> cl) {
		DbHandler.registerListener(MovInfoImpl.instance(), cl);
	}
}
