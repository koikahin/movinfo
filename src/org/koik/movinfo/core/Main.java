package org.koik.movinfo.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.koik.movinfo.core.db.MovInfoDb;
import org.koik.movinfo.log.Logger;
import org.koik.movinfo.provider.SyncManager;
import org.koik.movinfo.util.Utils;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

public class Main {
	
	private static final Map<String, Plugin> plugins = new HashMap<String, Plugin>();
	public static void main(String[] args) throws IOException, Exception {
		Main main = new Main();
		
		JCommander parser = new JCommander();
		parser.addObject(main);
		parser.setProgramName("java -jar movinfo.jar");
		
		List<String> splugins = Utils.load("plugins", true, false);
		for (String splugin : splugins) {
			String[] pair = splugin.split("=");
			if (pair.length != 2) 
				throw new RuntimeException("Invalid plugins file");
			
			String name = pair[0];
			Plugin plugin = (Plugin)Class.forName(pair[1]).newInstance();
			plugins.put(name, plugin);
			
			if (plugin.takesCmdLineOptions())
				parser.addCommand(name, plugin);
		}
		
		try {
			parser.parse(args);
		} catch (ParameterException ex) {
			System.err.println("There was a problem with the arguments passed: " + ex.getMessage());
			System.out.println("Usage: ");
			parser.usage();
		}
		if (main.help) {
			parser.usage();
			System.exit(0);
		}
		Logger.setDebug(main.verbose);
		main.setPlugin(plugins.get(parser.getParsedCommand()));
		main.start();
	}
	
	@Parameter(names={"-h", "--help"}, description="Displays this help")
	boolean help = false;
	
	@Parameter(names={"-c", "--clean"}, description="Clean the DB file and start fresh")
	boolean clean = false;
	
	@Parameter(names={"-d", "--dir"}, required=false, description="The movies directory to work within (Default: <current dir>)")
	private String dir=System.getProperty("user.dir");
	
	@Parameter(names={"-f", "--file"}, required=false, description="Filename (or approximate pattern) to include. Wildcards/regex are not supported")
	private String filepattern = null;
	
	@Parameter(names={"-v", "--verbose"}, required=false, description="Provide verbose output (for debugging)")
	boolean verbose = false;
	
	@Parameter(names={"-n", "--noget", "-nosync"}, required=false, description="Don't get missing movie information from the internet")
	boolean nosync = false;
	
	private void start() throws Exception {
		Logger.debug("Dir: " + dir);
		File fdir = new File(dir);
		
		if (!fdir.exists() || !fdir.canRead() || !fdir.canWrite()) {
			throw new IOException("Dir " + dir +" either doesn't exist, or can not be read from or written into");
		}
		
		MovInfoDb midb = MovInfoDb.getInstance(fdir, clean);
		
		final HashMap<String, File> files = new HashMap<>();
		for (File file : Utils.listValidFiles(fdir, true, filepattern)) {
			files.put(file.getName(), file);
		}
		
		List<Future<?>> futures = new ArrayList<>();
		List<Plugin> running = new ArrayList<>();
		if (!nosync) {
			SyncManager sync = new SyncManager();
			sync.init(fdir, new HashMap<String, File>(files), midb);
			running.add(sync);
			futures.add(ServiceUtils.async(sync).run(null));
		}
		
		if (plugin != null) {
			plugin.init(fdir, new HashMap<>(files), midb);
			running.add(plugin);
			futures.add(ServiceUtils.async(plugin).run(null));
		}
		
		// wait for all of them to finish
		for (Future<?> future : futures) {
			future.get();
		}
		
		for (Plugin p : running) {
			p.close();
		}
	}


	private Plugin plugin = null;
	private void setPlugin(Plugin plugin) {
		this.plugin = plugin;
	}
}
