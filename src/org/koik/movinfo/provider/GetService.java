package org.koik.movinfo.provider;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.koik.movinfo.config.Property;
import org.koik.movinfo.core.Service;
import org.koik.movinfo.log.Logger;
import org.koik.movinfo.provider.OMDbResult.Attribute;
import org.koik.movinfo.util.Pair;
import org.koik.movinfo.util.Utils;

public class GetService implements Service<Pair<File, PriorityQueue<OMDbSearchResult>>, Pair<File, List<OMDbResult>>>{

	private final OMDbAPI provider; 
	private final Map<String, OMDbResult> cache;
	
	public GetService(OMDbAPI provider, Map<String, OMDbResult> cache) {
		this.provider = provider;
		this.cache = cache;
	}

	@Override
	public Pair<File, List<OMDbResult>> run(Pair<File, PriorityQueue<OMDbSearchResult>> fileSr) throws Exception {
		File file = fileSr.getFirst();
		String filename = file.getName();
		PriorityQueue<OMDbSearchResult> sr = fileSr.getSecond();
		List<OMDbSearchResult> list = new ArrayList<>();
		/*
		 * Skip all TV Series results.
		 * Include all results with same (and highest) ranking.
		 */
		List<OMDbResult> movResults = new ArrayList<>();
		OMDbSearchResult sres;
		String currHigh = null;
		Set<String> doneIds = new HashSet<>(); // same id may be present more than once, skip repeats
		Logger.debugPad("Getting info for file: " + filename);
		while((sres = sr.poll()) != null) {
			list.add(sres);
			Logger.debugPad("processing search result", filename, sres);
			if (currHigh != null && currHigh.compareTo(sres.getScore()) != 0) {
				Logger.debugPad("we are done", filename, sres);
				break; // we are done
			}
			
			if (!doneIds.add(sres.getId())) {
				Logger.debugPad("result already processed", filename, sres);
				continue; 
			}
			
			OMDbResult res = get(sres);
			Logger.debugPad("got get result", filename, res);
			if (res == null) 
				continue; // exception scenario

			if (isMovie(res)) {
				Logger.debugPad("adding to valid results", filename, res.getId());
				movResults.add(res);
				currHigh = sres.getScore();
			} else {
				Logger.debugPad("not a movie", filename, res.getId());
				// discard result
				continue;
			}
		}
		
		// let's warn the user here of 0 or more than one result here
		if (movResults.size() == 0) {
			Logger.warn("No results could be found for: " + filename);
			Logger.warn(" - fix the filename or provide more contextual information (like year) and run again");
		} else if (movResults.size() > 1) {
			Logger.warn("More than one possible results for: " + filename);
			Logger.warn(" - the file could be referring to one or more of the following: ");
			for (OMDbResult res : movResults) {
				Logger.warn(" - \t" + res.toCSV());
			}
			Logger.warn("   If one of the above is the result, try adding the corresponding year to the filename and run again");
		}
		
		return new Pair<>(file, movResults);
	}
	
	private boolean isMovie(OMDbResult res) {
		/*
		 * Not a good way to determine but:
		 * 1. The 'release date' of many tv shows is 'N/A' while that of movies is of the format dd MM yyyy
		 * 2. The 'Runtime' can be parsed to see if it is at least 55 mins
		 */
		String relDate = res.get(Attribute.Released);
		if (relDate == null || relDate.trim().equalsIgnoreCase("n/a")) {
			Logger.debugPad("not a movie because the release date is either null, non existant or 'n/a'", res.getId(), relDate);
			return false;
		} else {
			/*
			 *  future-proofing: what if they change the format later?
			 *  Just to be sure, let's try parsing the date. If we fail, 
			 *  we can at-least warn.
			 */
			try {
				new SimpleDateFormat("dd MMM yyyy").parse(relDate);
			} catch (ParseException e) {
				System.err.println("WARNING!! Release date format has changed. Current value: " + relDate);
			}
			
//			// let's check 'rated' now (edit: not true. there are several movies with rating having a 'tv'
//			if (res.get(Attribute.Rated).toLowerCase().contains("tv")) {
//				Logger.debugPad("'Rated' contains 'tv'", res.getId(), res.get(Attribute.Rated));
//				return false;
//			}
			
			// let's check the duration now
			String runtime = res.get(Attribute.Runtime);
			int durationMins = Utils.toMins(runtime);
			Logger.debugPad("runtime", runtime, durationMins);
			if (durationMins < Property.minRuntime.intValue())
				return false;
		}
		return true;
	} 

	private OMDbResult get(OMDbSearchResult sres) throws URISyntaxException, org.json.simple.parser.ParseException, IOException {
		if (cache.containsKey(sres.getId())) {
			return cache.get(sres.getId());
		} else {
			OMDbGetResponse resp = provider.getFromId(sres.getId());
			if (resp.success()) {
				return resp.getResult();
			} else {
				System.err.println("Unexpected failure in get-id: " + sres.getId() + " message: " + resp.getFailureMessage());
				return null;
			}
		}
	}
}
