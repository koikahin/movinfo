package org.koik.movinfo.core.db.table;

import static org.koik.movinfo.core.db.table.DataType.STRING;

public interface MovInfo extends Table<MovInfo>{
	public static final String name = "movinfo";
	
	public static final Column<String> 
		Filename = new Column<String>(MovInfo.class, "Filename", STRING),
		Title = new Column<String>(MovInfo.class, "Title", STRING),
		Year = new Column<String>(MovInfo.class, "Year", STRING),
		Genre = new Column<String>(MovInfo.class, "Genre", STRING),
		ImdbRating = new Column<String>(MovInfo.class, "ImdbRating", STRING),
		ImdbVotes = new Column<String>(MovInfo.class, "ImdbVotes", STRING),
		Rated = new Column<String>(MovInfo.class, "Rated", STRING),
		Released = new Column<String>(MovInfo.class, "Released", STRING),
		Runtime = new Column<String>(MovInfo.class, "Runtime", STRING),
		Director = new Column<String>(MovInfo.class, "Director", STRING),
		Writer = new Column<String>(MovInfo.class, "Writer", STRING),
		Actors = new Column<String>(MovInfo.class, "Actors", STRING),
		Plot = new Column<String>(MovInfo.class, "Plot", STRING),
		Poster = new Column<String>(MovInfo.class, "Poster", STRING),
		Id = new Column<String>(MovInfo.class, "Id", STRING),
		TomatoMeter = new Column<String>(MovInfo.class, "TomatoMeter", STRING),
		TomatoImage = new Column<String>(MovInfo.class, "TomatoImage", STRING),
		TomatoRating = new Column<String>(MovInfo.class, "TomatoRating", STRING),
		TomatoReviews = new Column<String>(MovInfo.class, "TomatoReviews", STRING),
		TomatoFresh = new Column<String>(MovInfo.class, "TomatoFresh", STRING),
		TomatoRotten = new Column<String>(MovInfo.class, "TomatoRotten", STRING),
		TomatoConsensus = new Column<String>(MovInfo.class, "TomatoConsensus", STRING),
		TomatoUserMeter = new Column<String>(MovInfo.class, "TomatoUserMeter", STRING),
		TomatoUserRating = new Column<String>(MovInfo.class, "TomatoUserRating", STRING),
		TomatoUserReviews = new Column<String>(MovInfo.class, "TomatoUserReviews", STRING)
		;
	
	
	public static interface MovRecord extends Record<MovInfo> {
		
	}
}
