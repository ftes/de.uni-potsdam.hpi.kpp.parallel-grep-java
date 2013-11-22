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
	public static final int maxNoThreads = Runtime.getRuntime()
			.availableProcessors();

	public final String input;
	private int activeThreads = 0;
	private final Object maxThreadCondition = new Object();

	private final Map<String, Integer> results = new HashMap<>();
	private boolean locked = false;

	/**
	 * Thread tries to obtain lock, if failing to do so it will wait outside
	 * monitor control until notified by other thread that releases lock.
	 */
	private synchronized void lockForWrite() {
		if (locked) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.err.println(e);
			}
		}
		locked = true;
	}

	/**
	 * Notifies only one other thread after unlocking, as only one thread can
	 * write at one time. If several are waiting, this notify "cascades", until
	 * at some point all are notified.
	 */
	private synchronized void unlockAfterWrite() {
		locked = false;
		notify();
	}

	public void startSearchThread(String searchString) {
		synchronized (maxThreadCondition) {
			if (activeThreads >= maxNoThreads) {
				try {
					maxThreadCondition.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			activeThreads++;
			new SearchThread(this, searchString).start();
		}
	}

	public void waitForThreadsToFinish() {
		synchronized (maxThreadCondition) {
			while (activeThreads > 0) {
				try {
					maxThreadCondition.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void writeResult(String searchString, int occurences) {
		lockForWrite();
		results.put(searchString, occurences);
		unlockAfterWrite();
		synchronized (maxThreadCondition) {
			activeThreads--;
			maxThreadCondition.notify();
		}
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
		List<String> searchStrings = new ArrayList<>();

		br = new BufferedReader(new FileReader(searchStringsFile));
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			searchStrings.add(line);
		}
		br.close();

		Iterator<String> iter = searchStrings.iterator();
		while (iter.hasNext()) {
			String searchString = iter.next();
			iter.remove();
			grep.startSearchThread(searchString);
		}
		
		grep.waitForThreadsToFinish();

		PrintWriter out;
		try {
			out = new PrintWriter(outFile);
			for (Entry<String, Integer> result : grep.results.entrySet()) {
				out.println(result.getKey() + ";" + result.getValue());
			}
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
