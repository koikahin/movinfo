package org.koik.movinfo.provider;

import java.text.DecimalFormat;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class OMDbSearchResult implements Comparable<OMDbSearchResult> {
	String title, year, id;
	String score;
	public void setTitle(String title) {
		this.title = title;
	}
	public String getTitle() {
		return title;
	}
	public void setYear(String year) {
		this.year = year;
	}
	public String getYear() {
		return year;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getId() {
		return id;
	}
	public void setScore(String score) {
		this.score = score;
	}
	public String getScore() {
		return score;
	}
	
	public OMDbSearchResult(
			String title, 
			String year, 
			String id,
			String query,
			Set<String> years) {
		setTitle(title);
		setYear(year);
		setId(id);
		setScore(calculateScore(query, years));
	}
	
	public OMDbSearchResult(
			OMDbResult result,
			String query,
			Set<String> years) {
		this(result.getTitle(), result.getYear(), result.getId(), query, years);
	}
	
	@Override
	public int compareTo(OMDbSearchResult o) {
		// for two results with the same score, the newer release should take precedence 
		return (o.score + o.getYear())
				.compareTo(
						(score + getYear()));
	}
	
	@Override
	public String toString() {
		return "(" + title + "," + year + "," + id + "," + score + ")";
				
	}
	private String calculateScore(String query, Set<String> years) {
		// score will be a float of the form ?.???
		double score = 0.000;
		if (years.contains(getYear())) {
			// same year => contribute 1 to the score
			score = score + 1; 
		} 
		
		// how close is the title to the query? 'distance' is the number of transformations it takes to convert one to the other
		int distance = StringUtils.getLevenshteinDistance(getTitle().toLowerCase(), query.toLowerCase());
		
		// If distance is 0 (exact match), the contribution is 1, else <1
		score = score + (((double)1)/(distance+1));
		
		return new DecimalFormat("0.000").format(score);
	}
}