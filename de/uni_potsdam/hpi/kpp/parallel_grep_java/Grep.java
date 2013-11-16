package de.uni_potsdam.hpi.kpp.parallel_grep_java;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Grep {
	public static final String outFile = "output.txt";
	private final Map<String, Integer> results = new HashMap<>();
	private boolean locked = false;

	private synchronized void lockForWrite() {
		if (locked) {
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
		locked = true;
	}

	private synchronized void unlockAfterWrite() {
		locked = false;
		notify();
	}

	public void writeResult(String searchString, int occurences) {
		lockForWrite();
		results.put(searchString, occurences);
		unlockAfterWrite();
	}
	
	public Map<String, Integer> getResults() {
		return results;
	}

	public static void main(String[] args) throws IOException {
		String searchStringsFile = args[0];
		String inputFile = args[1];

		List<String> searchStrings = new ArrayList<>();

		BufferedReader br = new BufferedReader(
				new FileReader(searchStringsFile));
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			searchStrings.add(line);
		}
		br.close();

		StringBuilder sb = new StringBuilder();
		br = new BufferedReader(new FileReader(inputFile));
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			sb.append(line);
			sb.append("\n");
		}
		br.close();
		final String input = sb.toString();
		
		final Grep grep = new Grep();

		List<Thread> threads = new ArrayList<>();
		for (final String searchString : searchStrings) {
			Thread thread = new SearchThread(grep, searchString, input);
			thread.start();
			threads.add(thread);
		}
		
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {}
		}
		
		//write output
		PrintWriter out = new PrintWriter(outFile);
		for (Entry<String, Integer> result : grep.getResults().entrySet()) {
			out.println(result.getKey() + ";" + result.getValue());
		}
		out.close();
	}
}
