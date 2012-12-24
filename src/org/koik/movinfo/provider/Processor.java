package org.koik.movinfo.provider;

import static org.koik.movinfo.core.ServiceUtils.async;
import static org.koik.movinfo.core.ServiceUtils.pipe;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.koik.movinfo.config.Property;
import org.koik.movinfo.core.Service;
import org.koik.movinfo.log.Logger;

public class Processor {
	final Sink sink;
	public Processor(Sink sink) throws Exception {
		this.sink = sink;
	}
	
	public void process(Collection<File> files) throws IOException, InterruptedException {
		
		final ThreadPoolExecutor executor = new ThreadPoolExecutor(
				Property.threadCount.intValue(), 
				Property.threadCount.intValue(), 
				0, 
				TimeUnit.SECONDS, 
				new LinkedBlockingQueue<Runnable>());
		
		final ConcurrentHashMap<String, OMDbResult> getcache = new ConcurrentHashMap<>();
		
		SearchService search = new SearchService(OMDbAPI.getInstance(), getcache);
		GetService get = new GetService(OMDbAPI.getInstance(), getcache);
		
		Service<File, Future<Future<Void>>> compositeSvc = async(pipe(search, async(pipe(get, sink), executor)), executor);
		
		List<Future<Future<Void>>> futures = new ArrayList<>();
		for (final File file : files) {
			try {
				futures.add(compositeSvc.run(file));
			} catch (Exception e) {
				// code will never come here
				e.printStackTrace();
			}
		}
		
		for (Future<Future<Void>> future : futures) {
			try {
				future.get().get(); // wait for work to get done
			} catch (ExecutionException e) {
				// syserr any exceptions caught along the way
				e.printStackTrace();
			}
		}
		
		executor.shutdown();
		while(!executor.awaitTermination(1, TimeUnit.SECONDS)) {
			// code should never come here
			Logger.warn("Waiting for executor to shutdown");
		}
	}
}
