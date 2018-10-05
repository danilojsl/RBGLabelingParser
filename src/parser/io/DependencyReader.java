package parser.io;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import parser.DependencyInstance;
import parser.Options;

public abstract class DependencyReader {
	
	BufferedReader reader;
	boolean isLabeled=true;
	Options options;
	
	public static DependencyReader createDependencyReader(Options options) {
		String format = options.getFormat();
		if (format.equalsIgnoreCase("CONLL06") || format.equalsIgnoreCase("CONLL-06")) {
			return new Conll06Reader(options);
		} else if (format.equalsIgnoreCase("CONLL09") || format.equalsIgnoreCase("CONLL-09")) {
			return new Conll09Reader(options);
		} else {
			System.out.printf("!!!!! Unsupported file format: %s%n", format);
			return new Conll06Reader(options);
		}
	}
	
	public abstract DependencyInstance nextInstance(Map<String, String> coarseMap) throws IOException;
	
	public void startReading(String file) throws IOException {
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
	}
	
	public void close() throws IOException { if (reader != null) reader.close(); }
    
}
