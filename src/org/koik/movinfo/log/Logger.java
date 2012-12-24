package org.koik.movinfo.log;

import java.io.PrintStream;

public class Logger {
	private static boolean debug = false;
	public static void setDebug(boolean bool) {
		debug=bool;
	}
	
	public static void warn(Object... str) {
		writeto(System.err, str);
	}

	public static void debug(Object... str) {
		if (debug) writeto(System.out, str);
	}
	
	public static void debugPad(Object... str) {
		if (debug) writeto(true, System.out, str);
	}

	public static void info(Object... str) {
		writeto(System.out, str);
	}
	
	public static void infoPad(Object... str) {
		writeto(true, System.out, str);
	}
	
	private static final void writeto(PrintStream ps, Object... strings) {
		writeto(false, ps, strings);
	}
	
	private static final void writeto(boolean pad, PrintStream ps, Object... strings) {
		StringBuilder sb = new StringBuilder();
		if (debug) sb.append(getCallerPrefix());
		boolean first = true;
		for (Object string : strings) {
			if (!first && pad) 
				sb.append(" | ");
			first = false;
			sb.append(string);
		}
		ps.println(sb.toString());
	}
	private static final String getCallerPrefix() {
		for (StackTraceElement elem : Thread.currentThread().getStackTrace()) {
			if (!Logger.class.getName().equals(elem.getClassName()) &&
					!elem.getClassName().startsWith("java")) {
				return "[" + elem.getClassName().replaceFirst("^.*[.]", "") + "." + elem.getMethodName() + ":" + elem.getLineNumber() + "] ";
			}
		}
		return "";
	}
}
