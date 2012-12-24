package org.koik.movinfo.core;

import java.io.File;
import java.util.Map;

import org.koik.movinfo.core.db.MovInfoDb;

public interface Plugin extends Service<Void, Void>{
	void init(File dir, Map<String, File> files, MovInfoDb db);
	
	@Override
	public Void run(Void a) throws Exception;
	
	public void close();
	
	public boolean takesCmdLineOptions();
}
