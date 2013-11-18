package de.uni_potsdam.hpi.kpp.parallel_grep_java;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Grep {
	public static final String outFile = "output.txt";
	public static final int maxNoThreads = Runtime.getRuntime().availableProcessors();

	public final List<String> searchStrings = new ArrayList<>();
	public final String input;
	private int activeThreads = 0;

	private final Map<String, Integer> results = new HashMap<>();
	private boolean locked = false;

	public synchronized void lockForWrite() {
		if (locked) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.err.println(e);
			}
		}
		locked = true;
	}

	public synchronized void unlockAfterWrite() {
		locked = false;
		notify();
	}

	public void startSearchThread(String searchString) {
		activeThreads++;
		new SearchThread(this, searchString).start();
	}

	public void writeResultAndStartNewThread(String searchString, int occurences) {
		lockForWrite();
		results.put(searchString, occurences);
		activeThreads--;

		if (!searchStrings.isEmpty()) {
			searchString = searchStrings.get(0);
			searchStrings.remove(0);
			startSearchThread(searchString);
		} else if (activeThreads == 0) {
			//write result
			PrintWriter out;
			try {
				out = new PrintWriter(outFile);
				for (Entry<String, Integer> result : results.entrySet()) {
					out.println(result.getKey() + ";" + result.getValue());
				}
				out.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		unlockAfterWrite();
	}

	public Map<String, Integer> getResults() {
		return results;
	}

	public Grep(String input) {
		this.input = input;
	}

	public static void main(String[] args) throws IOException {
		System.out.println(String.format("Using %d threads", maxNoThreads));
		String searchStringsFile = args[0];
		String inputFile = args[1];

		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			sb.append(line);
			sb.append("\n");
		}
		br.close();

		final Grep grep = new Grep(sb.toString());

		br = new BufferedReader(new FileReader(searchStringsFile));
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			grep.searchStrings.add(line);
		}
		br.close();

		Iterator<String> iter = grep.searchStrings.iterator();
		int n = 0;
		grep.lockForWrite();
		while (n < maxNoThreads && iter.hasNext()) {
			String searchString = iter.next();
			iter.remove();
			n++;
			grep.startSearchThread(searchString);
		}
		grep.unlockAfterWrite();
	}
}
