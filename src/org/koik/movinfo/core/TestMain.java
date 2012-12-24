package org.koik.movinfo.core;

import java.io.IOException;

public class TestMain {

	/**
	 * @param args
	 * @throws Exception 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, Exception {
		args = new String[] {
//				"-v", 
//				"-n", 
//				"-c", 
//				"-f", 
//				"lafangey", 
				"-d", 
				"/run/media/sri/Media21/All Movies/done",  
				"csv"
				};
		Main.main(args);
	}	
}
