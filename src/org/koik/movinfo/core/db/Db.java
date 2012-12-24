package org.koik.movinfo.core.db;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.koik.movinfo.core.db.table.MovInfoImpl;


public class Db {
	private static final String driver = "org.sqlite.JDBC";
	private static final String urlPrefix = "jdbc:sqlite:";
	
	static {
		try {
			System.setProperty("sqlite.purejava", "true");
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			// fatal
			throw new Error(e);
		}
	}
	
	final File dbfile;
	final String connString;
	private Db(File dbfile) throws SQLException, IOException {
		this.dbfile = dbfile;
		this.connString = urlPrefix + dbfile.getAbsolutePath();
		if (exists()) {
			if (!isValid()) {
				throw new IOException("Invalid db file found: " + dbfile);
			}
		} else {
			create();
		}
	}
	
	private static final Map<File, Db> instances = new ConcurrentHashMap<File, Db>();
	static Db getInstance(File file) throws IOException, SQLException {
		File cfile = file.getCanonicalFile();
		if (!instances.containsKey(cfile)) {
			synchronized (cfile.toString().intern()) {
				if (!instances.containsKey(cfile)) {
					instances.put(cfile, new Db(file));
				}
			}
		}
		return instances.get(cfile);
	}
	
	private void create() throws SQLException {
		DbHandler.createTable(this, MovInfoImpl.instance(), false);
	}

	public boolean exists() {
		return dbfile.exists();
	}
	
	private boolean isValid() throws IOException, SQLException {
		// file should be readable, writable and a valid db file
		if (dbfile.canRead() && dbfile.canWrite()) {
			return DbHandler.tableExists(this, MovInfoImpl.instance());
		} else {
			throw new IOException(dbfile + " exists but not readable/writable");
		}
	}
	
	public Connection connect() throws SQLException {
		Connection conn = DriverManager.getConnection(connString);
		conn.setAutoCommit(true);
		return conn;
	}

	public void close() {
		// TODO Auto-generated method stub
		
	}
}
