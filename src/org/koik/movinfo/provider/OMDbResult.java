package org.koik.movinfo.provider;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.koik.movinfo.core.db.table.Column;
import org.koik.movinfo.core.db.table.MovInfo;
import org.koik.movinfo.log.Logger;

public class OMDbResult {
	Map<Attribute, String> result = new LinkedHashMap<Attribute, String>();
	public OMDbResult(Map<String, String> map) {
		for (Attribute a : Attribute.values()) {
			String respkey = a.getResponseKey();
			String key = a.name();
			String val = map.get(respkey);
			if (val != null) {
				result.put(a, val);
			} else {
				result.put(a, "");
				Logger.warn(key + " couldn't be determined since '" + respkey + "' is not present in response");
			}
		}
	}
	
	public OMDbResult(JSONObject json) {
		this((Map)json);
	}
	
	public String get(Attribute a) {
		return result.get(a);
	}

	public String getTitle() {
		return get(Attribute.Title);
	}

	public String getYear() {
		return get(Attribute.Year);
	}

	public String getId() {
		return get(Attribute.Id);
	}
	
	@Override
	public String toString() {
		return getId() + ", " + getTitle() + ", " + getYear();
	}

	public String toCSV() {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Attribute a : result.keySet()) {
			if (first)
				first = false;
			else 
				sb.append(",");
			sb.append("\"").append(result.get(a)).append("\"");
		}
		return sb.toString();
	}
	
	static enum Attribute {
		/*
		 * All responses including rottenTomato ones:
		Title,
		Year,
		Rated,
		imdbRating,
		Released,
		Runtime,
		Genre,
		Director,
		Writer,
		Actors,
		Plot,
		Poster,
		imdbVotes,
		imdbID,
		tomatoMeter,
		tomatoImage,
		tomatoRating,
		tomatoReviews,
		tomatoFresh,
		tomatoRotten,
		tomatoConsensus,
		tomatoUserMeter,
		tomatoUserRating,
		tomatoUserReviews,
		 */
		Title("Title", MovInfo.Title), 
		Year("Year", MovInfo.Year), 
		ImdbRating("imdbRating", MovInfo.ImdbRating), 
		ImdbVotes("imdbVotes", MovInfo.ImdbVotes), 
		Rated("Rated", MovInfo.Rated), 
		Genre("Genre", MovInfo.Genre), 
		Director("Director", MovInfo.Director), 
		Plot("Plot", MovInfo.Plot), 
		Writer("Writer", MovInfo.Writer), 
		Actors("Actors", MovInfo.Actors), 
		Poster("Poster", MovInfo.Poster), 
		Runtime("Runtime", MovInfo.Runtime), 
		Released("Released", MovInfo.Released), 
		Id("imdbID", MovInfo.Id);
		
		private final String responsekey;
		private final Column<?> col;
		Attribute(String key, Column<?> col) {
			this.responsekey = key;
			this.col = col;
		}
		
		public Column<?> getCorrespondingColumn() {
			return col;
		}

		public String getResponseKey() {
			return responsekey;
		}
		
		public static String toCSV() {
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (Attribute key : values()) {
				if (first) 
					first = false;
				else 
					sb.append(",");
				sb.append("\"").append(key).append("\"");
			}
			return sb.toString();
		}
	}
}
