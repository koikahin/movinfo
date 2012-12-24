package org.koik.movinfo.provider;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.json.simple.parser.ParseException;
import org.koik.movinfo.config.Property;
import org.koik.movinfo.log.Logger;
import org.koik.movinfo.util.Utils;

public class OMDbAPI {
	private OMDbAPI() {}
	
	private static OMDbAPI _instance = null;
	public static OMDbAPI getInstance() {
		if (_instance == null) {
			synchronized (OMDbAPI.class) {
				if (_instance == null) {
					_instance = new OMDbAPI();
				}
			}
		}
		return _instance;
	}
	
	private OMDbSearchResponse search(String query, String year, Set<String> years) 
			throws URISyntaxException, IOException, ParseException {
		URL url = new URI(
				"http", 
				"//www.omdbapi.com/?s=" + query + (year!=null ? "&y="+year : ""), 
				null).toURL();
		return new OMDbSearchResponse(Utils.connect(url), query, years);
	}

	public OMDbSearchResponse search(String cleanFilename, final String year) 
			throws URISyntaxException, IOException, ParseException {
		return search(cleanFilename, year, new HashSet<String>(){{add(year);}});
	}

	public OMDbSearchResponse search(String cleanFilename, Set<String> years) 
			throws URISyntaxException, IOException, ParseException {
		return search(cleanFilename, null, years);
	}

	public OMDbGetResponse get(String query, String year) 
			throws URISyntaxException, IOException, ParseException {
		URL url = new URI(
				"http", 
				"//www.omdbapi.com/?" +    
				"plot=" + Property.plot.value() +
				(year!=null ? "&y="+year : "") + 
				"&t=" + query, 
				null).toURL();
		Logger.debug(" get request for " + query + " for year " + year);
		return new OMDbGetResponse(Utils.connect(url));
	}
	
	public OMDbGetResponse get(String query) throws URISyntaxException, IOException, ParseException {
		return get(query, null);
	}

	public OMDbGetResponse getFromId(String id) throws URISyntaxException, ParseException, IOException {
		URL url = new URI(
				"http", 
				"//www.omdbapi.com/?" +    
				"plot=" + Property.plot.value() +
				"&i=" + id, 
				null).toURL();
		Logger.debug(" get request for " + id);
		return new OMDbGetResponse(Utils.connect(url));
	}
}
