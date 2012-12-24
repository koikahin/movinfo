package org.koik.movinfo.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

public class OMDbSearchResponse extends OMDbAbstractResponse {

	public OMDbSearchResponse(String response, String query, Set<String> tables) throws ParseException {
		super(response);
		if (success()) 
			parse(getResponseJson(), query, tables);
	}
	
	protected void parse(JSONObject json, String query, Set<String> tables) {
		if (json.containsKey("Search")) {
			for (Object hit : (JSONArray)json.get("Search")) {
				JSONObject jhit = (JSONObject) hit;
				OMDbSearchResult res = new OMDbSearchResult(
						(String)jhit.get("Title"),
						(String)jhit.get("Year"), 
						(String)jhit.get("imdbID"), 
						query, 
						tables);
				addResult(res);
			}
		} else {
			super.setFailure("Incomprehensible reply: " + json);
		}
	}

	List<OMDbSearchResult> results = new ArrayList<OMDbSearchResult>();
	public void addResult(OMDbSearchResult res) {
		results.add(res);
	}
	
	public int responseCount() {
		return results.size();
	}
	
	public List<OMDbSearchResult> getResultList() {
		return results;
	}
}
