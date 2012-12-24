package org.koik.movinfo.provider;

import static org.koik.movinfo.util.Utils.listValidFiles;
import static org.koik.movinfo.util.Utils.load;
import static org.koik.movinfo.util.Utils.pickYears;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.koik.movinfo.core.Service;
import org.koik.movinfo.log.Logger;
import org.koik.movinfo.util.Pair;

public class SearchService implements Service<File, Pair<File, PriorityQueue<OMDbSearchResult>>> {
	OMDbAPI provider;
	ConcurrentHashMap<String, OMDbResult> cache;
	public SearchService(OMDbAPI provider, ConcurrentHashMap<String, OMDbResult> cache) {
		this.provider = provider;
		this.cache = cache;
	}

	@Override
	public Pair<File, PriorityQueue<OMDbSearchResult>> run(File file) throws Exception {
		PriorityQueue<OMDbSearchResult> retval = new PriorityQueue<OMDbSearchResult>();	
		/*
		 * Try to figure out a set of 'year's for the movie. 
		 * If a set of 'year's could be found, then:
		 * 1. Try querying for the title for each year, if we get it, we are done
		 * 2. Try searching for the title for that year, if we get it, we're done
		 * If the above fails, or if we aren't able to determine the 'year' information: 
		 * 1. Query for the title
		 * 2. Search for the title
		 * Create a cumulative list of the two above, and figure out the best fit.
		 */
		String filename = file.getName();
		String cleanFilename = clean(filename, file.isDirectory());
		
		Set<String> years = guessYear(file);
		Logger.debug(filename + " :: " + cleanFilename + " :: " + years);
		
		for (String year : years) {
			Logger.debug(filename + " -- " + year);
			OMDbGetResponse getresp = provider.get(cleanFilename, year);
			if (getresp.success()) {
				OMDbResult result = getresp.getResult();
				cache.put(result.getId(), result);
				OMDbSearchResult sres = new OMDbSearchResult(result, cleanFilename, years);
				Logger.debugPad("get-result (with year)", filename, sres.toString());
				retval.add(sres);
				return new Pair<>(file, retval);
			} else {
				Logger.debugPad("get-result (with year)", filename, "failed with error", getresp.getFailureMessage());
				OMDbSearchResponse searchresp = provider.search(cleanFilename, year);
				if (searchresp.success() && searchresp.responseCount() > 0) {
					List<OMDbSearchResult> sres = searchresp.getResultList();
					Logger.debugPad("search-result (with year)", filename, sres);
					retval.addAll(sres);
					return new Pair<>(file, retval);
				} else {
					Logger.debugPad("search-result (with year)", filename, "failed with error", searchresp.getFailureMessage());
				}
			}
		}
		
		// What if year information was available, but was not leading to any hits?
		// Let's not search with year, but use the year information now for prioritizing the responses
		OMDbGetResponse getresp = provider.get(cleanFilename);
		if (getresp.success()) {
			OMDbResult result = getresp.getResult();
			cache.put(result.getId(), result);
			OMDbSearchResult sres = new OMDbSearchResult(result, cleanFilename, years);
			Logger.debugPad("get-result (without year)", filename, sres);
			retval.add(sres);
		} else {
			Logger.debugPad("get-result (without year)", filename, "failed with error", getresp.getFailureMessage());
		}
		
		OMDbSearchResponse searchresp= provider.search(cleanFilename, years);
		if (searchresp.success()) {
			List<OMDbSearchResult> sres = searchresp.getResultList();
			Logger.debugPad("search-response (without year)", filename, sres);
			retval.addAll(sres); 
		} else {
			Logger.debugPad("search-response (without year)", filename, "failed with error", searchresp.getFailureMessage());
		}
		return new Pair<>(file, retval);
	}
	
	private Set<String> guessYear(File file) throws IOException {
		/*
		 * See if the File has a year. If not, and if the file is 
		 * a directory, go one level down in its structure, pick out 
		 * files with 'valid' extensions, and see if any of them have 
		 * year info
		 */
		Set<String> retval = new HashSet<String>();
		retval.addAll(Arrays.asList(pickYears(file.getName())));
		if (retval.size() == 0) {
			if (file.isDirectory()) {
				for (File inner : listValidFiles(file , false)) {
					retval.addAll(Arrays.asList(pickYears(inner.getName())));
				}
			}
		}
		return retval;
	}
	
	private String clean(String filename, boolean dir) throws IOException {
		synchronized("ASDF".intern()) {
			if (!dir) 
				filename = filename.replaceAll("[.][^.]*$", ""); // remove file extension
			filename = filename.toLowerCase();
			for (Pair<String, String> rule : getRules()) {
				filename = filename.replaceAll(rule.getFirst(), rule.getSecond());
			}
			return filename;
		}
	}
	
	private static volatile List<Pair<String, String>> rules1 = null;
	private static List<Pair<String, String>> getRules() throws IOException {
		if (rules1 == null) {
			synchronized ("rules1".intern()) {
				if (rules1 == null) {
					rules1 = new ArrayList<Pair<String,String>>();
					for (String rule : load("clean-rules1", false, false)) {
						rule = rule.replaceAll("//.*", "");
						if (rule.trim().equals("")) continue;
						String [] parts = rule.split(" --> ", -1);
						rules1.add(new Pair<String, String>(parts[0], parts[1]));
					}
				}
			}
		}
		return rules1;
	}
}
