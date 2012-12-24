package org.koik.movinfo.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.koik.movinfo.log.Logger;


public class Utils {
	private static final ConcurrentHashMap<Tuple, List<String>> loadCache = new ConcurrentHashMap<Tuple, List<String>>();

	public static List<String> load(String file, boolean trim, boolean tolower) throws IOException {
		Tuple args = new Tuple(file, trim, tolower);
		if (!loadCache.containsKey(args)) {
			synchronized(args.toString().intern()) {
				if (!loadCache.containsKey(args)) {
					List<String> retval = new ArrayList<String>();
					BufferedReader br = new BufferedReader(loadResource(file));
					String line; 
					while ( (line = br.readLine()) != null ) {
						retval.add(
								trim ? (tolower ? line.toLowerCase() : line).trim() : (tolower ? line.toLowerCase() : line));
					}
					br.close();
					loadCache.put(args, retval);
				}
			}
		}
		return new ArrayList<>(loadCache.get(args));
	}
	
	public static final StringReader loadResource(String name) throws IOException {		
		InputStream is = Utils.class.getResourceAsStream(name);
		if (is == null) {
			is = Utils.class.getResourceAsStream("/" + name);
		}
		String content = readIntoString(is);
		is.close();
		return new StringReader(content);
	}
	
	public static final String readIntoString(InputStream is) {
		Scanner s = new Scanner(is);
		s.useDelimiter("\\A");
		try {
			return s.hasNext() ? s.next() : "";
		} finally {
			s.close();
		}
	}
	
	public static File[] listValidFiles(File dir, final boolean inclDirs) throws IOException {
		return listValidFiles(dir, inclDirs, null);
	}
	
	public static File[] listValidFiles(final File dir, final boolean inclDirs, final String pattern) throws IOException {
		final List<String> validExtns = Utils.load("extensions", true, true);
		
		return dir.listFiles(new FileFilter() {	
				@Override
				public boolean accept(File pathname) {
					if (pathname.isDirectory() && inclDirs)
						return patternMatches(pathname, pattern);
					else 
						return validExtns.contains(Utils.getExtn(pathname.getName())) && patternMatches(pathname, pattern);
				}
			});
	}
	
	private static boolean patternMatches(File file, String pattern) {
		return (pattern == null ? true : file.getName().toLowerCase().contains(pattern.toLowerCase()));
	}
	
	public static Object getExtn(String name) {
		return name.replaceAll(".*[.]", "").toLowerCase();
	}
	
	public static String[] pickYears(String str) {
//		Pattern p = Pattern.compile("(^|[^0-9])(19|20)[0-9][0-9]($|[^0-9])");
		Pattern p = Pattern.compile("(19|20)[0-9][0-9]");
		final Matcher m = p.matcher(str);
		return new ArrayList<String>(){{
			while (m.find()) {
				add(m.group());
			}
		}}.toArray(new String[0]);
	}
	
	public static String connect(URL url) throws IOException {
		try {
			Logger.debug("connect: " + url.toURI().toASCIIString());
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return readIntoString(url.openConnection().getInputStream());
	}
	
	
	private static int count(Pattern p, String str) {
		Matcher m = p.matcher(str);
		if (m.find() && m.groupCount() == 1) {
			return Integer.parseInt(m.group(1));
		} else {
			Logger.debugPad("Couldn't match pattern", p, "string", str);
			return 0;
		}
	}
	

	private static Pattern hrp = Pattern.compile("(\\d+)\\s*h|hr|hrs|hour|hours");
	private static Pattern minp = Pattern.compile("(\\d+)\\s*m|min|mins|minute|minutes");
	private static Pattern secp = Pattern.compile("(\\d+)\\s*s|sec|secs|second|seconds");
	public static int toMins(String durationStr) {
		int seconds = 0;
		if (durationStr != null) {
			seconds += TimeUnit.SECONDS.convert(count(hrp, durationStr), TimeUnit.HOURS);
			seconds += TimeUnit.SECONDS.convert(count(minp, durationStr), TimeUnit.MINUTES);
			seconds += TimeUnit.SECONDS.convert(count(secp, durationStr), TimeUnit.SECONDS);
		}
		int minutes = (seconds/60);
		Logger.debugPad("seconds", seconds);
		Logger.debugPad("minutes", minutes);
		return minutes;
	}

}
