package org.koik.movinfo.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.koik.movinfo.util.Utils;

public enum Property {
	threadCount("thread.count"),
	loadRt("tomatoes"),
	rtAsync("tomatoes.async"),
	plot("plot", "full", "short"),
	minRuntime("min.runtime");
	
	
	final String key, value;
	private Property(String key, String... valid) {
		this.key = key;
		value = load(key, valid);
	}
	private String load(String key, String... valid) {
		String value = System.getProperty(key, PropertyConfig.config.getProperty(key));
		if (valid == null || valid.length == 0)
			return value;
		
		List<String> validValues = Arrays.asList(valid);
		if (validValues.contains(value)) 
			return value;
		else 
			throw new RuntimeException("Invalid value for property: " + key +". Valid values: " + validValues);
	}
	public String key() {
		return key;
	}
	public String value() {
		return value;
	}
	public int intValue() {
		return Integer.parseInt(value());
	}
	
	static class PropertyConfig {
		private static final Properties config;
		static {
			try {
				config = new Properties(){{load(Utils.loadResource("movinfo.properties"));}};
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
